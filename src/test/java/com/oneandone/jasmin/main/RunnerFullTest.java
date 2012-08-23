package com.oneandone.jasmin.main;

import net.sf.beezle.sushi.fs.World;
import org.junit.Test;

public class RunnerFullTest {
    @Test
    public void all() throws Exception {
        Runner.create("all", new World()).addAll().invoke();
    }

    @Test
    public void all2() throws Exception {
        Runner.create("all2", new World()).addAll().invoke(2, 10000, true, true, true);
    }

    @Test
    public void all12() throws Exception {
        Runner.create("all12", new World()).addAll().invoke(12, 10000, true, true, true);
    }

    @Test
    public void explicit() throws Exception {
        World world;

        world = new World();
        Runner.create("explicit", world).add("foo/css/head", "foo/js/v1\nfoo/js/v2").invoke();
    }
}
