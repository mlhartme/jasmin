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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConcurrentEngineTest {
    @Test
    public void cachehits() throws Exception {
        Repository repository;
        Engine engine;

        repository = new Repository();
        repository.add(new Module("foo", new Source(null, "g", "a", "1", "scm")));
        engine = new Engine(repository, 15, 100, 100);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    @Test
    public void trashing() throws Exception {
        Repository repository;
        Engine engine;

        repository = new Repository();
        repository.add(new Module("foo", new Source(null, "g", "a", "1", "scm")));
        engine = new Engine(repository, 15, 0, 0);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    @Test
    public void trashingWithOneProcessor() throws Exception {
        Repository repository;
        Engine engine;

        repository = new Repository();
        repository.add(new Module("foo", new Source(null, "g", "a", "1", "scm")));
        engine = new Engine(repository, 1, 0, 0);
        parallel(engine, 2, 1000);
        parallel(engine, 3, 997);
        parallel(engine, 7, 997);
        parallel(engine, 13, 997);
    }

    private void parallel(Engine engine, int threadCount, int repeat) throws Exception {
        ProcessThread[] threads;

        threads = new ProcessThread[threadCount];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ProcessThread(engine, repeat);
            threads[i].start();
        }
        for (ProcessThread thread : threads) {
            thread.finish();
        }
        assertEquals(0, engine.load());
    }

    private static class ProcessThread extends Thread {
        private final Engine engine;
        private final int repeat;
        private Exception exception;

        public ProcessThread(Engine engine, int repeat) {
            this.engine = engine;
            this.repeat = repeat;
            this.exception = null;
        }

        @Override
        public void run() {
            for (int j = 0; j < repeat; j++) {
                try {
                    assertEquals("", engine.process("js/foo"));
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
}
