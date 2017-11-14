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

import net.oneandone.sushi.util.Separator;

import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/** A list of modules. Plus load functionality (including linking and reload file handling */
@net.oneandone.sushi.metadata.annotation.Type
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
