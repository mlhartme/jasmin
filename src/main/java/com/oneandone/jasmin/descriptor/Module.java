package com.oneandone.jasmin.descriptor;

import net.sf.beezle.sushi.metadata.ComplexType;
import net.sf.beezle.sushi.metadata.annotation.Sequence;
import net.sf.beezle.sushi.metadata.annotation.Type;
import net.sf.beezle.sushi.metadata.annotation.Value;

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
