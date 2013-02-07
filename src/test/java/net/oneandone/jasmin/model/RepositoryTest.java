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

import net.oneandone.graph.CyclicDependency;
import net.oneandone.sushi.fs.World;
import net.oneandone.sushi.util.Strings;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RepositoryTest {
    private World world;
    private Repository repository;

    @Before
    public void before() throws IOException {
        world = new World();
        repository = Repository.load(new Resolver(world));
    }

    @Test
    public void builtIn() {
        repository.get("jasmin-labjs");
    }

    @Test(expected = RuntimeException.class)
    public void notFound() throws Exception {
        repository.get("notfound");
    }

    @Test
    public void auto() {
        Module module;
        File file;

        module = repository.get("jasmin-auto-auto1");
        assertNotNull(module);
        assertEquals(2, module.files().size());
        file = module.files().get(0);
        if (!file.getNormal().getName().endsWith("auto1.js")) {
            module.files().remove(0);
            module.files().add(file);
            file = module.files().get(0);
        }
        assertTrue("", file.getNormal().getName().endsWith("auto1.js"));
        assertTrue("", file.getMinimized().getName().endsWith("auto1-min.js"));
        assertNull(file.getVariant());
        file = module.files().get(1);
        assertEquals("foo", file.getVariant());

        module = repository.get("jasmin-auto-sub-auto2");
        assertEquals(Arrays.asList(repository.get("jasmin-auto-auto1")), module.dependencies());
        assertEquals(1, module.files().size());
        file = module.files().get(0);
        assertTrue(file.getNormal().getPath().endsWith("auto/sub/auto2.js"));

        module = repository.get("jasmin-auto-sub-only");
        assertEquals(1, module.files().size());
        file = module.files().get(0);
        assertEquals(file.getNormal(), file.getMinimized());
    }

    @Test
    public void get() throws Exception {
        Module group;

        group = repository.get("foo");
        assertEquals("foo", group.getName());
    }

    @Test
    public void sequenceOne() throws Exception {
        assertEquals(Arrays.asList("sub.bar", "foo"), repository.sequence("foo"));
    }

    @Test
    public void sequenceFirstAsFirst() throws Exception {
        assertEquals(Arrays.asList("two", "sub.bar", "foo"), repository.sequence("two", "foo"));
    }

    @Test
    public void sequenceLastAsLast() throws Exception {
        assertEquals(Arrays.asList("sub.bar", "foo", "two"), repository.sequence("foo", "two"));
    }

    @Test
    public void resolveOne() throws Exception {
        check("foo/css/x", "my.css");
    }

    @Test
    public void resolveTwo() throws Exception {
        check("sub.bar+foo/css/x", "my.css");
    }

    @Test
    public void resolveNot() throws Exception {
        check("foo+!foo/css/x");
        check("foo+!sub.bar/css/x", "my.css");
        check("sub.bar+!foo/css/x");
    }

    @Test(expected = CyclicDependency.class)
    public void resolveDuplicate() throws Exception {
        check("foo+foo/css/x", "my.css");
    }

    private void check(String request, String... expected) throws Exception {
        References references;

        references = repository.resolve(Request.parse(request));
        assertEquals("expected " + Strings.toList(expected) + ", go " + references, expected.length, references.nodes.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], references.nodes.get(i).getName());
        }
    }
}
