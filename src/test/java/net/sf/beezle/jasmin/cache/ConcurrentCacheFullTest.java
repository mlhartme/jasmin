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
package net.sf.beezle.jasmin.cache;

import net.sf.beezle.jasmin.cache.util.GetThread;
import net.sf.beezle.jasmin.cache.util.TestCache;
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
