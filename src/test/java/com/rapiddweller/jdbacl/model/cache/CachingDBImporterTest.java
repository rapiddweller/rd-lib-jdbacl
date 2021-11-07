/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.cache;

import com.rapiddweller.common.FileUtil;
import com.rapiddweller.common.IOUtil;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.AbstractJDBCDBImporterTest;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;
import org.junit.Test;

import java.io.File;
import java.sql.Connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests the {@link CachingDBImporter}.<br/><br/>
 * Created: 18.03.2012 12:57:24
 * @author Volker Bergmann
 * @since 0.8.1
 */
public class CachingDBImporterTest extends AbstractJDBCDBImporterTest {

  private static final String TEST_TABLE_NAME = "CachingDBImporterTest";

  @Test
  public void testLazyImport() throws Exception {
    // given a database which has not been cached yet
    Connection connection = DBUtil.connect(URL, DRIVER, USER, PASSWORD, false);
    DBUtil.executeUpdate("create table " + TEST_TABLE_NAME + " ( id int, primary key (id))", connection);
    JDBCDBImporter realImporter = new JDBCDBImporter(URL, DRIVER, USER, PASSWORD, CATALOG, SCHEMA);
    CachingDBImporter importer = null;
    try {
      importer = new CachingDBImporter(realImporter);
      File cacheFile = importer.getCacheFile();
      FileUtil.deleteIfExists(cacheFile);
      // when importing the database without accessing the indexes...
      Database db1 = importer.importDatabase();
      DBTable table1 = db1.getTable(TEST_TABLE_NAME);
      // ...then the indexes shall not be fetched
      assertFalse(table1.areIndexesImported());
      // when requesting meta data a second time...
      Database db2 = importer.importDatabase();
      DBTable table2 = db2.getTable(TEST_TABLE_NAME);
      // then the cache must be able to fetch the missing information dynamically
      assertEquals(1, table2.getIndexes().size());
    } finally {
      DBUtil.executeUpdate("drop table " + TEST_TABLE_NAME, connection);
      IOUtil.close(importer);
    }
  }

  @Test
  public void testGetCacheFileName() {
    assertEquals("jdbc_hsqldb_mem_mydb_9001-usr_sa-cat_topcat-sch_public.meta.xml",
        CachingDBImporter.getCacheFileName("jdbc:hsqldb:mem:mydb:9001", "sa", "TOPCAT", "PUBLIC"));
    assertEquals("jdbc_hsqldb_mem_mydb_9001-usr_sa.meta.xml", CachingDBImporter.getCacheFileName(
        "jdbc:hsqldb:mem:mydb:9001", "sa", null, null));
  }

}
