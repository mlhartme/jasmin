package com.oneandone.jasmin.descriptor;

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
