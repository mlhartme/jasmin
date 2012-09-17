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

import net.oneandone.sushi.metadata.ComplexType;
import net.oneandone.sushi.metadata.Schema;
import net.oneandone.sushi.metadata.annotation.AnnotationSchema;
import net.oneandone.sushi.metadata.annotation.Sequence;

import java.util.ArrayList;
import java.util.List;

/** A list of modules. */
@net.oneandone.sushi.metadata.annotation.Type
public class Library {
    public static final Schema SCHEMA = new AnnotationSchema();
    public static final ComplexType TYPE = SCHEMA.complex(Library.class);

    @Sequence(String.class)
    private final List<String> jss;

    @Sequence(String.class)
    private final List<String> csss;

    @Sequence(Module.class)
    private final List<Module> modules;

    public Library() {
        this.jss = new ArrayList<String>();
        this.csss = new ArrayList<String>();
        this.modules = new ArrayList<Module>();
    }

    public List<String> jss() {
        return jss;
    }

    public List<String> csss() {
        return csss;
    }

    public List<Module> modules() {
        return modules;
    }

    @Override
    public String toString() {
        return TYPE.instance(this).toXml();
    }
}
