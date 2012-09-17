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

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserTest {
    @Test
    public void dependencies() {
        deps("/* jasmin */");
        deps("/* jasmin depend = foo */", "foo");
        deps("/* jasmin \n"
             + "     depend = foo\n"
             + "     depend=bar */\n", "foo", "bar");
    }

    private void deps(String str, String ... expected) {
        List<String> depends;
        List<String> webservices;

        depends = new ArrayList<String>();
        webservices = new ArrayList<String>();
        try {
            Parser.parseComment(str, depends, webservices);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals(Arrays.asList(expected), depends);
    }
}
