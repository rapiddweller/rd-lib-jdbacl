package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ConversionException;
import org.apache.derby.diag.ErrorLogReader;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

/**
 * The type Result set converter test.
 */
public class ResultSetConverterTest {
  /**
   * The Thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Test convert.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert() throws ConversionException {
    ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<>(Object.class);
    thrown.expect(ConversionException.class);
    resultSetConverter.convert(new ErrorLogReader());
  }

  /**
   * Test convert 2.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert2() throws ConversionException {
    ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<>(Object.class);
    thrown.expect(ArrayIndexOutOfBoundsException.class);
    resultSetConverter.convert(new SimpleResultSet());
  }

  /**
   * Test convert 3.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert3() throws ConversionException {
    ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<>(Object.class, true);
    thrown.expect(ArrayIndexOutOfBoundsException.class);
    resultSetConverter.convert(new SimpleResultSet());
  }

  /**
   * Test convert 4.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert4() throws ConversionException {
    ResultSetConverter<Object> resultSetConverter = new ResultSetConverter<>(Object.class, false);
    assertEquals(0, ((Object[]) resultSetConverter.convert(new SimpleResultSet())).length);
  }

  /**
   * Test convert 5.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert5() throws ConversionException {
    thrown.expect(ConversionException.class);
    ResultSetConverter.convert(new ErrorLogReader(), true);
  }

  /**
   * Test convert 6.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert6() throws ConversionException {
    thrown.expect(ArrayIndexOutOfBoundsException.class);
    ResultSetConverter.convert(new SimpleResultSet(), true);
  }

  /**
   * Test convert 7.
   *
   * @throws ConversionException the conversion exception
   */
  @Test
  public void testConvert7() throws ConversionException {
    assertEquals(0, ((Object[]) ResultSetConverter.convert(new SimpleResultSet(), false)).length);
  }

  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    assertEquals("ResultSetConverter", (new ResultSetConverter<>(Object.class)).toString());
  }
}

