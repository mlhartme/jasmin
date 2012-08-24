package net.sf.beezle.jasmin.model;

/**
 * Refers to modules. Format:
 *
 * module "/" type "/" variant
 */
public class Request {
    public static Request parse(String path) {
        int first;
        int second;
        String modules;
        String variant;
        String typeName;
        MimeType type;
        boolean minimize;

        first = path.indexOf('/');
        if (first == -1) {
            throw new IllegalArgumentException("missing slash: '" + path + "'");
        }
        second = path.indexOf('/', first + 1);
        if (second == -1) {
            throw new IllegalArgumentException("missing second slash: " + path);
        }
        modules = path.substring(0, first);
        typeName = path.substring(first + 1, second).toUpperCase();
        minimize = typeName.endsWith("-MIN");
        if (minimize) {
            typeName = typeName.substring(0, typeName.length() - 4);
        }
        type = MimeType.valueOf(typeName);
        variant = path.substring(second + 1);
        if (variant.length() == 0) {
            throw new IllegalArgumentException("empty variant: " + path);
        }
        return new Request(modules, type, minimize, variant);
    }

    public final String modules;
    public final boolean minimize;
    public final MimeType type;
    public final String variant;

    public Request(String modules, MimeType type, boolean minimize, String variant) {
        this.modules = modules;
        this.type = type;
        this.minimize = minimize;
        this.variant = variant;
    }

    @Override
    public int hashCode() {
        return modules.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder builder;

        builder = new StringBuilder();
        builder.append(modules);
        builder.append('/');
        builder.append(variant);
        builder.append(type.toString().toLowerCase());
        if (minimize) {
            builder.append("-min");
        }
        return builder.toString();
    }
}
