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
package net.oneandone.jasmin.main;

import net.oneandone.jasmin.descriptor.Base;
import net.oneandone.jasmin.model.File;
import net.oneandone.jasmin.model.Module;
import net.oneandone.jasmin.model.References;
import net.oneandone.jasmin.model.Repository;
import net.oneandone.jasmin.model.Resolver;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileCheck {
    private static final Logger LOG = Logger.getLogger(FileCheck.class);

    public static void main(String[] args) throws IOException {
        World world;
        FileCheck check;

        // usually called via wsd-app parent pom - without log4j config in place ...
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        Logger.getRootLogger().setLevel(Level.ERROR);
        Logger.getRootLogger().addAppender(ca);

        world = new World();
        if (args.length > 0) {
            world.setWorking(world.file(args[0]));
        }
        check = new FileCheck();
        check.minimizeApplication(getWepappDir((FileNode) world.getWorking().join("target")));
        System.out.println("file-check: " + check.size() + " files ...");
        check = check.exceptions();
        if (check.size() > 0) {
            System.err.println("failed with "  + check.size() + " errors:");
            System.err.println(check.toString());
            System.exit(1);
        } else {
            System.out.println("ok");
        }
    }

    private static FileNode getWepappDir(FileNode base) throws IOException {
        for (FileNode dir : base.list()) {
            if (dir.join("WEB-INF").isDirectory()) {
                return dir;
            }
        }
        throw new IOException("no webapp dir in " + base);
    }

    private final Map<Node, List<Exception>> map;

    public FileCheck() {
        this(new HashMap<Node, List<Exception>>());
    }

    public FileCheck(Map<Node, List<Exception>> map) {
        this.map = map;
    }

    public FileCheck exceptions() {
        Map<Node, List<Exception>> result;

        result = new HashMap<Node, List<Exception>>();
        for (Map.Entry<Node, List<Exception>> entry : map.entrySet()) {
            if (entry.getValue().size() > 0) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return new FileCheck(result);
    }

    @Override
    public String toString() {
        StringBuilder result;

        result = new StringBuilder();
        for (Map.Entry<Node, List<Exception>> entry : map.entrySet()) {
            result.append(entry.getKey().getURI()).append('\n');
            for (Exception e : entry.getValue()) {
                result.append("  ").append(e.getMessage()).append('\n');
            }
        }
        return result.toString();
    }

    public void add(Node node) {
        if (!map.containsKey(node)) {
            map.put(node, new ArrayList<Exception>());
        }
    }

    public void add(Node node, Exception e) {
        map.get(node).add(e);
    }

    //--

    public void minimizeClasspath() throws IOException {
        World world;
        Resolver resolver;

        world = new World();
        resolver = new Resolver(world);
        minimize(false, Repository.load(resolver), world);
    }

    public void minimizeApplication(FileNode application) throws IOException {
        World world;
        Repository repository;
        Node descriptor;
        Resolver resolver;

        world = new World();
        resolver = new Resolver(world);
        resolver.add(Base.DOCROOT, application);
        resolver.add(Base.LOCALHOST, world.validNode("http://nosuchhost"));
        repository = new Repository();
        repository.loadClasspath(resolver);
        descriptor = application.join("conf/jasmin.xml");
        if (!descriptor.exists()) {
            descriptor = application.join("WEB-INF/jasmin.xml");
            if (!descriptor.exists()) {
                descriptor = null;
            }
        }
        if (descriptor != null) {
            repository.loadApplication(resolver, application, descriptor);
        }
        repository.link();
        minimize(false, repository, application.getWorld());
    }

    public void minimize(boolean http, Repository repository, World world) throws IOException {
        Node src;
        Node dest;
        long started;
        Writer writer;

        for (Module module : repository.modules()) {
            for (File file : module.files()) {
                src = file.getNormal();
                if (http || !"http".equals(src.getRoot().getFilesystem().getScheme())) {
                    add(src);
                    try {
                        dest = world.getTemp().createTempFile();
                        LOG.info("fileCheck module=" + module.getName() + ", file=" + src);
                        started = System.currentTimeMillis();
                        writer = dest.createWriter();
                        References.create(file.getType(), true, src).writeTo(writer);
                        writer.close();
                        LOG.info("done, " + (System.currentTimeMillis() - started) + " ms");
                        dest.deleteFile();
                    } catch (Exception e) {
                        add(src, e);
                    }
                }
            }
        }
    }

    public int size() {
        return map.size();
    }
}
