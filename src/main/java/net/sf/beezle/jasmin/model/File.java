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

import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.metadata.annotation.Type;

/** Basic building block of a module. Created from a Resource by the resolver. */
@Type
public class File {
    private final Node normal;
    private final Node minimized;

    private final MimeType type;
    private final String variant;

    public File(Node normal, Node minimized, MimeType type, String variant) {
        this.normal = normal;
        this.minimized = minimized;
        this.type = type;
        this.variant = variant;
    }

    public Node getNormal() {
        return normal;
    }

    public Node getMinimized() {
        return minimized;
    }

    public Node get(boolean min) {
        return min && minimized != null ? minimized : normal;
    }

    public MimeType getType() {
        return type;
    }

    public String getVariant() {
        return variant;
    }

    public String toString() {
        return normal.getName();
    }
}
