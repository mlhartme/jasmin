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
import net.oneandone.sushi.metadata.annotation.Sequence;
import net.oneandone.sushi.metadata.annotation.Type;
import net.oneandone.sushi.metadata.annotation.Value;

import java.util.ArrayList;
import java.util.List;

/** A module has a name, a list of resources, and a list of dependencies to other modules. */
@Type
public class Module {
    public static final ComplexType TYPE = Library.SCHEMA.complex(Module.class);

    @Value
    private String name;

    /** relative paths. */
    @Sequence(Resource.class)
    private final List<Resource> resources;

    /** modules names. */
    @Sequence(String.class)
    private final List<String> dependencies;

    public Module() {
        this("default");
    }

    public Module(String name) {
        this.name = name;
        this.resources = new ArrayList<Resource>();
        this.dependencies = new ArrayList<String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> resources() {
        return resources;
    }

    public List<String> dependencies() {
        return dependencies;
    }

    @Override
    public String toString() {
        return TYPE.instance(this).toXml();
    }
}
