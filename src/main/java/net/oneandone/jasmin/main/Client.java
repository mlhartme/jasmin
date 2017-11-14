/*
 * Copyright 1&1 Internet AG, https://github.com/1and1/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 *
 */
package net.oneandone.jasmin.main;

import net.oneandone.jasmin.model.Engine;

import java.io.IOException;
import java.util.List;

/** Helper class for Runner */
public class Client extends Thread {
    private final Engine engine;
    private final List<String> paths;
    private final int requests;
    private final boolean random;
    private final boolean lastModified;
    private Exception exception;

    public Client(Engine engine, List<String> paths, int requests, boolean random, boolean lastModified) {
        this.engine = engine;
        this.paths = paths;
        this.requests = requests;
        this.random = random;
        this.lastModified = lastModified;
        this.exception = null;
    }

    @Override
    public void run() {
        try {
            doRun();
        } catch (Exception e) {
            exception = e;
        }
    }
    public void doRun() throws IOException {
        String path;

        for (int i = 0; i < requests; i++) {
            if (random) {
                path = paths.get(Runner.RANDOM.nextInt(paths.size()));
            } else {
                path = paths.get(i % paths.size());
            }
            if (lastModified && Runner.RANDOM.nextBoolean()) {
                engine.getLastModified(path);
            } else {
                get(path);
            }
        }
    }

    public void get(String path) throws IOException {
        long started;
        String str;

        started = System.currentTimeMillis();
        try {
            str = engine.request(path);
            Runner.LOG.info(path + ": " + str.length() + " chars, " + (System.currentTimeMillis() - started) + " ms");
        } catch (IOException e) {
            Runner.LOG.error(path, e);
            throw e;
        }
    }

    public void finish() throws Exception {
        join();
        if (exception != null) {
            throw exception;
        }
    }
}
