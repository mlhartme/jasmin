package net.sf.beezle.jasmin.model;

import net.sf.beezle.jasmin.cache.Cache;

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
