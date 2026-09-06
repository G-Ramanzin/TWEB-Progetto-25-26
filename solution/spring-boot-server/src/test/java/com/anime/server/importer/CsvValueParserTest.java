package com.anime.server.importer;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for the defensive CSV parsing helpers. */
class CsvValueParserTest {

    @Test
    void normaliseHeaderTrimsLowersAndReplacesSpaces() {
        assertEquals("anime_id", CsvValueParser.normaliseHeader("  Anime ID "));
        assertEquals("", CsvValueParser.normaliseHeader(null));
    }

    @Test
    void valuePicksFirstMatchingNonEmptyColumn() {
        Map<String, Integer> headers = Map.of("mal_id", 0, "title", 1);
        String[] row = {"42", "Cowboy Bebop"};
        assertEquals("42", CsvValueParser.value(row, headers, "anime_id", "mal_id"));
        assertEquals("Cowboy Bebop", CsvValueParser.value(row, headers, "title"));
        assertNull(CsvValueParser.value(row, headers, "missing"));
    }

    @Test
    void valueTreatsUnknownAsMissing() {
        Map<String, Integer> headers = Map.of("episodes", 0);
        assertNull(CsvValueParser.value(new String[]{"Unknown"}, headers, "episodes"));
        assertNull(CsvValueParser.value(new String[]{""}, headers, "episodes"));
    }

    @Test
    void parseLongToleratesDecimalNotation() {
        assertEquals(123L, CsvValueParser.parseLong("123.0"));
        assertEquals(123L, CsvValueParser.parseLong("123"));
        assertNull(CsvValueParser.parseLong("abc"));
        assertNull(CsvValueParser.parseLong(null));
    }

    @Test
    void parseDoubleAndIntAreDefensive() {
        assertEquals(8.75, CsvValueParser.parseDouble("8.75"));
        assertNull(CsvValueParser.parseDouble("n/a"));
        assertEquals(7, CsvValueParser.parseInt("7.0"));
    }
}
