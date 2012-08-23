package com.oneandone.jasmin.main;

import com.oneandone.jasmin.descriptor.Base;
import com.oneandone.jasmin.model.Attributes;
import com.oneandone.jasmin.model.Engine;
import com.oneandone.jasmin.model.Repository;
import com.oneandone.jasmin.model.Resolver;
import com.oneandone.sales.unifiedlogging.ExplicitContext;
import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.fs.World;
import net.sf.beezle.sushi.fs.file.FileNode;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.util.List;

/** Application configuration, factory for engines. */
public class Application {
    public static Application load(World world, ServletConfig config, FileNode docroot) throws Exception {
        String str;
        Node applicationDescriptor;
        boolean autoReload;
        String siteId;
        Integer expires;
        int pathCache;
        int referenceCache;
        Resolver resolver;

        str = getString(config, "project", null);
        if (str != null) {
            throw new IOException("'project' parameter no longer supported");
        }
        autoReload = !"prod".equals(config.getServletContext().getInitParameter("mode"));
        autoReload = getBoolean(config, "autoReload", autoReload);
        resolver = new Resolver(world, autoReload);
        applicationDescriptor = getApplicationDescriptor(config, docroot, resolver);
        siteId = getString(config, "siteId", docroot.getName());
        expires = getInteger(config, "expires", Application.MANY_YEARS);
        return new Application(config.getServletContext(), resolver, siteId, applicationDescriptor, expires);
    }

    public static FileNode file(World world, String str) throws IOException {
        if (str.endsWith("/")) {
            str = str.substring(0, str.length() - 1);
        }
        return world.file(new java.io.File(str).getCanonicalFile());
    }

    private static Node getApplicationDescriptor(ServletConfig config, FileNode application, Resolver resolver) throws Exception {
        String explicit;
        Node result;
        Node resolvedProject;

        explicit = getString(config, "projectDescriptor", null);
        if (explicit != null) {
            throw new IOException("'projectDescriptor' parameter no longer supported");
        }
        result = application.join("WEB-INF/jasmin.xml");
        if (!result.exists()) {
            return null;
        }
        resolvedProject = resolver.resolve(application, result.getRelative(application));
        if (resolvedProject != null) {
            result = resolvedProject;
        }
        return result;
    }

    //--

    private final ServletContext context;
    public final Resolver resolver;

    /** may be null. */
    public final Node applicationDescriptor;

    public final ExplicitContext loggingContext;

    /**
     * Response expires after the number of seconds specified here. Also used for Cache-Control: max-age.
     * Null to disable expires header.
     * See http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
     */
    public final Integer expires;

    public static final int MANY_YEARS = 60 * 60 * 24 * 365 * 15;

    public Application(ServletContext context, Resolver resolver, String siteId, Node applicationDescriptor, Integer expires) {
        this.context = context;
        this.resolver = resolver;
        this.applicationDescriptor = applicationDescriptor;
        this.loggingContext = new ExplicitContext(siteId, "novisit", "noucuoId");
        this.expires = expires;
    }

    public String getName() {
        return loggingContext.getSiteId();
    }

    public Engine createEngineSimple(Node docroot, Node localhost) throws IOException {
        return (Engine) createEngine(docroot, localhost)[0];
    }

    /** @return (engine, reloadFiles) */
    public Object[] createEngine(Node docroot, Node localhost) throws IOException {
        Repository repository;
        List<Node> reloadFiles;

        resolver.add(Base.DOCROOT, docroot);
        resolver.add(Base.LOCALHOST, localhost);
        try {
            repository = new Repository(new Attributes() {
                @Override
                public Object get(String name) {
                    return context.getAttribute(name);
                }
            });
            repository.loadClasspath(resolver);
            if (applicationDescriptor != null) {
                repository.loadApplication(resolver, docroot, applicationDescriptor);
            }
            reloadFiles = repository.link();
            return new Object[] { new Engine(repository), reloadFiles };
        } catch (Throwable e) {
            resolver.reset();
            if (e instanceof IOException) {
                throw (IOException) e;
            } else if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else if (e instanceof Error) {
                throw (Error) e;
            } else {
                throw new IllegalStateException(e);
            }
        }
    }

    //--

    private static Boolean getBoolean(ServletConfig config, String key, boolean dflt) {
        String value;

        value = getString(config, key, null);
        if (value == null) {
            return dflt;
        }
        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        } else {
            throw new IllegalArgumentException(key + ": 'true' or 'false' expected, got '" + value + "'");
        }
    }

    private static Integer getInteger(ServletConfig config, String key, Integer dflt) {
        String str;

        str = getString(config, key, null);
        return str == null ? dflt : Integer.valueOf(str);
    }

    private static String getString(ServletConfig config, String key, String dflt) {
        String result;

        result = config.getInitParameter(key);
        return result == null ? dflt : result;
    }

}
