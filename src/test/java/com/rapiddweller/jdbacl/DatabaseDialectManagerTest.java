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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.version.VersionNumber;
import com.rapiddweller.jdbacl.dialect.HSQL2Dialect;
import com.rapiddweller.jdbacl.dialect.HSQLDialect;
import com.rapiddweller.jdbacl.dialect.OracleDialect;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DatabaseDialectManager}.<br/><br/>
 * Created: 20.10.2011 14:16:04
 *
 * @author Volker Bergmann
 * @since 0.6.12
 */
public class DatabaseDialectManagerTest {

  /**
   * Test plain settings.
   */
  @Test
  public void testPlainSettings() {
    check("Oracle", "11.2.0.2", OracleDialect.class);
  }

  /**
   * Test versioned settings.
   */
  @Test
  public void testVersionedSettings() {
    check("HSQLDB", "1.5.8", HSQLDialect.class);
    check("HSQLDB", "2", HSQL2Dialect.class);
    check("HSQLDB", "2.0.0", HSQL2Dialect.class);
    check("HSQLDB", "2.9", HSQL2Dialect.class);
  }

  private static void check(String product, String version, Class<? extends DatabaseDialect> expectedClass) {
    VersionNumber versionNumber = VersionNumber.valueOf(version);
    DatabaseDialect dialect = DatabaseDialectManager.getDialectForProduct(product, versionNumber);
    assertEquals(expectedClass, dialect.getClass());
  }

}
