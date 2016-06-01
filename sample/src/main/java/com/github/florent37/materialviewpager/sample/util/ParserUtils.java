package com.github.florent37.materialviewpager.sample.util;

/**
 * Created by aaron on 2016/2/24.
 */
import android.content.ContentProvider;
import android.net.Uri;
import android.text.format.Time;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Various utility methods used by {@link com.github.florent37.materialviewpager.sample.io.JSONHandler}.
 */
public class ParserUtils {
    /** Used to sanitize a string to be {@link Uri} safe. */
    private static final Pattern sSanitizePattern = Pattern.compile("[^a-z0-9-_]");

    /**
     * Sanitize the given string to be {@link Uri} safe for building
     * {@link ContentProvider} paths.
     */
    public static String sanitizeId(String input) {
        if (input == null) {
            return null;
        }
        return sSanitizePattern.matcher(input.replace("+", "plus").toLowerCase()).replaceAll("");
    }

    /**
     * Parse the given string as a RFC 3339 timestamp, returning the value as
     * milliseconds since the epoch.
     */
    public static long parseTime(String timestamp) {
        final Time time = new Time();
        time.parse3339(timestamp);
        return time.toMillis(false);
    }

    public static String joinStrings(String connector, ArrayList<String> strings, StringBuilder recycle) {
        if (strings.size() <= 0) {
            return "";
        }
        if (recycle == null) {
            recycle = new StringBuilder();
        } else {
            recycle.setLength(0);
        }
        recycle.append(strings.get(0));
        for (int i = 1; i < strings.size(); i++) {
            recycle.append(connector);
            recycle.append(strings.get(i));
        }
        return recycle.toString();
    }
}
