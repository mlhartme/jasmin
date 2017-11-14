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

import static org.junit.Assert.assertEquals;

public class ModuleTest {
    @Test
    public void variantTree() throws Exception {
        Repository repository;
        Module group;

        repository = Repository.load(new Resolver(World.create()));
        group = repository.get("variant.tree");
        assertEquals("lead", group.getBest(MimeType.JS, "bar"));
        assertEquals("foo", group.getBest(MimeType.JS, "foo"));
        assertEquals("lead", group.getBest(MimeType.JS, "foox"));
        assertEquals("foo:bar", group.getBest(MimeType.JS, "foo:bar"));
        assertEquals("foo", group.getBest(MimeType.JS, "foo:baz"));
        assertEquals("foo:bar", group.getBest(MimeType.JS, "foo:bar:bau"));
        assertEquals("foo:bar:baz", group.getBest(MimeType.JS, "foo:bar:baz"));
    }
}
