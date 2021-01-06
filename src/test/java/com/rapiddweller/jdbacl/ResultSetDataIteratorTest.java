package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import com.rapiddweller.format.DataContainer;

import java.io.IOException;

import java.sql.ResultSet;

import org.apache.derby.diag.ErrorLogReader;
import org.apache.derby.diag.ErrorMessages;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResultSetDataIteratorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        assertEquals("ResultSetDataIterator[Query]", (new ResultSetDataIterator(new ErrorLogReader(), "Query")).toString());
    }

    @Test
    public void testConstructor2() {
        thrown.expect(IllegalArgumentException.class);
        new ResultSetDataIterator(null, "Query");
    }

    @Test
    public void testConstructor3() {
        thrown.expect(IllegalArgumentException.class);
        new ResultSetDataIterator(new ErrorLogReader(), null);
    }

    @Test
    public void testGetColumnLabels() {
        assertEquals(7, (new ResultSetDataIterator(new ErrorLogReader(), "Query")).getColumnLabels().length);
    }

    @Test
    public void testNext() {
        ResultSetDataIterator resultSetDataIterator = new ResultSetDataIterator(new ErrorLogReader(), "Query");
        thrown.expect(RuntimeException.class);
        resultSetDataIterator.next(new DataContainer<ResultSet>());
    }

    @Test
    public void testNext2() throws IOException {
        ErrorMessages errorMessages = new ErrorMessages();
        ResultSetDataIterator resultSetDataIterator = new ResultSetDataIterator(errorMessages, "Query");
        DataContainer<ResultSet> dataContainer = new DataContainer<ResultSet>();
        DataContainer<ResultSet> actualNextResult = resultSetDataIterator.next(dataContainer);
        assertSame(dataContainer, actualNextResult);
        assertSame(errorMessages, actualNextResult.getData());
    }

    @Test
    public void testNext3() {
        assertNull((new ResultSetDataIterator(new SimpleResultSet(), "Query")).next(null));
    }

    @Test
    public void testToString() {
        assertEquals("ResultSetDataIterator[Query]", (new ResultSetDataIterator(new ErrorLogReader(), "Query")).toString());
    }
}

