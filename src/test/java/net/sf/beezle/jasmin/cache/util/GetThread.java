package net.sf.beezle.jasmin.cache.util;

import net.sf.beezle.jasmin.cache.Cache;

import java.util.Random;

public class GetThread extends Thread {
    private final Cache<String, String> cache;
    private final int repeat;
    private final String[] keys;
    private final Random random;
    private Exception exception;

    public GetThread(Cache<String, String> cache, int repeat, String[] keys) {
        this.cache = cache;
        this.repeat = repeat;
        this.keys = keys;
        this.random = new Random();
        this.exception = null;
    }

    @Override
    public void run() {
        String key;

        for (int j = 0; j < repeat; j++) {
            try {
                key = keys[random.nextInt(keys.length)];
                if (random.nextBoolean()) {
                    cache.probe(key);
                } else {
                    throw new RuntimeException("TODO: cache.get(key);");
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
}
