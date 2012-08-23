package com.oneandone.jasmin.model;

import com.oneandone.jasmin.cache.Cache;

/** Maps paths to hashes. */
public class HashCache extends Cache<String, String> {
    public HashCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public int valueSize(String hash) {
        return hash.length();
    }
}
