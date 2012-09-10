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
package net.sf.beezle.jasmin.descriptor;

import net.sf.beezle.jasmin.model.MimeType;
import net.sf.beezle.sushi.metadata.annotation.Option;
import net.sf.beezle.sushi.metadata.annotation.Type;
import net.sf.beezle.sushi.metadata.annotation.Value;

/** Basic building block of a module, used to instantiate Files. */
@Type
public class Resource {
    @Value
    private MimeType type;

    @Value
    private Base base;

    /** may include encoded query parameters */
    @Value
    private String path;

    @Option
    private String minPath;

    @Option
    private String variant;

    public Resource() {
        this(MimeType.JS, Base.CLASSPATH, "nopath", null, null);
    }

    public Resource(MimeType type, Base base, String path, String minPath, String variant) {
        if (path.startsWith("/")) {
            throw new IllegalArgumentException("path must not start with '/': " + path);
        }
        if (minPath != null && minPath.startsWith("/")) {
            throw new IllegalArgumentException("minPath must not start with '/': " + minPath);
        }
        this.type = type;
        this.base = base;
        this.path = path;
        this.minPath = minPath;
        this.variant = variant;
    }

    public MimeType getType() {
        return type;
    }

    public void setType(MimeType type) {
        this.type = type;
    }

    public Base getBase() {
        return base;
    }

    public void setBase(Base base) {
        this.base = base;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMinPath() {
        return minPath;
    }

    public void setMinPath(String minPath) {
        this.minPath = minPath;
    }

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }
}
