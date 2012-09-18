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

import net.oneandone.jasmin.main.Servlet;
import net.oneandone.ssass.scss.term.Strng;

/**
 * Refers to modules. Format:
 *
 * module "/" type "/" variant
 */
public class Request {
    public static Request parse(String path) {
        int firstIdx;
        int secondIdx;
        String first;
        String second;
        String variant;
        Request result;

        firstIdx = path.indexOf('/');
        if (firstIdx == -1) {
            throw new IllegalArgumentException("missing slash: '" + path + "'");
        }
        first = path.substring(0, firstIdx);
        secondIdx = path.indexOf('/', firstIdx + 1);
        if (secondIdx == -1) {
            second = path.substring(firstIdx + 1);
            variant = "lead";
        } else {
            second = path.substring(firstIdx + 1, secondIdx);
            variant = path.substring(secondIdx + 1);
            if (variant.length() == 0) {
                throw new IllegalArgumentException("empty variant: " + path);
            }
        }

        try {
            result = create(first, second, variant);
        } catch (IllegalArgumentException e) {
            try {
                result = create(second, first, variant);
                Servlet.LOG.warn("deprecated request: type should preceed modules: " + path);
            } catch (IllegalArgumentException eAgain) {
                throw e;
            }
        }
        return result;
    }

    public static Request create(String typeName, String modules, String variant) {
        boolean minimize;

        typeName = typeName.toUpperCase();
        minimize = typeName.endsWith("-MIN");
        if (minimize) {
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        return new Request(modules, MimeType.valueOf(typeName), minimize, variant);
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
