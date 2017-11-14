/*
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

// TODO Interface with Js and Css implementation. But that's an incompatible change of the sushi xml representation.
public enum MimeType {
    JS("text/javascript"), CSS("text/css");

    public static MimeType lookup(String path) {
        for (MimeType type : values()) {
            if (type.isInstance(path)) {
                return type;
            }
        }
        return null;
    }

    private final String mime;
    private final String extension;

    MimeType(String mime) {
        this.mime = mime;
        this.extension = "." + name().toLowerCase();
    }

    public String getMime() {
        return mime;
    }

    public boolean isInstance(String path) {
        return path.endsWith(extension);
    }

    public String comment(String line) {
        switch (this) {
            case JS :
                return "//### " + line + "\n";
            case CSS :
                return "/**** " + line + " */\n";
            default :
                throw new IllegalArgumentException(this.toString());
        }
    }
}
