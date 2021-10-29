package com.rapiddweller.jdbacl;

import com.rapiddweller.format.DataContainer;
// import org.apache.derby.diag.ErrorLogReader;
// import org.apache.derby.diag.ErrorMessages;
// import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.sql.ResultSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * The type Result set data iterator test.
 */
public class ResultSetDataIteratorTest {
  /**
   * The Thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Test constructor.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testConstructor() {
    assertEquals("ResultSetDataIterator[Query]", (new ResultSetDataIterator(new ErrorLogReader(), "Query")).toString());
  }
  */

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    thrown.expect(IllegalArgumentException.class);
    new ResultSetDataIterator(null, "Query");
  }

  /**
   * Test constructor 3.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testConstructor3() {
    thrown.expect(IllegalArgumentException.class);
    new ResultSetDataIterator(new ErrorLogReader(), null);
  }
  */

  /**
   * Test get column labels.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testGetColumnLabels() {
    assertEquals(7, (new ResultSetDataIterator(new ErrorLogReader(), "Query")).getColumnLabels().length);
  }
  */

  /**
   * Test next.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testNext() {
    ResultSetDataIterator resultSetDataIterator = new ResultSetDataIterator(new ErrorLogReader(), "Query");
    thrown.expect(RuntimeException.class);
    resultSetDataIterator.next(new DataContainer<>());
  }
  */

  /**
   * Test next 2.
   *
   * @throws IOException the io exception
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testNext2() throws IOException {
    ErrorMessages errorMessages = new ErrorMessages();
    ResultSetDataIterator resultSetDataIterator = new ResultSetDataIterator(errorMessages, "Query");
    DataContainer<ResultSet> dataContainer = new DataContainer<>();
    DataContainer<ResultSet> actualNextResult = resultSetDataIterator.next(dataContainer);
    assertSame(dataContainer, actualNextResult);
    assertSame(errorMessages, actualNextResult.getData());
  }
  */

  /**
   * Test next 3.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testNext3() {
    assertNull((new ResultSetDataIterator(new SimpleResultSet(), "Query")).next(null));
  }
  */

  /**
   * Test to string.
   */
  /* TODO make this test independent of implementation details in a JDBC driver
  @Test
  public void testToString() {
    assertEquals("ResultSetDataIterator[Query]", (new ResultSetDataIterator(new ErrorLogReader(), "Query")).toString());
  }
  */
}

