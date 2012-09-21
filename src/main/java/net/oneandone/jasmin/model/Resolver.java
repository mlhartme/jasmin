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

import net.oneandone.jasmin.descriptor.Base;
import net.oneandone.jasmin.descriptor.Resource;
import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.fs.file.FileNode;
import net.oneandone.sushi.fs.webdav.WebdavNode;
import net.oneandone.sushi.fs.webdav.WebdavRoot;
import net.oneandone.sushi.fs.zip.ZipNode;
import org.pustefixframework.live.LiveResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/** Maps a file to a file node; handles live resources. */
public class Resolver {
    private World world;
    private final LiveResolver live;
    private final Map<Base, Node> bases;

    public Resolver(World world) {
        this(world, false);
    }

    public Resolver(World world, boolean live) {
        this.world = world;
        this.live = live ? new LiveResolver() : null;
        this.bases = new HashMap<Base, Node>();
    }

    public World getWorld() {
        return world;
    }

    //--

    public boolean isLife() {
        return live != null;
    }

    public FileNode getLiveXml() {
        java.io.File file;

        file =  live.getLiveXmlFile();
        return file != null ? world.file(file) : null;
    }

    //--

    public Node get(Base base) {
        return bases.get(base);
    }

    public void add(Base base, Node node) {
        if (bases.put(base, node) != null) {
            throw new IllegalArgumentException("already mapped: " + base);
        }
    }

    public void reset() {
        bases.clear();
    }

    //--

    public File resolve(Node classpathBase, Resource resource) throws IOException {
        Base base;
        Node baseResolved;
        Node normal;
        String minimizedPath;
        Node minimized;

        base = resource.getBase();
        if (base == Base.CLASSPATH) {
            baseResolved = classpathBase;
        } else {
            baseResolved = bases.get(base);
        }
        if (baseResolved == null) {
            throw new IllegalStateException("unknown base: " + base.toString());
        }
        normal = resolve(baseResolved, resource.getPath());
        minimizedPath = resource.getMinPath();
        minimized = minimizedPath == null ? null : resolve(baseResolved, minimizedPath);
        return new File(normal, minimized, resource.getType(), resource.getVariant());
    }

    public Node resolve(Node root, String path) throws IOException {
        try {
            return doResolve(root, path);
        } catch (IOException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("LifeResolver failed", e);
        }
    }

    private Node doResolve(Node root, String path) throws Exception {
        java.io.File file;
        FileNode resolvedRoot;
        WebdavRoot webdavRoot;
        int idx;
        String pathPart;
        String queryPart;
        Node result;

        if (path.startsWith("/")) {
            throw new IllegalArgumentException(path);
        }
        if (root instanceof WebdavNode) {
            webdavRoot = ((WebdavNode) root).getRoot();
            idx = path.indexOf('?');
            if (idx == -1) {
                pathPart = path;
                queryPart = null;
            } else {
                pathPart = path.substring(0, idx);
                queryPart = path.substring(idx + 1);
            }
            if (!root.getPath().isEmpty()) {
                pathPart = webdavRoot.getFilesystem().join(root.getPath(), pathPart);
            }
            return webdavRoot.node(pathPart, queryPart);
        }
        if (live != null) {
            if (root instanceof FileNode) {
                file = live.resolveLiveRoot(((FileNode) root).getAbsolute(), "/" + path);
            } else if (root instanceof ZipNode) {
                file = live.resolveLiveRoot(((ZipNode) root).getRoot().getZip().getName(), "/" + path);
            } else {
                file = null;
            }
            if (file != null) {
                resolvedRoot = world.file(file);
                result = resolvedRoot.join(path);
                if (result.exists()) {
                    return result;
                }
            }
        }
        return root.join(path);
    }
}
