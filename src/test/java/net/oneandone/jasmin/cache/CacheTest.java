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
package net.oneandone.jasmin.cache;

import net.oneandone.jasmin.cache.util.TestCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public class CacheTest {
    private TestCache cache;

    @Before
    public void before() {
        cache = new TestCache(500);
    }

    @After
    public void after() {
        cache.validate();
        cache.resize(0);
    }

    @Test
    public void get() throws IOException {
        String key = "foo";
        String value;

        assertNull(cache.probe(key));
        value = cache.get(key);
        assertSame(key, value);
        assertSame(key, cache.probe(key));
        assertEquals(1, cache.items());
        assertEquals(3, cache.size());
    }

    @Test
    public void free() throws IOException {
        String key = "foo";

        cache.get(key);
        assertEquals(1, cache.items());
        cache.resize(4);
        assertEquals(1, cache.items());
        cache.resize(3);
        assertEquals(1, cache.items());
        cache.resize(2);
        assertEquals(0, cache.items());
        assertEquals(0, cache.size());
        assertNull(cache.probe(key));
    }

    @Test
    public void ordering() throws IOException {
        cache.get("foo");
        cache.get("bar");
        cache.resize(3);
        assertEquals(1, cache.items());
        assertNull(cache.probe("foo"));
        assertNotNull(cache.probe("bar"));
    }

    @Test
    public void reordering() throws IOException {
        cache.get("foo");
        cache.get("bar");
        cache.get("foo");
        cache.resize(3);
        assertEquals(1, cache.items());
        assertNull(cache.probe("bar"));
        assertNotNull(cache.probe("foo"));
    }

    @Test
    public void stats() throws IOException {
        String key = "foo";

        assertEquals(0, cache.gets());
        assertEquals(0, cache.misses());
        cache.get(key);
        assertEquals(1, cache.gets());
        assertEquals(1, cache.misses());
        cache.get(key);
        assertEquals(2, cache.gets());
        assertEquals(1, cache.misses());
        cache.resize(0);
        assertEquals(2, cache.gets());
        assertEquals(1, cache.misses());
        cache.get(key);
        assertEquals(3, cache.gets());
        assertEquals(2, cache.misses());
        cache.get(key);
        assertEquals(4, cache.gets());
        assertEquals(2, cache.misses());
    }
}
