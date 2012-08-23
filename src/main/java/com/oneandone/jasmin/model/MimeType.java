package com.oneandone.jasmin.model;

// TODO Interface with Js and Css implementation. But that's an incompatible change of the sushi xml representation.
public enum MimeType {
    JS("text/javascript"), CSS("text/css");

    public static final int LINE_BREAK = 300;

    public static MimeType lookup(String path) {
        for (MimeType type : values()) {
            if (type.isInstance(path)) {
                return type;
            }
        }
        return null;
    }

    private final String mime;
    private final String extension;

    MimeType(String mime) {
        this.mime = mime;
        this.extension = "." + name().toLowerCase();
    }

    public String getMime() {
        return mime;
    }

    public boolean isInstance(String path) {
        return path.endsWith(extension);
    }

    public String comment(String line) {
        switch (this) {
            case JS :
                return "//### " + line + "\n";
            case CSS :
                return "/**** " + line + " */\n";
            default :
                throw new IllegalArgumentException(this.toString());
        }
    }
}
