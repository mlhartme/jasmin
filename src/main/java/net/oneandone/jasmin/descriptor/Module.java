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
