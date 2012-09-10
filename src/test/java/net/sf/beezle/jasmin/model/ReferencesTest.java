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
import net.sf.beezle.sushi.fs.World;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReferencesTest {
    private static final String UMLAUTE = "\"\u00f6\u00d6\u00fc\";"; // öÖü

    private World world = new World();

    @Test
    public void jsNormal() throws Exception {
        assertEquals("a;b;", js("a; b;"));
    }

    @Test
    public void jsBroken() throws Exception {
        try {
            js("a=\"");
            fail();
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("line 1: unterminated string literal"));
        }
    }

    @Test
    public void umlaut() throws Exception {
        assertEquals(UMLAUTE, js(UMLAUTE));
    }

    //--

    @Test
    public void cssNormal() throws Exception {
        assertEquals("#sant .notes{font-size:11px;}", css("#sant .notes {\n\n        font-size: 11px;    \n\n}"));
    }

    @Test(expected = IOException.class)
    public void cssBroken() throws Exception {
        css("a={");
    }

    @Test
    public void sass() throws Exception {
        assertEquals("\n.dot{color:\"abc\";}",
                css("$blue: \"abc\";", ".dot {\n"
                        + "  color: $blue\n"
                        + "}\n")
        );
    }

    //--

    private String js(String ... sources) throws IOException {
        return minimize(MimeType.JS, sources);
    }

    private String css(String ... sources) throws IOException {
        return minimize(MimeType.CSS, sources);
    }

    private String minimize(MimeType type, String ... sources) throws IOException {
        Node dest;
        Writer writer;
        References references;

        dest = world.memoryNode();
        writer = dest.createWriter();
        references = new References(type, true);
        for (String source : sources) {
            references.add(true, world.memoryNode(source));
        }
        references.writeTo(writer);
        writer.close();
        return dest.readString();
    }
}
