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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class Cache<K, V> {
    protected final LinkedHashMap<K, Item<V>> items;
    protected final int maxSize;

    /** cumulated valueSize of all items. */
    private int size;
    private int lookups;
    private int misses;
    private long addedBytes;
    private long removedBytes;

    public Cache(int maxSize) {
        this.items = new LinkedHashMap<>(16, 0.75f, true);
        this.maxSize = maxSize;
        this.size = 0;
        this.lookups = 0;
        this.misses = 0;
    }

    public int getMaxSize() {
        return maxSize;
    }

    //--

    public abstract int valueSize(V value);

    //--

    public synchronized V lookup(K key) {
        Item<V> item;

        lookups++;
        item = items.get(key);
        if (item != null) {
            item.accessTime = System.currentTimeMillis();
            item.accessCount++;
            return item.value;
        }
        misses++;
        return null;
    }

    public synchronized void add(K key, V value, long created, long duration) {
        int valueSize;
        Item<V> item;
        Item<V> concurrent;

        item = new Item<>(value, created, duration);
        concurrent = items.put(key, item);
        if (concurrent != null) {
            size -= valueSize(concurrent.value);
        }
        valueSize = valueSize(item.value);
        size += valueSize;
        addedBytes += valueSize;
        item.accessTime = created;
        item.accessCount++;
        shrink(maxSize);
    }

    /** lookup without stats */
    public synchronized V probe(K key) {
        Item<V> item;

        item = items.get(key);
        return item == null ? null : item.value;
    }

    public synchronized void resize(int max) {
        shrink(max);
    }

    private void shrink(int max) {
        Iterator<Map.Entry<K, Item<V>>> iter;
        Item<V> item;
        int valueSize;

        if (size > max) {
            iter = items.entrySet().iterator();
            while (iter.hasNext()) {
                item = iter.next().getValue();
                valueSize = valueSize(item.value);
                removedBytes += valueSize;
                size -= valueSize;
                iter.remove();
                if (size <= max) {
                    break;
                }
            }
            if (size < 0) {
                throw new IllegalStateException();
            }
            if (items.size() == 0 && size != 0) {
                throw new IllegalStateException();
            }
        }
    }

    //--

    public synchronized int items() {
        return items.size();
    }

    public synchronized int size() {
        return size;
    }

    public synchronized int misses() {
        return misses;
    }

    public synchronized int gets() {
        return lookups;
    }

    public synchronized long addedBytes() {
        return addedBytes;
    }

    public synchronized long removedBytes() {
        return removedBytes;
    }

    public synchronized void validate() {
        int s;

        s = 0;
        for (Item<V> item : items.values()) {
            s += valueSize(item.value);
        }
        if (s != size) {
            throw new IllegalStateException(s + " != " + size);
        }
    }

    //--

    @Override
    public synchronized String toString() {
        StringBuilder builder;
        int percent;
        int count;
        Item<V> item;

        builder = new StringBuilder();
        builder.append("size: ").append(maxSize).append(" (").append(maxSize == 0 ? 100 : (size * 100 / maxSize)).append("% used)\n");
        count = gets();
        if (count == 0) {
            percent = 0;
        } else {
            percent = (count - misses) * 100 / count;
        }
        builder.append("lookups: ").append(count).append(" (").append(percent).append("% hits)\n");
        for (Map.Entry<K, Item<V>> entry : items.entrySet()) {
            item = entry.getValue();
            entryToString(entry.getKey(), item.value, builder);
            builder.append(" - ").append(item.stats()).append("\n");
        }
        return builder.toString();
    }

    protected abstract void entryToString(K key, V value, StringBuilder builder);
}
