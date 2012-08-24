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
package net.sf.beezle.jasmin.main;

import net.sf.beezle.jasmin.model.Engine;

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
            str = engine.process(path);
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
