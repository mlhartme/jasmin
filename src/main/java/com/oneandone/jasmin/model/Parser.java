package com.oneandone.jasmin.model;

import net.sf.beezle.sushi.util.Separator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class Parser {
    /** @return variant */
    public static void parseComment(String str, List<String> depends, List<String> calls)  throws IOException {
        final String prefix = "/* jasmin";
        int idx;
        String key;
        String value;

        if (!str.startsWith(prefix)) {
            throw new IOException("missing jasmin comment");
        }
        idx = str.indexOf("*/");
        if (idx == -1) {
            throw new IOException("jasmin comment is not closed");
        }
        for (String[] line : parse(str.substring(prefix.length(), idx))) {
            key = line[0];
            value = line[1];
            if ("depend".equals(key)) {
                depends.add(value);
            } else if ("call".equals(key)) {
                calls.add(value);
            } else {
                throw new IOException("unknown key: " + key);
            }
        }
    }

    private static final Separator NL = Separator.on('\n').trim().skipEmpty();

    public static List<String[]> parse(String str) throws IOException {
        int idx;
        List<String[]> result;

        result = new ArrayList<String[]>();
        for (String line : NL.split(str)) {
            idx = line.indexOf('=');
            if (idx == -1) {
                throw new IOException("malformed jasmin comment: missing '=' in line '" + line + "'");
            }
            result.add(new String[] { line.substring(0, idx).trim(), line.substring(idx + 1).trim() });
        }
        return result;
    }

    private Parser() {
    }
}
