package com.oneandone.jasmin.model;

import net.sf.beezle.sushi.fs.Node;
import net.sf.beezle.sushi.metadata.annotation.Type;

/** Basic building block of a module. Created from a Resource by the resolver. */
@Type
public class File {
    private final Node normal;
    private final Node minimized;

    private final MimeType type;
    private final String variant;

    public File(Node normal, Node minimized, MimeType type, String variant) {
        this.normal = normal;
        this.minimized = minimized;
        this.type = type;
        this.variant = variant;
    }

    public Node getNormal() {
        return normal;
    }

    public Node getMinimized() {
        return minimized;
    }

    public Node get(boolean min) {
        return min && minimized != null ? minimized : normal;
    }

    public MimeType getType() {
        return type;
    }

    public String getVariant() {
        return variant;
    }

    public String toString() {
        return normal.getName();
    }
}
