package com.oneandone.jasmin.main;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FileCheckTest {
    @Test
    public void normal() throws Exception {
        FileCheck check;

        check = new FileCheck();
        check.minimizeClasspath();
        assertTrue(check.size() > 1);
        check = check.exceptions();
        assertTrue(check.toString(), check.size() == 0);
    }
}
