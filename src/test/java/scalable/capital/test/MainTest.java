package scalable.capital.test;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {
    Main main = new Main();

    @Test
    void testQuery() {
        final String[] args = {"search term"};
        final Optional<String> query = main.getQuery(args);

        assertTrue(query.isPresent());
        assertEquals("search term", query.get());
    }

    @Test
    void testQuery_empty() {
        final String[] args = {};
        final Optional<String> query = main.getQuery(args);

        assertFalse(query.isPresent());
    }

    @Test
    void testGetPage() {
        final String uri = "http://foaas.com/version";
        final Optional<String> page = main.getPage(uri);

        assertTrue(page.isPresent());
        assertEquals("Version 1.2.0 FOAAS", page.get());
    }
}