package net.sf.beezle.jasmin.cache;

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
