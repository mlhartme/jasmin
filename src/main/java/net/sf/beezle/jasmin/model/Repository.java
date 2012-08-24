package net.sf.beezle.jasmin.model;

import net.sf.beezle.jasmin.descriptor.Base;
import net.sf.beezle.jasmin.descriptor.Library;
import net.sf.beezle.jasmin.descriptor.Resource;
import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.fs.file.FileNode;
import net.sf.beezle.sushi.fs.filter.Filter;
import net.sf.beezle.sushi.fs.memory.MemoryNode;
import net.sf.beezle.sushi.fs.webdav.WebdavNode;
import net.sf.beezle.sushi.fs.zip.ZipNode;
import net.sf.beezle.sushi.graph.CyclicDependency;
import net.sf.beezle.sushi.graph.Graph;
import net.sf.beezle.sushi.util.Strings;
import net.sf.beezle.sushi.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** A list of modules. Plus load functionality (including linking and reload file handling */
@net.sf.beezle.sushi.metadata.annotation.Type
public class Repository {
    /** simplified load method for testing */
    public static Repository load(Resolver resolver) throws IOException {
        Repository repository;

        repository = new Repository();
        repository.loadClasspath(resolver);
        repository.link();
        return repository;
    }

    public static final String METAINF_DESCRIPTOR = "META-INF/jasmin.xml";

    public static final String METAINF_PROPERTIES = "META-INF/wsd.properties";
    public static final String WEBINF_PROPERTIES = "WEB-INF/wsd.properties";

    //--

    /** For webservices; may be null */
    private final Attributes attributes;

    private final List<Module> modules;

    /** using during loading, until() link is called */
    private Map<Module, List<String>> notLinked;
    /** using during loading, until() link is called */
    private List<Node> reloadFiles;

    public Repository() {
        this(new Attributes() {
            @Override
            public Object get(String name) {
                throw new IllegalArgumentException("no such attribute: " + name);
            }
        });
    }

    public Repository(Attributes attributes) {
        if (attributes == null) {
            throw new IllegalArgumentException();
        }
        this.attributes = attributes;
        this.modules = new ArrayList<Module>();
        this.notLinked = new HashMap<Module, List<String>>();
        this.reloadFiles = new ArrayList<Node>();
    }

    public List<Module> modules() {
        return modules;
    }

    public Module lookup(String name) {
        for (Module module : modules) {
            if (name.equals(module.getName())) {
                return module;
            }
        }
        return null;
    }

    /** @return never null */
    public Module get(String name) {
        Module module;

        module = lookup(name);
        if (module == null) {
            throw new IllegalArgumentException("no such module: " + name);
        }
        return module;
    }

    public void add(Module module) {
        String name;

        name = module.getName();
        if (lookup(name) != null) {
            throw new IllegalArgumentException("duplicate module: " + name);
        }
        modules.add(module);
    }

    //--

    public List<String> sequence(String... names) throws CyclicDependency {
        List<Module> moduleList;
        List<String> result;

        moduleList = new ArrayList<Module>();
        for (String name : names) {
            moduleList.add(get(name));
        }
        result = new ArrayList<String>();
        for (Module module : sequence(moduleList)) {
            result.add(module.getName());
        }
        return result;
    }


    // topological sort
    public List<Module> sequence(List<Module> moduleList) throws CyclicDependency {
        List<Module> work;
        Graph<Module> graph;
        Module left;
        Module right;

        graph = new Graph<Module>();
        for (int i = 0; i < moduleList.size(); i++) {
            right = moduleList.get(i);
            graph.addNode(right);
            if (i > 0) { // inject dependency to previous modules
                left = moduleList.get(i - 1);
                graph.addEdge(left, right);
            }
        }
        work = new ArrayList<Module>();
        work.addAll(moduleList);
        for (int i = 0; i < work.size(); i++) { // list grows!
            right = work.get(i);
            for (Module l : right.dependencies()) {
                graph.addEdge(l, right);
                if (!work.contains(l)) {
                    work.add(l);
                }
            }
        }
        return graph.sort();
    }

    //--

    public References resolve(Request request) throws IOException, CyclicDependency {
        List<Module> includes;
        List<Module> excludes;
        References references;
        List<Module> moduleList;
        Node resolved;
        boolean minimize;

        includes = new ArrayList<Module>();
        excludes = new ArrayList<Module>();
        for (String name : Module.SEP.split(request.modules)) {
            if (name.length() == 0) {
                throw new IllegalStateException();
            }
            if (name.charAt(0) == Module.NOT) {
                excludes.add(get(name.substring(1)));
            } else {
                includes.add(get(name));
            }
        }
        moduleList = sequence(includes);
        moduleList.removeAll(sequence(excludes));
        references = new References(request.type, request.minimize);
        for (Module module : moduleList) {
            for (File file : module.resolve(request)) {
                resolved = file.get(request.minimize);
                minimize = request.minimize && file.getMinimized() == null;
                references.add(minimize, resolved);
            }
        }
        return references;
    }

    public List<String> getVariants() {
        List<String> results;
        String variant;

        results = new ArrayList<String>();
        for (Module module : modules) {
            for (File file : module.files()) {
                variant = file.getVariant();
                if (variant != null && !results.contains(variant)) {
                    results.add(variant);
                }
            }
        }
        return results;
    }

    //--

    public void loadClasspath(Resolver resolver) throws IOException {
        Enumeration<URL> e;

        e = getClass().getClassLoader().getResources(METAINF_DESCRIPTOR);
        while (e.hasMoreElements()) {
            loadClasspathItem(resolver, e.nextElement());
        }
    }

    private void loadClasspathItem(Resolver resolver, URL url) throws IOException {
        FileNode classpathItem;
        Node descriptor;
        Node properties;
        Node base;

        classpathItem = resolver.getWorld().locateClasspathItem(url, "/" + METAINF_DESCRIPTOR);
        base = classpathItem.isFile() ? classpathItem.openZip() : classpathItem;
        descriptor = resolver.resolve(base, METAINF_DESCRIPTOR);
        properties = resolver.resolve(base, METAINF_PROPERTIES);
        loadLibrary(resolver, base, descriptor, properties);
    }

    /**
     * An applicationis not a classpath item because jasmin.xml is from WEB-INF, it's not a resource.
     * I don't want to make it a resource (by moving WEB-INF/jasmin.xml to META-INF/jasmin.xml) because
     * all other config files reside in WEB-INF, and webapps usually have no META-INF directory at all.
     */
    public void loadApplication(Resolver resolver, Node base, Node descriptor) throws IOException {
        Node properties;

        properties = resolver.resolve(base, WEBINF_PROPERTIES);
        loadLibrary(resolver, base, descriptor, properties);
    }

    public void loadLibrary(Resolver resolver, Node base, Node descriptor, Node properties) throws IOException {
        Source source;
        Module module;
        Library library;
        File file;

        addReload(descriptor);
        source = Source.load(properties, base);
        library = (Library) Library.TYPE.loadXml(descriptor).get();
        autoFiles(resolver, library, source);
        for (net.sf.beezle.jasmin.descriptor.Module descriptorModule : library.modules()) {
            module = new Module(descriptorModule.getName(), source);
            notLinked.put(module, descriptorModule.dependencies());
            for (Resource resource : descriptorModule.resources()) {
                file = resolver.resolve(source.classpathBase, resource);
                addReload(file);
                resolver.resolve(source.classpathBase, resource);
                module.files().add(file);
            }
            add(module);
        }
    }

    private void addReload(File file) {
        addReload(file.getNormal());
        addReload(file.get(true));
    }

    private void addReload(Node node) {
        if (node instanceof WebdavNode) {
            return;
        }
        if (node instanceof FileNode) {
            // done
        } else if (node instanceof ZipNode) {
            node = node.getWorld().file(((ZipNode) node).getRoot().getZip().getName());
        } else if (node instanceof MemoryNode) {
            // for tests
        } else {
            throw new IllegalStateException("unexpected node: " + node);
        }
        if (!reloadFiles.contains(node)) {
            reloadFiles.add(node);
        }
    }

    private void autoFiles(Resolver life, Library library, Source source) throws IOException {
        autoFiles(life, source, MimeType.JS, library.jss());
        autoFiles(life, source, MimeType.CSS, library.csss());
    }

    private void autoFiles(Resolver resolver, Source source, MimeType type, List<String> includes) throws IOException {
        Filter filter;
        Module module;
        Ref ref;
        File file;
        List<String> depends;
        List<String> calls;
        List<Ref> refs;
        Map<String, List<Ref>> map;
        Ref normal;
        Ref companion;
        Ref minimized;

        filter =  new Filter();
        filter.include(includes);
        map = new HashMap<String, List<Ref>>();
        for (Node node : source.classpathBase.find(filter)) {
            ref = Ref.create(node, source);
            refs = map.get(ref.module);
            if (refs == null) {
                refs = new ArrayList<Ref>();
                map.put(ref.module, refs);
            }
            refs.add(ref);
        }
        for (Map.Entry<String, List<Ref>> entry : map.entrySet()) {
            module = lookup(entry.getKey());
            if (module == null) {
                module = new Module(entry.getKey(), source);
                modules.add(module);
            } else {
                if (module.getSource() != source) {
                    throw new IllegalStateException();
                }
            }
            refs = entry.getValue();
            while (!refs.isEmpty()) {
                normal = refs.remove(0);
                companion = removeCompanionOpt(refs, normal);
                if (companion == null) {
                    if (normal.minimized) {
                        minimized = normal;
                    } else {
                        minimized = null;
                    }
                } else {
                    if (normal.minimized) {
                        minimized = normal;
                        normal = companion;
                    } else {
                        minimized = companion;
                    }
                }
                file = resolver.resolve(source.classpathBase, new Resource(type, Base.CLASSPATH, normal.path,
                        minimized != null ? minimized.path : null, normal.variant));
                try {
                    depends = notLinked.get(module);
                    if (depends == null) {
                        depends = new ArrayList<String>();
                        notLinked.put(module, depends);
                    }
                    calls = new ArrayList<String>();
                    Parser.parseComment(file.getNormal().readString(), depends, calls);
                } catch (IOException e) {
                    throw new IOException(normal.node.getURI() + ": " + e.getMessage(), e);
                }
                if (calls.size() > 0) {
                    if (file.getMinimized() != null) {
                        throw new UnsupportedOperationException(file.getNormal().getURI().toString());
                    }
                    Node tmp = resolver.getWorld().memoryNode();
                    OutputStream dest = tmp.createOutputStream();
                    for (String webservice : calls) {
                        Call.call(attributes, webservice, dest);
                        dest.write('\n');
                    }
                    InputStream orig = file.getNormal().createInputStream();
                    resolver.getWorld().getBuffer().copy(orig, dest);
                    orig.close();
                    dest.close();
                    file = new File(tmp, null, file.getType(), file.getVariant());
                }
                module.files().add(file);
                addReload(file);
            }
        }
    }

    //--

    // companion: Ref with same module and variant, but different minimization
    private static Ref removeCompanionOpt(List<Ref> refs, Ref cmp) {
        Iterator<Ref> iter;
        Ref ref;

        iter = refs.iterator();
        while (iter.hasNext()) {
            ref = iter.next();
            if (ref.module.equals(cmp.module) && Util.eq(ref.variant, cmp.variant)) {
                iter.remove();
                return ref;
            }
        }
        return null;
    }

    private static class Ref {
        private static final String MIN = "-min";
        public static Ref create(Node node, Source source) {
            String path;
            String module;
            int idx;
            boolean minimized;
            String variant;

            path = node.getRelative(source.classpathBase);
            module = path;
            module = Strings.removeLeftOpt(module, "script/");
            module = Strings.removeLeftOpt(module, "style/");
            idx = module.lastIndexOf('.');
            if (idx != -1) {
                module = module.substring(0, idx);
            }
            minimized = module.endsWith(MIN);
            if (minimized) {
                module = module.substring(0, module.length() - MIN.length());
            }
            idx = module.indexOf(':');
            if (idx != -1) {
                variant = module.substring(idx + 1);
                module = module.substring(0, idx);
            } else {
                variant = null;
            }
            module = source.artifactId + "-" + module.replace('/', '-');
            return new Ref(node, path, module, variant, minimized);
        }

        public final Node node;
        public final String path;
        public final String module;
        public final String variant;
        public final boolean minimized;

        public Ref(Node node, String path, String module, String variant, boolean minimized) {
            this.node = node;
            this.path = path;
            this.module = module;
            this.variant = variant;
            this.minimized = minimized;
        }
    }

    //--

    /**
     * Call this after you've loaded all libraries
     * @return reload files
     */
    public List<Node> link() {
        List<Module> dependencies;
        Module module;
        Module resolved;
        StringBuilder problems;
        List<Node> result;

        problems = new StringBuilder();
        for (Map.Entry<Module, List<String>> entry : notLinked.entrySet()) {
            module = entry.getKey();
            dependencies = module.dependencies();
            for (String name : entry.getValue()) {
                resolved = lookup(name);
                if (resolved == null) {
                    problems.append("module '" + module.getName() + "': cannot resolve dependency '" + name + "'\n");
                } else {
                    dependencies.add(resolved);
                }
            }
        }
        if (problems.length() > 0) {
            throw new IllegalArgumentException(problems.toString());
        }
        result = reloadFiles;
        notLinked = null;
        reloadFiles = null;
        return result;
    }
}
