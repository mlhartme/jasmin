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
