/**
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
package net.oneandone.jasmin.model;

import net.oneandone.sushi.fs.World;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ConcurrentEngineTest {
    private static final World WORLD = new World();

    private static Repository repository() throws IOException {
        Module module;
        Repository repository;

        repository = new Repository();

        module = new Module("foo", new Source(null, "g", "foo", "1", "scm"));
        module.files().add(new File(WORLD.resource("multiuser/foo.css"), null, MimeType.CSS, null));
        repository.add(module);

        module = new Module("bar", new Source(null, "g", "bar", "1", "scm"));
        module.files().add(new File(WORLD.resource("multiuser/bar.css"), null, MimeType.CSS, null));
        repository.add(module);
        return repository;
    }

    @Test
    public void cachehits() throws Throwable {
        Engine engine;

        engine = new Engine(repository(), 15, 100, 100);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    @Test
    public void trashing() throws Throwable {
        Engine engine;

        engine = new Engine(repository(), 15, 0, 0);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    @Test
    public void trashingWithOneProcessor() throws Throwable {
        Engine engine;

        engine = new Engine(repository(), 1, 0, 0);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    private void parallel(Engine engine, int threadCount, int repeat) throws Throwable {
        ProcessThread[] threads;
        int idx;

        threads = new ProcessThread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            idx  = i % engine.repository.modules().size();
            threads[i] = new ProcessThread(engine, repeat, "css/" + engine.repository.modules().get(idx).getName());
            threads[i].start();
        }
        for (ProcessThread thread : threads) {
            thread.finish();
        }
        assertEquals(0, engine.load());
    }

    private static class ProcessThread extends Thread {
        private final Engine engine;
        private final String path;
        private final int repeat;
        private Throwable exception;

        public ProcessThread(Engine engine, int repeat, String path) {
            this.engine = engine;
            this.path = path;
            this.repeat = repeat;
            this.exception = null;
        }

        @Override
        public void run() {
            String expected;
            String actual;

            expected = null;
            for (int j = 0; j < repeat; j++) {
                try {
                    actual = engine.request(path);
                    if (expected == null) {
                        expected = actual;
                    } else {
                        assertEquals(expected, actual);
                    }
                } catch (Throwable e) {
                    exception = e;
                    return;
                }
            }
        }

        public void finish() throws Throwable {
            join();
            if (exception != null) {
                throw exception;
            }
        }
    }
}
