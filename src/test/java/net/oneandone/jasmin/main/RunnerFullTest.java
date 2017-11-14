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
package net.oneandone.jasmin.main;

import net.oneandone.sushi.fs.World;
import org.junit.Test;

public class RunnerFullTest {
    @Test
    public void all() throws Exception {
        Runner.create("all", World.create()).addAll().invoke();
    }

    @Test
    public void all2() throws Exception {
        Runner.create("all2", World.create()).addAll().invoke(2, 10000, true, true, true);
    }

    @Test
    public void all12() throws Exception {
        Runner.create("all12", World.create()).addAll().invoke(12, 10000, true, true, true);
    }

    @Test
    public void explicit() throws Exception {
        World world;

        world = World.create();
        Runner.create("explicit", world).add("foo/css/head", "foo/js/v1\nfoo/js/v2").invoke();
    }
}
