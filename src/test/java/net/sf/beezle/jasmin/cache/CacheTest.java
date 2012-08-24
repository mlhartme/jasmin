package net.sf.beezle.jasmin.cache;

import net.sf.beezle.jasmin.cache.util.TestCache;
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
