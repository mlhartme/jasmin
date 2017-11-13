/**
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.oneandone.jasmin.model;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.Writer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReferencesTest {
    private static final String UMLAUTE = "\"\u00f6\u00d6\u00fc\";"; // öÖü

    private World world;

    @Before
    public void world() throws IOException {
        world = World.create();
    }

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
            assertTrue(e.getMessage().contains("1:"));
            assertTrue(e.getMessage(), e.getMessage().toLowerCase().contains("unterminated string literal"));
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

    private String js(String... sources) throws IOException {
        return minimize(MimeType.JS, sources);
    }

    private String css(String... sources) throws IOException {
        return minimize(MimeType.CSS, sources);
    }

    private String minimize(MimeType type, String... sources) throws IOException {
        Node dest;
        References references;

        dest = world.memoryNode();
        try (Writer writer = dest.newWriter()) {
            references = new References(type, true);
            for (String source : sources) {
                references.add(true, false, world.memoryNode(source));
            }
            references.writeTo(writer);
        }
        return dest.readString();
    }
}
