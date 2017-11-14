/*
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
