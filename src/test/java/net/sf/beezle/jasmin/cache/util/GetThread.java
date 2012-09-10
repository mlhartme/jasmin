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
package net.sf.beezle.jasmin.cache.util;

import net.sf.beezle.jasmin.cache.Cache;

import java.util.Random;

public class GetThread extends Thread {
    private final Cache<String, String> cache;
    private final int repeat;
    private final String[] keys;
    private final Random random;
    private Exception exception;

    public GetThread(Cache<String, String> cache, int repeat, String[] keys) {
        this.cache = cache;
        this.repeat = repeat;
        this.keys = keys;
        this.random = new Random();
        this.exception = null;
    }

    @Override
    public void run() {
        String key;

        for (int j = 0; j < repeat; j++) {
            try {
                key = keys[random.nextInt(keys.length)];
                if (random.nextBoolean()) {
                    cache.probe(key);
                } else {
                    throw new RuntimeException("TODO: cache.get(key);");
                }
            } catch (Exception e) {
                exception = e;
                return;
            }
        }
    }

    public void finish() throws Exception {
        join();
        if (exception != null) {
            throw exception;
        }
    }
}
