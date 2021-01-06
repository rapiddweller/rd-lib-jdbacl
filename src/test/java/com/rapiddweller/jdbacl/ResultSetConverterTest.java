package com.rapiddweller.jdbacl;

import static org.junit.Assert.assertEquals;

import com.rapiddweller.common.ConversionException;
import org.apache.derby.diag.ErrorLogReader;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ResultSetConverterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConvert() throws ConversionException {
        ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<Object>(Object.class);
        thrown.expect(ConversionException.class);
        resultSetConverter.convert(new ErrorLogReader());
    }

    @Test
    public void testConvert2() throws ConversionException {
        ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<Object>(Object.class);
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        resultSetConverter.convert(new SimpleResultSet());
    }

    @Test
    public void testConvert3() throws ConversionException {
        ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<Object>(Object.class, true);
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        resultSetConverter.convert(new SimpleResultSet());
    }

    @Test
    public void testConvert4() throws ConversionException {
        ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<Object>(Object.class, false);
        assertEquals(0, ((Object[]) resultSetConverter.convert(new SimpleResultSet())).length);
    }

    @Test
    public void testConvert5() throws ConversionException {
        thrown.expect(ConversionException.class);
        ResultSetConverter.convert(new ErrorLogReader(), true);
    }

    @Test
    public void testConvert6() throws ConversionException {
        thrown.expect(ArrayIndexOutOfBoundsException.class);
        ResultSetConverter.convert(new SimpleResultSet(), true);
    }

    @Test
    public void testConvert7() throws ConversionException {
        assertEquals(0, ((Object[]) ResultSetConverter.convert(new SimpleResultSet(), false)).length);
    }

    @Test
    public void testToString() {
        assertEquals("ResultSetConverter", (new ResultSetConverter<Object>(Object.class)).toString());
    }
}

