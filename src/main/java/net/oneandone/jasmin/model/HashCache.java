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
package net.oneandone.jasmin.model;

import net.oneandone.jasmin.cache.Cache;

/** Maps paths to hashes. */
public class HashCache extends Cache<String, String> {
    public HashCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public int valueSize(String hash) {
        return hash.length();
    }

    @Override
    protected void entryToString(String key, String value, StringBuilder builder) {
        builder.append(value).append(" <- ").append(key);
    }

}
