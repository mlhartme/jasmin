package com.oneandone.jasmin.model;

import com.oneandone.jasmin.descriptor.Base;
import net.sf.beezle.sushi.fs.World;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class EngineTest {
    private Engine engine;

    @Before
    public void before() throws IOException {
        World world;
        Resolver resolver;

        world = new World();
        resolver = new Resolver(world, true);
        resolver.add(Base.CLASSPATH, world.guessProjectHome(EngineTest.class).join("src/test/resources"));
        engine = new Engine(Repository.load(resolver));
    }

    @Test
    public void normal() throws IOException {
        assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                engine.process("foo/js/lead"));
    }

    @Test
    public void twoFilesNormal() throws IOException {
        assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n" + "\n"
                + "//###\n" + "var special = 0;\n", engine.process("two/js/lead"));
    }

    @Test
    public void variantSpecial() throws IOException {
        assertEq("//###\n" + "var special = 0;\n", engine.process("foo/js/special"));
    }

    @Test
    public void variantLead() throws IOException {
        assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                engine.process("foo/js/lead"));
    }

    @Test
    public void variantUnknown() throws IOException {
        assertEq("//###\n" + "var str = \"äöü\";\n" + "var a = 0;\n" + "/* comment */\n" + "var b = 2;\n",
                engine.process("foo/js/unknown"));
    }

    @Test
    public void min() throws IOException {
        assertEquals("var str=\"äöü\";var a=0;var b=2;", engine.process("foo/js-min/lead"));
    }

    private void assertEq(String expected, String found) {
        assertEquals(expected, found.replaceAll("//###.*\n", "//###\n"));
    }
}
