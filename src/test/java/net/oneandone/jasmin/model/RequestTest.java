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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class RequestTest {
    @Test(expected = IllegalArgumentException.class)
    public void missingSlash() {
        Request.parse("foo");
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptyVariant() {
        Request.parse("/.");
    }

    @Test
    public void js() {
        Request request;

        request = Request.parse("foo+bar/js/lead");
        assertSame(MimeType.JS, request.type);
        assertFalse(request.minimize);
        assertEquals("foo+bar", request.modules);
        assertEquals("lead", request.variant);
    }

    @Test
    public void dotname() {
        Request request;

        request = Request.parse("dot.name/css/x");
        assertEquals("dot.name", request.modules);
        assertEquals("x", request.variant);
        assertFalse(request.minimize);
        assertSame(MimeType.CSS, request.type);
    }

    @Test
    public void not() {
        Request request;

        request = Request.parse("!a/css/x");
        assertEquals("!a", request.modules);
        assertEquals("x", request.variant);
        assertFalse(request.minimize);
        assertSame(MimeType.CSS, request.type);
    }

    @Test
    public void css() {
        Request request;

        request = Request.parse("x/css-min/foo");
        assertSame(MimeType.CSS, request.type);
        assertTrue(request.minimize);
        assertEquals("x", request.modules);
        assertEquals("foo", request.variant);
    }
}
