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

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;
import org.junit.Test;

public class ConcurrentReferenceManualTest {
    private static final World WORLD = new World();
    private References reference;

    @Test
    public void normal() throws Exception {
        single("");
        single("abc");
        single("01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n"
            + "01234567890123456789012345678901234567890123456789012345678901234567890123456789\n");
    }

    private void single(String str) throws Exception {
        check(MimeType.CSS, false, str);
        check(MimeType.CSS, true, str);
        check(MimeType.JS, false, str);
        check(MimeType.JS, true, str);
    }

    private void check(MimeType type, boolean minimize, String str) throws Exception {
        doCheck(type, minimize, WORLD.getTemp().createTempFile(), str);
        doCheck(type, minimize, WORLD.node("dav://localhost/webdav/foo.txt"), str);
    }

    private void doCheck(MimeType type, boolean minimize, Node node, String str) throws Exception {
        node.writeString(str);
        reference = References.create(type, minimize, node);
        parallel(5, 369, reference.readBytes());
        node.deleteFile();
    }

    private void parallel(int count, int repeat, byte[] expected) throws Exception {
        ReadThread[] threads;

        threads = new ReadThread[count];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new ReadThread(reference, expected, repeat);
            threads[i].start();
        }
        for (ReadThread thread : threads) {
            thread.finish();
        }
    }
}
