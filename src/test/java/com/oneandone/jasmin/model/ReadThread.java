/**
 *
 */
package com.oneandone.jasmin.model;

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
