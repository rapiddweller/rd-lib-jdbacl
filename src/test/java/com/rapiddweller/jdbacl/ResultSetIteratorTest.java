package com.rapiddweller.jdbacl;

import org.apache.derby.diag.ErrorLogReader;
import org.apache.derby.diag.ErrorMessages;
import org.h2.tools.SimpleResultSet;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * The type Result set iterator test.
 */
public class ResultSetIteratorTest {
  /**
   * The Thrown.
   */
  @Rule
  public ExpectedException thrown = ExpectedException.none();

  /**
   * Test constructor.
   */
  @Test
  public void testConstructor() {
    assertEquals("ResultSetIterator[]", (new ResultSetIterator(new ErrorLogReader())).toString());
    assertEquals("ResultSetIterator[Query]", (new ResultSetIterator(new ErrorLogReader(), "Query")).toString());
  }

  /**
   * Test constructor 2.
   */
  @Test
  public void testConstructor2() {
    thrown.expect(IllegalArgumentException.class);
    new ResultSetIterator(null);
  }

  /**
   * Test constructor 3.
   */
  @Test
  public void testConstructor3() {
    thrown.expect(IllegalArgumentException.class);
    new ResultSetIterator(null, "Query");
  }

  /**
   * Test get column labels.
   */
  @Test
  public void testGetColumnLabels() {
    assertEquals(7, (new ResultSetIterator(new ErrorLogReader())).getColumnLabels().length);
  }

  /**
   * Test has next.
   */
  @Test
  public void testHasNext() {
    thrown.expect(RuntimeException.class);
    (new ResultSetIterator(new ErrorLogReader())).hasNext();
  }

  /**
   * Test has next 2.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testHasNext2() throws IOException {
    assertTrue((new ResultSetIterator(new ErrorMessages())).hasNext());
  }

  /**
   * Test has next 3.
   */
  @Test
  public void testHasNext3() {
    assertFalse((new ResultSetIterator(new SimpleResultSet())).hasNext());
  }

  /**
   * Test next.
   */
  @Test
  public void testNext() {
    thrown.expect(RuntimeException.class);
    (new ResultSetIterator(new ErrorLogReader())).next();
  }

  /**
   * Test next 2.
   *
   * @throws IOException the io exception
   */
  @Test
  public void testNext2() throws IOException {
    ErrorMessages errorMessages = new ErrorMessages();
    assertSame(errorMessages, (new ResultSetIterator(errorMessages)).next());
  }

  /**
   * Test next 3.
   */
  @Test
  public void testNext3() {
    thrown.expect(IllegalStateException.class);
    (new ResultSetIterator(new SimpleResultSet())).next();
  }

  /**
   * Test to string.
   */
  @Test
  public void testToString() {
    assertEquals("ResultSetIterator[]", (new ResultSetIterator(new ErrorLogReader())).toString());
  }
}

