package net.sf.beezle.jasmin.model;

public class Content {
    public final String mimeType;
    public final long lastModified;
    public final byte[] bytes;

    public Content(String mimeType, long lastModified, byte[] bytes) {
        this.mimeType = mimeType;
        this.lastModified = lastModified;
        this.bytes = bytes;
    }
}
