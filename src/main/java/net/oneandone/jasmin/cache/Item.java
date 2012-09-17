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
package net.oneandone.jasmin.cache;

import java.text.SimpleDateFormat;

public class Item<T> {
    /** never null. */
    public final T value;

    public final long createTime;
    public final long duration;

    public long accessTime;
    public int accessCount;

    public Item(T value, long createTime, long duration) {
        if (value == null) {
            throw new IllegalArgumentException();
        }
        this.value = value;
        this.createTime = createTime;
        this.duration = duration;
        this.accessTime = 0;
        this.accessCount = 0;
    }

    public static final SimpleDateFormat FORMATTER = new SimpleDateFormat("dd.MM.yy HH:mm:ss");

    /** does *not* include the value. */
    @Override
    public String toString() {
        return accessCount + " (" + FORMATTER.format(accessTime) + "), " + duration + " ms";
    }
}
