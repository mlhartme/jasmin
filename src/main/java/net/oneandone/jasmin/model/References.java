/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.oneandone.jasmin.model;

import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.Compiler;

import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.ErrorManager;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import net.oneandone.jasmin.main.Servlet;
import net.oneandone.mork.mapping.ExceptionErrorHandler;
import net.oneandone.mork.mapping.Mapper;
import net.oneandone.mork.misc.GenericException;
import net.oneandone.ssass.scss.Output;
import net.oneandone.ssass.scss.Stylesheet;
import net.oneandone.sushi.fs.GetLastModifiedException;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.fs.zip.ZipNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/** Reference to a node, with minimize flag and type. */
public class References {
    public static final byte LF = 10;
    private static final Mapper SSASS;

    static {
        SSASS = new Mapper("net.oneandone.ssass.Mapper");
        SSASS.load();
    }

    public static References create(MimeType type, boolean minimize, Node node) {
        References result;

        result = new References(type, minimize);
        result.add(minimize, node);
        return result;
    }

    public final MimeType type;
    public final boolean overallMinimize;
    public final List<Boolean> minimizes;
    public final List<Node> nodes;

    public References(MimeType type, boolean overallMinimize) {
        this.type = type;
        this.overallMinimize = overallMinimize;
        this.minimizes = new ArrayList<>();
        this.nodes = new ArrayList<>();
    }

    public void add(boolean minimize, Node node) {
        minimizes.add(minimize);
        nodes.add(node);
    }

    /** core method */
    public void writeTo(Writer writer) throws IOException {
        ByteArrayOutputStream messages;
        Reader reader;
        String srcName;
        Object[] results;
        Output output;
        Mapper mapper;

        if (type == MimeType.CSS) {
            output = new Output(writer, overallMinimize);
            mapper = SSASS.newInstance();
            mapper.setErrorHandler(new ExceptionErrorHandler());
        } else {
            output = null;
            mapper = null;
        }
        for (int i = 0; i < nodes.size(); i++) {
            Node node;

            node = nodes.get(i);
            if (i > 0) {
                writer.write(LF);
            }
            if (!overallMinimize) {
                writer.write(type.comment(location(node)));
            }
            switch (type) {
                case JS :
                    reader = node.createReader();
                    srcName = node.toString();
                    Compiler compiler = new Compiler(new LoggerErrorManager());
                    List<SourceFile> sources;
                    sources = new ArrayList<>();
                    sources.add(SourceFile.fromReader(srcName, reader));
                    CompilerOptions options = new CompilerOptions();
                    options.setOutputCharset("utf-8");
                    Result result = compiler.compile(new ArrayList<SourceFile>(), sources, options);
                    reader.close();
                    if (!result.success) {
                        if (result.errors.length != 1) {
                            throw new IllegalStateException();
                        }
                        throw new IOException(result.errors[0].sourceName + ":" + result.errors[0].lineNumber + ":"
                                + result.errors[0].description);
                    }
                    writer.write(overallMinimize ? compiler.toSource() : node.readString());
                    break;
                case CSS :
                    results = mapper.run(node);
                    if (results == null) {
                        throw new IOException(node.toString() + ": css/sass error");
                    }
                    try {
                        ((Stylesheet) results[0]).toCss(output);
                    } catch (GenericException e) {
                        throw new IOException(node.toString() + ": css generation failed: " + e.getMessage(), e);
                    }
                    break;
                default :
                    throw new IllegalArgumentException(type.toString());
            }
        }
    }

    public String readString() throws IOException {
        StringWriter result;

        result = new StringWriter();
        writeTo(result);
        return result.toString();
    }

    public byte[] readBytes() throws IOException {
        return readString().getBytes("utf-8");
    }

    /* @return -1 for when unknown */
    public long getLastModified() throws GetLastModifiedException {
        long result;

        result = Long.MIN_VALUE; // will never be return because there's at least one node
        for (Node node : nodes) {
            result = Math.max(result , node.getLastModified());
        }
        return result;
    }

    //--

    @Override
    public int hashCode() {
        return nodes.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        References references;

        if (obj instanceof References) {
            references = (References) obj;
            return type == references.type && minimizes.equals(references.minimizes) && nodes.equals(references.nodes);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder builder;

        builder = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            builder.append(nodes.get(i).getURI());
            builder.append(minimizes.get(i) ? "(min) " : " ");
        }
        return builder.toString();
    }

    //--

    // do not return full paths -- security!
    public static String location(Node node) {
        String name;
        int idx;

        if (node instanceof ZipNode) {
            name = ((ZipNode) node).getRoot().getZip().getName();
            idx = name.lastIndexOf('/');
            if (idx >= 0) {
                name = name.substring(idx + 1);
            }
            return "zip:" + name + "/" + node.getPath();
        } else if (node instanceof FileNode) {
            return "file:" + node.getRelative(node.getWorld().getWorking());
        } else {
            return node.getName();
        }
    }


    //--

    public class LoggerErrorManager extends BasicErrorManager {
        public LoggerErrorManager() {
        }

        @Override
        public void println(CheckLevel level, JSError error) {
            switch (level) {
                case ERROR:
                    Servlet.LOG.debug("error: " + error.toString());
                    break;
                case WARNING:
                    Servlet.LOG.debug("warning: " + error.toString());
                    break;
            }
        }

        @Override
        protected void printSummary() {
        }
    }
}
