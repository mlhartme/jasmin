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
package net.sf.beezle.jasmin.model;

// TODO Interface with Js and Css implementation. But that's an incompatible change of the sushi xml representation.
public enum MimeType {
    JS("text/javascript"), CSS("text/css");

    public static final int LINE_BREAK = 300;

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
