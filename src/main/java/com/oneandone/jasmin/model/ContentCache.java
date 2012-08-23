package com.oneandone.jasmin.model;

import com.oneandone.jasmin.cache.Cache;

/** Maps hash to content. */
public class ContentCache extends Cache<String, Content> {
    public ContentCache(int maxSize) {
        super(maxSize);
    }

    @Override
    public int valueSize(Content content) {
        return content.bytes.length;
    }

    @Override
    public String valueToString(Content content) {
        return content.bytes.length + " bytes";
    }
}
