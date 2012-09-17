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

import net.oneandone.sushi.fs.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleTest {
    @Test
    public void variantTree() throws Exception {
        Repository repository;
        Module group;

        repository = Repository.load(new Resolver(new World()));
        group = repository.get("variant.tree");
        assertEquals("lead", group.getBest(MimeType.JS, "bar"));
        assertEquals("foo", group.getBest(MimeType.JS, "foo"));
        assertEquals("lead", group.getBest(MimeType.JS, "foox"));
        assertEquals("foo:bar", group.getBest(MimeType.JS, "foo:bar"));
        assertEquals("foo", group.getBest(MimeType.JS, "foo:baz"));
        assertEquals("foo:bar", group.getBest(MimeType.JS, "foo:bar:bau"));
        assertEquals("foo:bar:baz", group.getBest(MimeType.JS, "foo:bar:baz"));
    }
}
