package com.oneandone.jasmin.cache;

import com.oneandone.jasmin.cache.util.GetThread;
import com.oneandone.jasmin.cache.util.TestCache;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentCacheFullTest {
    private Cache<String, String> cache;

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
    public void normal() throws Exception {
        cache = new TestCache(0);
        parallel(2, 10000000, "foo");
        System.out.println(cache.toString());
        cache = new TestCache(6);
        parallel(2, 10000000, "foo", "bar", "baz", "bum");
        System.out.println(cache.toString());
        cache = new TestCache(7);
        parallel(7, 10000000, "foo", "1", "toolongforcache", "abcd", "efg", "hij", "klmn", "o", "pq");
        System.out.println(cache.toString());
    }

    private void parallel(int count, int repeat, String... keys) throws Exception {
        GetThread[] threads;

        threads = new GetThread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new GetThread(cache, repeat, keys);
            threads[i].start();
        }
        for (GetThread thread : threads) {
            thread.finish();
        }
    }
}
