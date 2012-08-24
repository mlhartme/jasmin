package net.sf.beezle.jasmin.model;

import net.sf.beezle.sushi.fs.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ModuleTest {
    @Test
    public void variantTree() throws Exception {
        Repository repository;
        Module group;

        repository = Repository.load(new Resolver(new World()));
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
