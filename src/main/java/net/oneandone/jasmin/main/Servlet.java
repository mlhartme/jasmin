/*
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
package net.oneandone.jasmin.main;

import net.oneandone.jasmin.model.Engine;
import net.oneandone.jasmin.model.File;
import net.oneandone.jasmin.model.Module;
import net.oneandone.jasmin.model.Resolver;
import net.oneandone.jasmin.model.Source;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.fs.http.HttpFilesystem;
import net.oneandone.sushi.fs.http.HttpNode;
import net.oneandone.sushi.util.Strings;
import org.json.JSONException;
import org.json.JSONWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class Servlet extends HttpServlet {
    public static final Logger LOG = LoggerFactory.getLogger(Servlet.class);

    private static final String HOSTNAME = getHostname();

    private static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.error("unknown hostname", e);
            return "unknown";
        }
    }

    // re-create engine if one of these files was changed; null if life resolving is off
    private List<Node> reloadFiles;
    private long loaded;
    private long otherVmStartupDate;

    private FileNode docroot;
    private Application application;

    // lazy init, because I need a URL first:
    private Engine engine;

    public Servlet() {
        // NOT longer than 10 years because the date format has only 2 digits for the year.
        this.otherVmStartupDate = VM_STARTUP_DATE.getTime() - TEN_YEARS;
    }

    /** creates configuration. */
    @Override
    public void init(ServletConfig config) throws ServletException {
        World world;
        String str;

        try {
            world = World.create();
            configure(world, "http");
            configure(world, "https");
            str = config.getInitParameter("docroot");
            docroot = Application.file(world, str != null ? str : config.getServletContext().getRealPath(""));
            docroot.checkDirectory();
            LOG.info("home: " + world.getHome());
            application = Application.load(world, config, docroot);
            LOG.info("docroot: " + docroot);
        } catch (RuntimeException | Error e) {
            error(null, "init", e);
            throw e;
        } catch (Exception e) {
            error(null, "init", e);
            throw new ServletException(e);
        } catch (Throwable e) {
            error(null, "init", e);
            throw new RuntimeException("unexpected throwable", e);
        }
    }

    private static final int HTTP_TIMEOUT = 10 * 1000;

    private static void configure(World world, String scheme) {
        HttpFilesystem webdav;

        webdav = world.getFilesystem(scheme, HttpFilesystem.class);
        webdav.setDefaultConnectionTimeout(HTTP_TIMEOUT);
        webdav.setDefaultReadTimeout(HTTP_TIMEOUT);
    }

    /** Creates engine from configuration and resolve. Sychronized ensures we initialize only once. */
    private synchronized void lazyInit(HttpServletRequest request) throws IOException {
        List<File> files;
        URL url;
        long lastModified;
        long now;
        Resolver resolver;
        Node localhost;
        FileNode file;
        Object[] tmp;

        resolver = application.resolver;
        if (engine != null && resolver.isLife()) {
            for (Node node : reloadFiles) {
                lastModified = node.getLastModified();
                if (lastModified > loaded) {
                    now = System.currentTimeMillis();
                    if (lastModified > now) {
                        // fail to avoid repeated re-init
                        throw new IOException(node.getUri() + " has lastModifiedDate in the future: "
                                + new Date(lastModified) + "(now: " + new Date(now) + ")");
                    }
                    LOG.info("reloading jasmin for application '" + application.getContextPath() + "' - changed file: " + node);
                    engine = null;
                    resolver.reset();
                }
            }
        }
        if (engine == null) {
            url = new URL(request.getRequestURL().toString());
            try {
                localhost = resolver.getWorld().node(new URI(url.getProtocol(), null, url.getHost(), url.getPort(), "", null, null));
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
            tmp = application.createEngine(docroot, localhost);
            engine = (Engine) tmp[0];
            LOG.info("started engine, initial url=" + url);
            for (Module module : engine.repository.modules()) {
                files = module.files();
                if (files.size() > 2) {
                    LOG.warn("deprecated: module '" + module.getName() + "' contains more than 2 file: " + files);
                }
                for (File f : files) {
                    if (f.getNormal() instanceof HttpNode) {
                        LOG.warn("deprecated: module '" + module.getName() + "' uses base LOCALHOST: " + f.getNormal().getUri());
                    }
                }
            }
            if (resolver.isLife()) {
                reloadFiles = (List<Node>) tmp[1];
                file = resolver.getLiveXml();
                if (file != null) {
                    reloadFiles.add(file);
                }
                LOG.info("reload if one of these " + reloadFiles.size() + " files is modified: ");
                for (Node node : reloadFiles) {
                    LOG.info("  " + node.getUri());
                }
                loaded = System.currentTimeMillis();
            }
            if (engine == null) {
                throw new IllegalStateException();
            }
        }
    }

    //--

    /**
     * Called by the servlet engine to process get requests:
     * a) to set the Last-Modified header in the response
     * b) to check if 304 can be redurned if the "if-modified-since" request header is present
     * @return -1 for when unknown
     */
    @Override
    public long getLastModified(HttpServletRequest request) {
        String path;
        int idx;
        long result;

        result = -1;
        try {
            path = request.getPathInfo();
            if (path != null && path.startsWith("/get/")) {
                lazyInit(request);
                path = path.substring(5);
                idx = path.indexOf('/');
                if (idx != -1) {
                    path = path.substring(idx + 1);
                    result = engine.getLastModified(path);
                }
            }
        } catch (IOException e) {
            error(request, "getLastModified", e);
            // fall-through
        } catch (RuntimeException | Error e) {
            error(request, "getLastModified", e);
            throw e;
        }
        LOG.debug("getLastModified(" + request.getPathInfo() + ") -> " + result);
        return result;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            doGetUnchecked(request, response);
        } catch (IOException e) {
            // I can't compile against this class because the servlet api does not officially
            // report this situation ...
            // See http://tomcat.apache.org/tomcat-5.5-doc/catalina/docs/api/org/apache/catalina/connector/ClientAbortException.html
            if (e.getClass().getName().equals("org.apache.catalina.connector.ClientAbortException")) {
                // this is not an error: the client browser closed the response stream, e.g. because
                // the user already left the page
                LOG.info("aborted by client", e);
            } else {
                error(request, "get", e);
            }
            throw e;
        } catch (RuntimeException | Error e) {
            error(request, "get", e);
            throw e;
        }
    }

    private static final String MODULE_PREFIX = "/admin/module/";

    private void doGetUnchecked(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path;

        path = request.getPathInfo();
        if (path == null) {
            response.sendRedirect(request.getContextPath() + request.getServletPath() + "/");
            return;
        }
        lazyInit(request);
        LOG.debug("get " + path);
        if (path.startsWith("/get/")) {
            get(request, response, path.substring(5));
            return;
        }
        if (path.equals("/admin/")) {
            main(response);
            return;
        }
        if (path.equals("/admin/repository")) {
            repository(request, response);
            return;
        }
        if (path.equals("/admin/hashCache")) {
            hashCache(response);
            return;
        }
        if (path.equals("/admin/contentCache")) {
            contentCache(response);
            return;
        }
        if (path.startsWith(MODULE_PREFIX)) {
            module(request, response, path.substring(MODULE_PREFIX.length()));
            return;
        }
        if (path.equals("/admin/reload")) {
            reload(response);
            return;
        }
        if (path.equals("/admin/check")) {
            fileCheck(response);
            return;
        }
        notFound(request, response);
    }

    private static final long FIVE_MINUTES = 1000L * 60 * 5;
    private static final long SEVEN_DAYS = 1000L * 3600 * 24 * 7;
    private static final long TEN_YEARS = 1000L * 3600 * 24 * 365 * 10;

    private void get(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        String version;
        boolean expire;
        int idx;
        long started;
        long duration;
        int bytes;
        boolean gzip;
        long date;

        idx = path.indexOf('/');
        if (idx == -1) {
            notFound(request, response);
            return;
        }
        started = System.currentTimeMillis();
        version = path.substring(0, idx);
        expire = !"no-expires".equals(version);
        if (expire && !VM_STARTUP_STR.equals(version)) {
            try {
                synchronized (FMT) {
                    date = FMT.parse(version).getTime();
                }
            } catch (ParseException e) {
                notFound(request, response);
                return;
            }
            if (sameTime(VM_STARTUP, date) || sameTime(otherVmStartupDate, date)) {
                // ok
            } else if (date > otherVmStartupDate) {
                otherVmStartupDate = date;
            } else {
                // usually, otherVmStartupDate is smaller, but after switching back, VM_STARTUP will be smaller
                if (Math.min(otherVmStartupDate, VM_STARTUP) - date > SEVEN_DAYS) {
                    gone(request, response);
                    return;
                }
            }
        }
        path = path.substring(idx + 1);
        if (application.resolver.isLife()) {
            // unknown headers are ok: see http://tools.ietf.org/html/rfc2616#section-7.1
            response.addHeader("Hi", "Sie werden bedient von Jasmin, vielen Dank fuer ihren Request!");
        }
        checkCharset(request.getHeader("Accept-Charset"));
        if (expire && application.expires != null) {
            response.setDateHeader("Expires", started + 1000L * application.expires);
            response.addHeader("Cache-Control", "max-age=" + application.expires);
        }
        gzip = canGzip(request);
        bytes = engine.request(path, response, gzip);
        duration = System.currentTimeMillis() - started;
        LOG.info(path + "|" + bytes + "|" + duration + "|" + gzip + "|" + referer(request));
    }

    private static boolean sameTime(long left, long right) {
        long diff;

        diff = left - right;
        if (diff < 0) {
            diff = -diff;
        }
        return diff < FIVE_MINUTES;
    }

    private static boolean canGzip(HttpServletRequest request) {
        String accepted;

        accepted = request.getHeader("Accept-Encoding");
        return accepted != null && contains(accepted, "gzip");
    }

    // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    public static void checkCharset(String accepts) throws IOException {
        if (accepts == null) {
            // ie7 does not send this header
            return;
        }
        // I've seen both "utf-8" and "UTF-8" -> test case-insensitive
        if (contains(accepts.toLowerCase(), Engine.ENCODING)) {
            return;
        }
        if (contains(accepts, "*")) {
            return;
        }
        throw new IOException(Engine.ENCODING + " encoding is not accepted: " + accepts);
    }

    // see http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    public static boolean contains(String list, String keyword) {
        int idx;
        int colon;
        String quality;

        idx = list.indexOf(keyword);
        if (idx == -1) {
            return false;
        }
        idx += keyword.length();
        colon = list.indexOf(",", idx);
        if (colon == -1) {
            colon = list.length();
        }
        quality = list.substring(idx, colon);
        idx = quality.indexOf('=');
        if (idx == -1) {
            return true;
        }
        return !"0".equals(quality.substring(idx + 1).trim());
    }

    private void main(HttpServletResponse response) throws IOException {
        html(response,
                "<p>Jasmin Servlet " + getVersion() + "</p>",
                "<p>Hostname: " + HOSTNAME + "</p>",
                "<p>Docroot: " + docroot.getAbsolute() + "</p>",
                "<p>VM Startup: " + VM_STARTUP_STR + "</p>",
                "<p>Other VM Startup: " + FMT.format(otherVmStartupDate) + "</p>",
                "<p>Loaded: " + new Date(loaded) + "</p>",
                "<p>HashCache: " + engine.hashCache.getMaxSize() + "</p>",
                "<p>ContentCache: " + engine.contentCache.getMaxSize() + "</p>",
                "<p>Requested Bytes: " + engine.requestedBytes.get() + "</p>",
                "<p>Computed Bytes: " + engine.computedBytes() + "</p>",
                "<p>Removed Bytes: " + engine.removedBytes() + "</p>",
                "<p>Load: " + engine.load() + "</p>",
                application.resolver.isLife() ? "<a href='reload'>Reload Files</a>" : "(no reload)",
                "<a href='repository'>Repository</a>",
                "<a href='hashCache'>Hash Cache</a>",
                "<a href='contentCache'>Content Cache</a>",
                "<a href='check'>File Check</a>");
    }

    private String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    private void repository(HttpServletRequest request, HttpServletResponse response) throws IOException {
        JSONWriter dest;
        List<Module> modules;

        response.setContentType("application/json");
        try (Writer writer = response.getWriter()) {
            dest = new JSONWriter(writer);
            dest.array();
            modules = new ArrayList<Module>(engine.repository.modules());
            Collections.sort(modules, new Comparator<Module>() {
                @Override
                public int compare(Module left, Module right) {
                    return left.getName().compareTo(right.getName());
                }
            });
            for (Module module : modules) {
                dest.object();
                dest.key("name");
                dest.value(module.getName());
                dest.key("details");
                dest.value(Strings.removeRight(request.getRequestURL().toString(), "/repository") + "/module/" + module.getName());
                dest.endObject();
            }
            dest.endArray();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private void hashCache(HttpServletResponse response) throws IOException {
        text(response, engine.hashCache.toString());
    }

    private void contentCache(HttpServletResponse response) throws IOException {
        text(response, engine.contentCache.toString());
    }


    private void module(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        JSONWriter dest;
        Module module;
        Source source;

        module = engine.repository.lookup(name);
        source = module.getSource();
        if (module == null) {
            notFound(request, response);
            return;
        }
        response.setContentType("application/json");
        try (Writer writer = response.getWriter()) {
            dest = new JSONWriter(writer);
            dest.object();
            dest.key("name");
            dest.value(module.getName());
            dest.key("files");
            dest.array();
            for (File file : module.files()) {
                dest.object();
                dest.key("type");
                dest.value(file.getType());
                dest.key("normal");
                dest.value(file.getNormal().getUri());
                if (file.getMinimized() != null) {
                    dest.key("minimized");
                    dest.value(file.getMinimized().getUri());
                }
                dest.key("variant");
                dest.value(file.getVariant());
                dest.endObject();
            }
            dest.endArray();
            dest.key("dependencies");
            dest.array();
            for (Module dependency : module.dependencies()) {
                dest.value(dependency.getName());
            }
            dest.endArray();
            dest.key("source");
            dest.object();
            dest.key("artifactId");
            dest.value(source.artifactId);
            dest.key("groupId");
            dest.value(source.groupId);
            dest.key("version");
            dest.value(source.version);
            dest.key("scm");
            dest.value(source.scm);
            dest.endObject();
            dest.endObject();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    private void reload(HttpServletResponse response) throws IOException {
        String[] lines;

        lines = new String[reloadFiles.size()];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = reloadFiles.get(i).getUri().toString();
        }
        text(response, lines);
    }

    private void fileCheck(HttpServletResponse response) throws IOException {
        FileCheck check;

        check = new FileCheck();
        check.minimize(true, engine.repository, application.resolver.getWorld());
        text(response, check.toString());
    }

    private void text(HttpServletResponse response, String... lines) throws IOException {
        response.setContentType("text/plain");
        try (Writer writer = response.getWriter()) {
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }

    private void html(HttpServletResponse response, String... lines) throws IOException {
        response.setContentType("text/html");
        try (Writer writer = response.getWriter()) {
            writer.write("<html><header></header><body>\n");
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }
            writer.write("</body>");
        }
    }

    private void notFound(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.warn("not found: " + request.getPathInfo());
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void gone(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LOG.warn("gone: " + request.getPathInfo());
        response.sendError(HttpServletResponse.SC_GONE);
    }

    //--

    /** @param request may be null */
    private void error(HttpServletRequest request, String method, Throwable throwable) {
        StringBuilder message;

        message = new StringBuilder();
        message.append(method).append(":").append(throwable.getMessage());
        if (request != null) {
            message.append('(');
            message.append("referer=").append(referer(request));
            message.append(",pathinfo=").append(pathInfo(request));
        }
        LOG.error(message.toString(), throwable);
    }

    private static String pathInfo(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getPathInfo();
    }
    private static String referer(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("Referer");
    }

    //-- XSLT functions

    public static final SimpleDateFormat FMT = new SimpleDateFormat("yyMMdd-HHmm");
    public static final Date VM_STARTUP_DATE = new Date();
    public static final long VM_STARTUP = VM_STARTUP_DATE.getTime();
    public static final String VM_STARTUP_STR = FMT.format(VM_STARTUP_DATE);

    public static String getVmStartup() {
        return VM_STARTUP_STR;
    }
}
