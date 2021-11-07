/*
 * (c) Copyright 2010-2021 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.Encodings;
import com.rapiddweller.common.TimeUtil;
import com.rapiddweller.jdbacl.DBUtil;

import java.sql.Connection;
import java.sql.Types;
import java.util.Date;

/**
 * Abstract parent class for testing XML in-/output of jdbacl models.<br/><br/>
 * Created: 28.11.2010 09:58:56
 * @author Volker Bergmann
 * @since 0.6.4
 */
public abstract class AbstractModelTest {

  protected static final String URL = "jdbc:hsqldb:mem:benerator";
  protected static final String DRIVER = "org.hsqldb.jdbcDriver";
  protected static final String USER = "sa";
  protected static final String PASSWORD = null;
  protected static final String CATALOG = null;
  protected static final String SCHEMA = "PUBLIC";

  protected static final String CREATE_TABLES_FILE_NAME = "com/rapiddweller/jdbacl/model/xml/create_tables.sql";
  protected static final String DROP_TABLES_FILE_NAME = "com/rapiddweller/jdbacl/model/xml/drop_tables.sql";
  protected static final String EAGER_TEST_MODEL_FILENAME = "com/rapiddweller/jdbacl/model/xml/testmodel-eager.xml";
  protected static final String LAZY_TEST_MODEL_FILENAME = "com/rapiddweller/jdbacl/model/xml/testmodel-lazy-seq.xml";

  private Connection connection;

  @SuppressWarnings("unused")
  protected static Database createTestModel() {
    Database db = new Database("HSQL", "hsql", "1.5.8", new Date());
    db.setImportDate(TimeUtil.date(2011, 9, 21, 16, 50, 38, 0));
    db.setUser("Alice");
    db.setTableInclusionPattern("MY_.*");
    db.setTableExclusionPattern(".*_JN");

    DBCatalog catalog = new DBCatalog(null, db);
    DBSchema schema = new DBSchema("public", catalog);

    DBTable table1 = new DBTable("table1", TableType.TABLE, schema);
    DBColumn id1 = new DBColumn("id1", table1, Types.INTEGER, "int");
    DBColumn name1 = new DBColumn("name1", table1, Types.INTEGER, "int");
    DBPrimaryKeyConstraint pk1 = new DBPrimaryKeyConstraint(table1, "table1_pk", false, "id1");
    DBUniqueConstraint uk1 = new DBUniqueConstraint(table1, "table1_name1_uk", false, "name1");
    DBIndex index1 = new DBUniqueIndex("index1", true, uk1);

    DBTable table2 = new DBTable("table2", TableType.TABLE, schema);
    DBColumn id2 = new DBColumn("id2", table2, Types.INTEGER, "int");
    DBColumn ref2 = new DBColumn("ref2", table2, Types.INTEGER, "int");
    DBPrimaryKeyConstraint pk2 = new DBPrimaryKeyConstraint(table2, "table2_pk", false, "id2");
    DBForeignKeyConstraint fk2 = new DBForeignKeyConstraint("table2_fk2", false, table2, new String[] {"ref2"}, table1, new String[] {"id1"});

    DBTable table3 = new DBTable("table3", TableType.TABLE, schema);
    DBColumn id3_1 = new DBColumn("id3_1", table3, Types.INTEGER, "int");
    DBColumn id3_2 = new DBColumn("id3_2", table3, Types.INTEGER, "int");
    DBColumn name3 = new DBColumn("name3", table3, Types.INTEGER, "varchar(8)");
    DBColumn type3 = new DBColumn("type3", table3, Types.INTEGER, "char");
    DBPrimaryKeyConstraint pk3 = new DBPrimaryKeyConstraint(table3, "table3_pk", false, "id3_1", "id3_2");
    DBUniqueConstraint uk3 = new DBUniqueConstraint(table3, "table3_name3_uk", false, "name3", "type3");

    DBTable table4 = new DBTable("table4", TableType.TABLE, schema);
    DBColumn id4 = new DBColumn("id4", table4, Types.INTEGER, "int");
    DBColumn ref4_1 = new DBColumn("ref4_1", table4, Types.INTEGER, "int");
    DBColumn ref4_2 = new DBColumn("ref4_2", table4, Types.INTEGER, "int");
    DBPrimaryKeyConstraint pk4 = new DBPrimaryKeyConstraint(table4, "table4_pk", false, "id4");
    DBForeignKeyConstraint fk4 = new DBForeignKeyConstraint("table4_fk2", false, table4, new String[] {"ref4_1", "ref4_2"},
        table3, new String[] {"id3_1", "id3_2"});
    DBIndex index4 = new DBNonUniqueIndex("index4", true, table4, "ref4_1", "ref4_2");

    return db;
  }

  protected void createTables() throws Exception {
    connection = DBUtil.connect(URL, DRIVER, USER, PASSWORD, false);
    DBUtil.executeScriptFile(CREATE_TABLES_FILE_NAME, Encodings.UTF_8, connection, false, null);
  }

  protected void dropTables() throws Exception {
    try {
      connection = DBUtil.connect(URL, DRIVER, USER, PASSWORD, false);
      DBUtil.executeScriptFile(DROP_TABLES_FILE_NAME, Encodings.UTF_8, connection, false, null);
    } finally {
      DBUtil.close(connection);
    }
  }

}
