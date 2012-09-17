/**
 * Copyright 1&1 Internet AG, http://www.1and1.org
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
