/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License (GPL).
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED CONDITIONS,
 * REPRESENTATIONS AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE
 * HEREBY EXCLUDED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.rapiddweller.jdbacl.dialect;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link HSQL2Dialect}.<br/><br/>
 * Created: 20.10.2011 21:01:00
 *
 * @author Volker Bergmann
 * @since 0.6.12
 */
public class HSQL2DialectTest extends DatabaseDialectTest<HSQL2Dialect> {

  /**
   * Test supports regex.
   */
  @Test
  public void testSupportsRegex() {
    assertTrue((new HSQL2Dialect()).supportsRegex());
  }

  /**
   * Instantiates a new Hsql 2 dialect test.
   */
  public HSQL2DialectTest() {
    super(new HSQL2Dialect());
  }

  /**
   * Test regex query.
   */
  @Test
  public void testRegexQuery() {
    assertEquals("NOT REGEXP_MATCHES(Expression, 'Regex')",
        (new HSQL2Dialect()).regexQuery("Expression", true, "Regex"));
    assertEquals("REGEXP_MATCHES(Expression, 'Regex')", (new HSQL2Dialect()).regexQuery("Expression", false, "Regex"));
  }

  /**
   * Test regex.
   */
  @Test
  public void testRegex() {
    assertTrue(dialect.supportsRegex());
    assertEquals("REGEXP_MATCHES(code, '[A-Z]{5}')", dialect.regexQuery("code", false, "[A-Z]{5}"));
    assertEquals("NOT REGEXP_MATCHES(code, '[A-Z]{5}')", dialect.regexQuery("code", true, "[A-Z]{5}"));
  }

}
