/*
 * (c) Copyright 2012-2014 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.jdbc;

import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.Database;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link JDBCDBImporter}.<br/><br/>
 * Created: 31.01.2012 12:32:56
 *
 * @author Volker Bergmann
 * @since 0.8.0
 */
public class JDBCDBImporterTest extends AbstractJDBCDBImporterTest {

  private Connection connection;

  /**
   * Sets up.
   *
   * @throws Exception the exception
   */
  @Before
  public void setUp() throws Exception {
    this.connection = setupDatabase();
  }

  /**
   * Tear down.
   *
   * @throws Exception the exception
   */
  @After
  public void tearDown() throws Exception {
    dropDatabaseTables(connection);
  }

  /**
   * Test import database hsql.
   */
  @Test
  public void testImportDatabase_HSQL() {
    Database db = new Database("hsqlmem", new JDBCDBImporter(connection, "sa", null), true);
    checkImports(false, false, false, db);
    DBSchema schema = checkSchema(db);
    DBTable table = checkTables(schema);
    checkIndexes(table);
    checkImports(false, false, false, db);
    checkSequences(schema);
    checkImports(true, false, false, db);
    checkTriggers(schema);
    checkImports(true, true, false, db);
    checkPackages(schema);
    checkImports(true, true, true, db);
  }

  private static void checkSequences(DBSchema schema) {
    List<DBSequence> sequences = schema.getSequences(true);
    assertEquals(1, sequences.size());
    assertEquals("SEQ1", sequences.get(0).getName());
  }

  private static void checkTriggers(DBSchema schema) {
    assertEquals(0, schema.getTriggers().size());
  }

  private static void checkPackages(DBSchema schema) {
    assertEquals(0, schema.getPackages().size());
  }

  /**
   * Check imports.
   *
   * @param sequences the sequences
   * @param triggers  the triggers
   * @param packages  the packages
   * @param db        the db
   */
  public void checkImports(boolean sequences, boolean triggers, boolean packages, Database db) {
    assertEquals(sequences, db.isSequencesImported());
    assertEquals(triggers, db.isTriggersImported());
    assertEquals(packages, db.isPackagesImported());
  }

}
