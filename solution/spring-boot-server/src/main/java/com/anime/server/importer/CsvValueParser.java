package com.anime.server.importer;

import java.util.Map;

/**
 * Small stateless helpers used by the CSV importer to read values in a
 * header-name tolerant way and to parse numbers defensively.
 * <p>
 * Kept free of Spring dependencies so it can be unit tested in isolation.
 */
public final class CsvValueParser {

    private CsvValueParser() {
        // utility class
    }

    /**
     * Normalises a CSV header: trimmed, lower case, spaces to underscores.
     *
     * @param header raw header cell
     * @return normalised header usable as a lookup key
     */
    public static String normaliseHeader(String header) {
        return header == null ? "" : header.trim().toLowerCase().replace(' ', '_');
    }

    /**
     * Returns the first non-empty value among the candidate column names.
     * Values equal to "Unknown", "UNKNOWN" or "\N" are treated as missing.
     *
     * @param row       CSV row cells
     * @param headerMap normalised header name to column index
     * @param keys      candidate column names, in priority order
     * @return the value found, or {@code null} when missing
     */
    public static String value(String[] row, Map<String, Integer> headerMap, String... keys) {
        for (String key : keys) {
            Integer idx = headerMap.get(key);
            if (idx != null && idx < row.length) {
                String v = row[idx] == null ? "" : row[idx].trim();
                if (!v.isEmpty() && !v.equalsIgnoreCase("unknown") && !v.equals("\\N")) {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Parses a long, tolerating decimal notation like "123.0" that some
     * CSV exports use for integer ids.
     *
     * @param s raw cell value
     * @return the parsed value, or {@code null} when unparsable
     */
    public static Long parseLong(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            String t = s.trim();
            if (t.contains(".")) t = t.substring(0, t.indexOf('.'));
            return Long.parseLong(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an int with the same tolerance as {@link #parseLong(String)}.
     */
    public static Integer parseInt(String s) {
        Long v = parseLong(s);
        return v == null ? null : v.intValue();
    }

    /**
     * Parses a double.
     *
     * @param s raw cell value
     * @return the parsed value, or {@code null} when unparsable
     */
    public static Double parseDouble(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
