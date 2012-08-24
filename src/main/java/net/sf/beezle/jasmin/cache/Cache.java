package net.sf.beezle.jasmin.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public abstract class Cache<K, V> {
    private final LinkedHashMap<K, Item<V>> items;
    private final int maxSize;

    /** cumulated valueSize of all items. */
    private int size;
    private int lookups;
    private int misses;

    public Cache(int maxSize) {
        this.items = new LinkedHashMap<K, Item<V>>(16, 0.75f, true);
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
    public String valueToString(V value) {
        return value.toString();
    }

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
        Item<V> item;
        Item<V> concurrent;

        item = new Item<V>(value, created, duration);
        concurrent = items.put(key, item);
        if (concurrent != null) {
            size -= valueSize(concurrent.value);
        }
        size += valueSize(item.value);
        item.accessTime = created;
        item.accessCount++;
        doResize(maxSize);
    }

    /** lookup without stats */
    public synchronized V probe(K key) {
        Item<V> item;

        item = items.get(key);
        return item == null ? null : item.value;
    }

    public synchronized void resize(int max) {
        doResize(max);
    }

    private void doResize(int max) {
        Iterator<Map.Entry<K, Item<V>>> iter;
        Item<V> item;

        if (size > max) {
            iter = items.entrySet().iterator();
            while (iter.hasNext()) {
                item = iter.next().getValue();
                size -= valueSize(item.value);
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
            builder.append(entry.getKey().toString()).append(": ").append(toString(entry.getValue())).append("\n");
        }
        return builder.toString();
    }

    private String toString(Item<V> item) {
        return item.toString() + ": " + valueToString(item.value);
    }
}
