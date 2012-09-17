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
package net.oneandone.jasmin.cache.util;

import net.oneandone.jasmin.cache.Cache;

import java.io.IOException;

public class TestCache extends Cache<String, String> {
    public TestCache(int maxSize) {
        super(maxSize);
    }

    public String compute(String key) {
        for (int i = 0; i < 100; i++) {
            // some busy wait ... no-op:
            i = i * 1;
        }
        return key;
    }

    public String get(String key) throws IOException {
        String value;
        long started;

        value = lookup(key);
        if (value != null) {
            return value;
        }
        started = System.currentTimeMillis();
        value = compute(key);
        add(key, value, started, System.currentTimeMillis() - started);
        return value;
    }

    @Override
    public int valueSize(String value) {
        return value.length();
    }
}
