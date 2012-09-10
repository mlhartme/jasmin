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
