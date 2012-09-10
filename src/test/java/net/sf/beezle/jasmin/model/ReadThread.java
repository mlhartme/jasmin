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
/**
 *
 */
package net.sf.beezle.jasmin.model;

import java.util.Arrays;

public class ReadThread extends Thread {
    private final References ref;
    private final byte[] expected;
    private final int repeat;
    private Exception exception;

    public ReadThread(References ref, byte[] expected, int repeat) {
        this.ref = ref;
        this.expected = expected;
        this.repeat = repeat;
        this.exception = null;
    }

    @Override
    public void run() {
        byte[] bytes;

        for (int j = 0; j < repeat; j++) {
            try {
                bytes = ref.readBytes();
                if (!Arrays.equals(expected, bytes)) {
                    throw new IllegalStateException(toString(expected) + " vs " +  toString(bytes));
                }
            } catch (Exception e) {
                exception = e;
                return;
            }
        }
    }

    public void finish() throws Exception {
        join();
        if (exception != null) {
            throw exception;
        }
    }

    private static String toString(byte[] bytes) {
        StringBuilder builder;

        builder = new StringBuilder();
        for (byte b : bytes) {
            if (b >= ' ' && b <= 'z') {
                builder.append((char) b);
            } else {
                builder.append('[').append(b).append(']');
            }
        }
        return builder.toString();
    }
}
