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
package net.oneandone.jasmin.descriptor;

import net.oneandone.jasmin.model.MimeType;
import net.oneandone.sushi.metadata.annotation.Option;
import net.oneandone.sushi.metadata.annotation.Type;
import net.oneandone.sushi.metadata.annotation.Value;

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
