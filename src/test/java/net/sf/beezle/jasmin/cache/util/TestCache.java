package net.sf.beezle.jasmin.cache.util;

import net.sf.beezle.jasmin.cache.Cache;

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
