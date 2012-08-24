package net.sf.beezle.jasmin.model;

import net.sf.beezle.jasmin.cache.Cache;

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
