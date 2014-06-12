/**
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
package net.oneandone.jasmin.cache.util;

import net.oneandone.jasmin.cache.Cache;

import java.io.IOException;

public class TestCache extends Cache<String, String> {
    public TestCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public int valueSize(String value) {
        return value.length();
    }

    @Override
    protected void entryToString(String key, String value, StringBuilder builder) {
        builder.append(key).append(": ").append(value);
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
}
