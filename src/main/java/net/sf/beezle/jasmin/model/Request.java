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

/**
 * Refers to modules. Format:
 *
 * module "/" type "/" variant
 */
public class Request {
    public static Request parse(String path) {
        int first;
        int second;
        String modules;
        String variant;
        String typeName;
        MimeType type;
        boolean minimize;

        first = path.indexOf('/');
        if (first == -1) {
            throw new IllegalArgumentException("missing slash: '" + path + "'");
        }
        second = path.indexOf('/', first + 1);
        if (second == -1) {
            throw new IllegalArgumentException("missing second slash: " + path);
        }
        modules = path.substring(0, first);
        typeName = path.substring(first + 1, second).toUpperCase();
        minimize = typeName.endsWith("-MIN");
        if (minimize) {
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        type = MimeType.valueOf(typeName);
        variant = path.substring(second + 1);
        if (variant.length() == 0) {
            throw new IllegalArgumentException("empty variant: " + path);
        }
        return new Request(modules, type, minimize, variant);
    }

    public final String modules;
    public final boolean minimize;
    public final MimeType type;
    public final String variant;

    public Request(String modules, MimeType type, boolean minimize, String variant) {
        this.modules = modules;
        this.type = type;
        this.minimize = minimize;
        this.variant = variant;
    }

    @Override
    public int hashCode() {
        return modules.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder;

        builder = new StringBuilder();
        builder.append(modules);
        builder.append('/');
        builder.append(variant);
        builder.append(type.toString().toLowerCase());
        if (minimize) {
            builder.append("-min");
        }
        return builder.toString();
    }
}
