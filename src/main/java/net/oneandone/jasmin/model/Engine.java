/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.jasmin.model;

import net.oneandone.graph.CyclicDependency;
import net.oneandone.jasmin.main.Servlet;
import net.oneandone.sushi.fs.GetLastModifiedException;
import net.oneandone.sushi.io.Buffer;
import net.oneandone.sushi.util.Strings;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Engine {
    public static final String ENCODING = "utf-8";

    public final Repository repository;
    private final Semaphore computeSemaphore;
    public final HashCache hashCache;
    public final ContentCache contentCache;
    private final HashMap<String, CountDownLatch> pending;

    /** of compressed content */
    public AtomicLong requestedBytes;

    public Engine(Repository repository) {
        this(repository, 8, 1000000, 10000000);
    }

    public Engine(Repository repository, int maxComputeThreads, int hashSize, int contentSize) {
        this.repository = repository;
        this.computeSemaphore = new Semaphore(maxComputeThreads);
        this.hashCache = new HashCache(hashSize);
        this.contentCache = new ContentCache(contentSize);
        this.pending = new HashMap<>();
        this.requestedBytes = new AtomicLong();
    }

    /** number of threads that could compute a unique path */
    public int load() {
        return pending.size();
    }

    public long requestedBytes() {
        return requestedBytes.get();
    }
    public long computedBytes() {
        return contentCache.addedBytes();
    }

    public long removedBytes() {
        return contentCache.removedBytes();
    }

    /**
     * Output is prepared in-memory before the response is written because
     * a) that's the common case where output is cached. If output is to big for this, that whole caching doesn't work
     * b) I send sent proper error pages
     * c) I can return the number of bytes actually written
     * @return bytes written or -1 if building the module content failed.
     */
    public int request(String path, HttpServletResponse response, boolean gzip) throws IOException {
        Content content;
        byte[] bytes;

        try {
            content = doRequest(path);
        } catch (IOException e) {
            Servlet.LOG.error("request failed: " + e.getMessage(), e);
            response.setStatus(500);
            response.setContentType("text/html");
            try (Writer writer = response.getWriter()) {
                writer.write("<html><body><h1>" + e.getMessage() + "</h1>");
                writer.write("<details><br/>");
                printException(e, writer);
                writer.write("</body></html>");
            }
            return -1;
        }
        if (gzip) {
            // see "High Performance Websites", by Steve Souders
            response.setHeader("Content-Encoding", "gzip");
            response.addHeader("Cache-Control", "private");
            bytes = content.bytes;
        } else {
            bytes = unzip(content.bytes);
        }
        response.setBufferSize(0);
        response.setContentType(content.mimeType);
        response.setCharacterEncoding(ENCODING); // TODO: inspect header - does this have an effect?
        if (content.lastModified != -1) {
            response.setDateHeader("Last-Modified", content.lastModified);
        }
        response.getOutputStream().write(bytes);
        return bytes.length;
    }

    private void printException(Throwable e, Writer writer) throws IOException {
        writer.write("type: " + e.getClass() + "<br/>\n");
        writer.write("message: " + e.getMessage() + "<br/>\n");
        for (StackTraceElement element : e.getStackTrace()) {
            writer.write("  " + element.toString() + "<br/>\n");
        }
        if (e.getCause() != null && e.getCause() != e) {
            writer.write("<br/>\n");
            writer.write("... cause by ... <br/>\n");
            writer.write("<br/>\n");
            printException(e.getCause(), writer);
        }
    }

    /** Convenience method for testing */
    public String request(String path) throws IOException {
        Content content;

        content = doRequest(path);
        return new String(unzip(content.bytes), ENCODING);
    }

    /* @return -1 (i.e: unknown) when not cached; not computed */
    public long getLastModified(String path) throws GetLastModifiedException {
        String hash;
        Content content;

        hash = hashCache.probe(path);
        if (hash != null) {
            content = contentCache.probe(hash);
            if (content != null) {
                return content.lastModified;
            }
        }
        return -1;
    }

    public void free() {
        hashCache.resize(0);
        contentCache.resize(0);
    }

    //--

    private Content doRequest(String path) throws IOException {
        Content result;

        result = doRequestNoStats(path);
        requestedBytes.addAndGet(result.bytes.length);
        return result;
    }

    /** @return gzip compressed content */
    private Content doRequestNoStats(String path) throws IOException {
        AtomicLong l;
        String hash;
        Content content;
        CountDownLatch gate;

        while (true) {
            hash = hashCache.lookup(path);
            if (hash != null) {
                content = contentCache.lookup(hash);
                if (content != null) {
                    return content;
                }
            }
            synchronized (pending) {
                gate = pending.get(path);
                if (gate == null) {
                    gate = new CountDownLatch(1);
                    if (pending.put(path, gate) != null) {
                        throw new IllegalStateException();
                    }
                    // we're the first thread to request this path -- compute it
                    break;
                } else {
                    if (gate.getCount() != 1) {
                        throw new IllegalStateException(path + " " + gate.getCount());
                    }
                }
            }
            try {
                // wait until the thread that's computing this path has finished
                gate.await();
            } catch (InterruptedException e) {
                // continue
            }
            // continue loop - content is either cached now or we try to re-compute it
        }

        return doCompute(path, gate);
    }

    private Content doCompute(String path, CountDownLatch gate) throws IOException {
        try {
            computeSemaphore.acquire();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
        try {
            return doComputeUnlimited(path, gate);
        } finally {
            computeSemaphore.release();
        }
    }

    /** exactly one thread will invoke this method for one path */
    private Content doComputeUnlimited(String path, CountDownLatch gate) throws IOException {
        long startContent;
        long endContent;
        ByteArrayOutputStream result;
        References references;
        byte[] bytes;
        String hash;
        Content content;

        if (gate.getCount() != 1) {
            throw new IllegalStateException(path + " " + gate.getCount());
        }
        startContent = System.currentTimeMillis();
        try {
            try {
                references = repository.resolve(Request.parse(path));
            } catch (CyclicDependency e) {
                throw new RuntimeException(e.toString(), e);
            } catch (IOException e) {
                throw new IOException(path + ": " + e.getMessage(), e);
            }
            result = new ByteArrayOutputStream();
            try (OutputStream dest = new GZIPOutputStream(result); Writer writer = new OutputStreamWriter(dest, ENCODING)) {
                references.writeTo(writer);
            }
            bytes = result.toByteArray();
            endContent = System.currentTimeMillis();
            hash = hash(bytes);
            content = new Content(references.type.getMime(), references.getLastModified(), bytes);
            hashCache.add(path, hash, endContent /* that's where hash computation starts */, 0 /* too small for meaningful measures */);
            contentCache.add(hash, content, startContent, endContent - startContent);
        } finally {
            synchronized (pending) {
                if (gate.getCount() != 1) {
                    throw new IllegalStateException(path + " " + gate.getCount());
                }
                if (pending.remove(path) != gate) {
                    throw new IllegalStateException();
                }
                gate.countDown();
            }
        }
        return content;

    }

    private static final MessageDigest DIGEST;

    static {
        try {
            DIGEST = MessageDigest.getInstance("SHA");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String hash(byte[] bytes) {
        byte[] result;

        synchronized (DIGEST) {
            result = DIGEST.digest(bytes);
        }
        return Strings.toHex(result);
    }

    private static byte[] unzip(byte[] bytes) {
        // TODO: pool?
        try {
            return new Buffer().readBytes(new GZIPInputStream(new ByteArrayInputStream(bytes)));
        } catch (IOException e) {
            throw new IllegalStateException("unexpected IOException from ByteArrayInputStream: " + e.getMessage(), e);
        }
    }

}
