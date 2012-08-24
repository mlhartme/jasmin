package net.sf.beezle.jasmin.model;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ParserTest {
    @Test
    public void dependencies() {
        deps("/* jasmin */");
        deps("/* jasmin depend = foo */", "foo");
        deps("/* jasmin \n"
             + "     depend = foo\n"
             + "     depend=bar */\n", "foo", "bar");
    }

    private void deps(String str, String ... expected) {
        List<String> depends;
        List<String> webservices;

        depends = new ArrayList<String>();
        webservices = new ArrayList<String>();
        try {
            Parser.parseComment(str, depends, webservices);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertEquals(Arrays.asList(expected), depends);
    }
}
