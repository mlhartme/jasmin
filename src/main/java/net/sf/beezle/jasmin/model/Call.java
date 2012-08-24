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

import net.sf.beezle.sushi.util.Separator;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** A list of modules. Plus load functionality (including linking and reload file handling */
@net.sf.beezle.sushi.metadata.annotation.Type
public final class Call {
    public static void call(Attributes attributes, String callStr, OutputStream out) {
        Object attribute;
        List<String> call;
        String target;
        int idx;
        Method method;

        call = Separator.SPACE.split(callStr);
        if (call.size() < 1) {
            throw new IllegalArgumentException("invalid call: " + call);
        }
        target = call.remove(0);
        idx = target.lastIndexOf('.');
        if (idx == -1) {
            throw new IllegalArgumentException("invalid target: " + target);
        }
        attribute = attributes.get(target.substring(0, idx));
        if (attribute == null) {
            throw new IllegalArgumentException("attribute not found: " + target);
        }
        method = lookup(attribute.getClass(), target.substring(idx + 1), formals(call.size()));
        if (method == null) {
            throw new IllegalArgumentException("method not found: " + target);
        }
        try {
            method.invoke(attribute, actuals(call, out));
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("illegal access", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("method failed: " + e.getTargetException());
        }
    }

    private static Class<?>[] formals(int strings) {
        Class<?>[] result;

        result = new Class<?>[strings + 1];
        for (int i = 0; i < strings; i++) {
            result[i] = String.class;
        }
        result[strings] = OutputStream.class;
        return result;
    }

    private static Object[] actuals(List<String> arguments, OutputStream out) {
        Object[] result;

        result = new Object[arguments.size() + 1];
        for (int i = 0; i < result.length - 1; i++) {
            result[i] = arguments.get(i);
        }
        result[result.length - 1] = out;
        return result;
    }

    private static Method lookup(Class<?> clazz, String name, Class<?>[] signature) {
        Method m;

        try {
            return clazz.getDeclaredMethod(name, signature);
        } catch (NoSuchMethodException e) {
            if (Object.class.equals(clazz)) {
                return null;
            }
            return lookup(clazz.getSuperclass(), name, signature);
        }
    }

    private Call() {
    }
}
