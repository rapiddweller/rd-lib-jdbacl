package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.derby.diag.ErrorLogReader;
import org.apache.derby.diag.ErrorMessages;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResultSetIteratorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        assertEquals("ResultSetIterator[]", (new ResultSetIterator(new ErrorLogReader())).toString());
        assertEquals("ResultSetIterator[Query]", (new ResultSetIterator(new ErrorLogReader(), "Query")).toString());
    }

    @Test
    public void testConstructor2() {
        thrown.expect(IllegalArgumentException.class);
        new ResultSetIterator(null);
    }

    @Test
    public void testConstructor3() {
        thrown.expect(IllegalArgumentException.class);
        new ResultSetIterator(null, "Query");
    }

    @Test
    public void testGetColumnLabels() {
        assertEquals(7, (new ResultSetIterator(new ErrorLogReader())).getColumnLabels().length);
    }

    @Test
    public void testHasNext() {
        thrown.expect(RuntimeException.class);
        (new ResultSetIterator(new ErrorLogReader())).hasNext();
    }

    @Test
    public void testHasNext2() throws IOException {
        assertTrue((new ResultSetIterator(new ErrorMessages())).hasNext());
    }

    @Test
    public void testHasNext3() {
        assertFalse((new ResultSetIterator(new SimpleResultSet())).hasNext());
    }

    @Test
    public void testNext() {
        thrown.expect(RuntimeException.class);
        (new ResultSetIterator(new ErrorLogReader())).next();
    }

    @Test
    public void testNext2() throws IOException {
        ErrorMessages errorMessages = new ErrorMessages();
        assertSame(errorMessages, (new ResultSetIterator(errorMessages)).next());
    }

    @Test
    public void testNext3() {
        thrown.expect(IllegalStateException.class);
        (new ResultSetIterator(new SimpleResultSet())).next();
    }

    @Test
    public void testToString() {
        assertEquals("ResultSetIterator[]", (new ResultSetIterator(new ErrorLogReader())).toString());
    }
}

