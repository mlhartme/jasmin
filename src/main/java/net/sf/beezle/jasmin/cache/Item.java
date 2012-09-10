/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
