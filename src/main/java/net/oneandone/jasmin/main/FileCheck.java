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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileCheck {
    private static final Logger LOG = LoggerFactory.getLogger(FileCheck.class);

    private final Map<Node, List<Exception>> map;

    public FileCheck() {
        this(new HashMap<Node, List<Exception>>());
    }

    public FileCheck(Map<Node, List<Exception>> map) {
        this.map = map;
    }

    public FileCheck exceptions() {
        Map<Node, List<Exception>> result;

        result = new HashMap<>();
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
            result.append(entry.getKey().getUri()).append('\n');
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

        world = World.create();
        resolver = new Resolver(world);
        minimize(false, Repository.load(resolver), world);
    }

    public void minimizeApplication(FileNode application) throws IOException {
        World world;
        Repository repository;
        Node descriptor;
        Resolver resolver;

        world = World.create();
        resolver = new Resolver(world);
        resolver.add(Base.DOCROOT, application);
        resolver.add(Base.LOCALHOST, world.validNode("http://nosuchhost"));
        repository = new Repository();
        repository.loadClasspath(resolver);
        descriptor = application.join("conf/jasmin.xml");
        if (descriptor.exists()) {
            throw new IllegalStateException("unexpected location for descriptor: " + descriptor);
        }
        descriptor = application.join(Repository.APPLICATION_DESCRIPTOR);
        if (descriptor.exists()) {
            repository.loadApplication(resolver, application, descriptor);
        }
        repository.link();
        minimize(false, repository, application.getWorld());
    }

    public void minimize(boolean http, Repository repository, World world) throws IOException {
        Node src;
        Node dest;
        long started;

        for (Module module : repository.modules()) {
            for (File file : module.files()) {
                src = file.getNormal();
                if (http || !"http".equals(src.getRoot().getFilesystem().getScheme())) {
                    add(src);
                    try {
                        dest = world.getTemp().createTempFile();
                        LOG.info("fileCheck module=" + module.getName() + ", file=" + src);
                        started = System.currentTimeMillis();
                        try (Writer writer = dest.newWriter()) {
                            References.create(file.getType(), true, src).writeTo(writer);
                        }
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
