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
package net.oneandone.jasmin.model;

import net.oneandone.sushi.fs.Node;
import net.oneandone.sushi.fs.World;

import java.io.IOException;

public class ProfilerMain {

    private static final World WORLD;

    static {
        try {
            WORLD = World.create();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        js(WORLD.file("/home/mhm/Projects/order-modules/frontend-elements/src/main/resources/PUSTEFIX-INF/style/button.css").readString());
    }

    private static void js(String str) throws Exception {
        check(MimeType.CSS, str);
    }

    private static void check(MimeType type, String str) throws Exception {
        References references;

        Node node = WORLD.getTemp().createTempFile();
        node.writeString(str);
        references = References.create(type, true, node);
        references.readBytes();
        node.deleteFile();

        System.gc();
        Thread.sleep(1000);

        System.out.println("Startup done");
        Thread.sleep(20000);
        System.out.println("computing ...");

        node = WORLD.getTemp().createTempFile();
        node.writeString(str);
        references = References.create(type, true, node);
        references.readBytes();
        node.deleteFile();

        System.out.println("busy wait to keep references");
        for (; ; ) ;
    }
}
