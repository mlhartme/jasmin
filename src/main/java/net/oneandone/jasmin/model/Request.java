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
        modules = path.substring(0, first);

        second = path.indexOf('/', first + 1);
        if (second == -1) {
            typeName = path.substring(first + 1).toUpperCase();
            variant = "lead";
        } else {
            typeName = path.substring(first + 1, second).toUpperCase();
            variant = path.substring(second + 1);
            if (variant.length() == 0) {
                throw new IllegalArgumentException("empty variant: " + path);
            }
        }
        minimize = typeName.endsWith("-MIN");
        if (minimize) {
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        type = MimeType.valueOf(typeName);
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
