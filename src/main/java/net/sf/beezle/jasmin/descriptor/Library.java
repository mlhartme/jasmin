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
package net.sf.beezle.jasmin.descriptor;

import net.sf.beezle.sushi.metadata.ComplexType;
import net.sf.beezle.sushi.metadata.Schema;
import net.sf.beezle.sushi.metadata.annotation.AnnotationSchema;
import net.sf.beezle.sushi.metadata.annotation.Sequence;

import java.util.ArrayList;
import java.util.List;

/** A list of modules. */
@net.sf.beezle.sushi.metadata.annotation.Type
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
