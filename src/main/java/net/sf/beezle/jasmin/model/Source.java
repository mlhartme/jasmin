package net.sf.beezle.jasmin.model;

import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.util.Strings;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

/** Per-classpath item info. */
public class Source {
    private static final Logger LOG = Logger.getLogger(Source.class);

    public static Source load(Node properties, Node base) throws IOException {
        Reader src;
        Properties p;
        String groupId;
        String artifactId;
        String version;
        String scm;

        properties.checkExists();
        src = properties.createReader();
        p = new Properties();
        p.load(src);
        src.close();
        groupId = get(p, "groupId");
        artifactId = get(p, "artifactId");
        version = get(p, "version");
        scm = get(p, "scmConnection");
        return new Source(base, groupId, artifactId, version, scm);
    }

    private static String get(Properties p, String key) {
        String value;

        value = p.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("unknown key: " + key);
        }
        return value;
    }

    /** root node searched for files. */
    public final Node classpathBase;

    public final String groupId;
    public final String artifactId;
    public final String version;
    public final String scm;

    public Source(Node classpathBase, String groupId, String artifactId, String version, String scm) {
        this.classpathBase = classpathBase;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scm = scm;
    }
}
