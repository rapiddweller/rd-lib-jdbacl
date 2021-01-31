/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;

import java.io.InputStream;
import java.io.StreamTokenizer;

import java.util.ArrayList;

import java.util.Arrays;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.ForeignKeyPath;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link SQLUtil} class.<br/><br/>
 * Created: 28.11.2010 10:27:54
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class SQLUtilTest {

    private static final String CALL = "call myprocedure";

    private static final String INSERT = "insert into users (id, name) values (1, 'Alice')";
    private static final String UPDATE = "UPDATE users set name='xxx' where name = 'Alice'";
    private static final String DELETE = "DELETE users where name = 'Alice'";
    private static final String TRUNCATE = "TRUNCATE users";

    private static final String ALTER_SESSION = "ALTER SESSION BLABLA";

    private static final String QUERY = "select * from users";
    private static final String WITH = "with temp as (select dummy c from dual) select * from temp a, temp b";

    private static final String ALTER_TABLE = "ALTER TABLE users";
    private static final String DROP_TABLE = "drop table users";
    private static final String CREATE_TABLE = "Create Table USERS";

    @Test
    public void testParseColumnTypeAndSize2() {
        assertEquals(1, SQLUtil.parseColumnTypeAndSize("Spec").length);
    }

    @Test
    public void testPrependAlias() {
        assertEquals(1, SQLUtil.prependAlias("Table Alias", new String[]{"Column Names"}).length);
        assertEquals(1, SQLUtil.prependAlias(null, new String[]{"Column Names"}).length);
        assertEquals(0, SQLUtil.prependAlias("Table Alias", new String[]{}).length);
    }

    @Test
    public void testRenderColumnNames() {
        DBTable table = new DBTable("Name");
        DBColumn e = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        ArrayList<DBColumn> dbColumnList = new ArrayList<>();
        dbColumnList.add(e);
        assertEquals("Name", SQLUtil.renderColumnNames(dbColumnList));
    }

    @Test
    public void testRenderColumnNames2() {
        DBTable table = new DBTable("Name");
        DBColumn e = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        ArrayList<DBColumn> dbColumnList = new ArrayList<>();
        dbColumnList.add(e);
        DBTable table1 = new DBTable("Name");
        dbColumnList.add(new DBColumn("Name", table1, DBDataType.getInstance("BLOB")));
        assertEquals("Name, Name", SQLUtil.renderColumnNames(dbColumnList));
    }

    @Test
    public void testRenderColumnNames3() {
        DBTable table = new DBTable("Name");
        assertEquals("Name",
                SQLUtil.renderColumnNames(new DBColumn[]{new DBColumn("Name", table, DBDataType.getInstance("BLOB"))}));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testRenderColumnNames4() {
        SQLUtil.renderColumnNames(new DBColumn[]{});
    }

    @Test
    public void testRenderColumnNames5() {
        assertEquals("(Column Names)", SQLUtil.renderColumnNames(new String[]{"Column Names"}));
    }

    @Test
    public void testRenderColumn() {
        DBTable table = new DBTable("Name");
        assertEquals("Name BLOB NULL", SQLUtil.renderColumn(new DBColumn("Name", table, DBDataType.getInstance("BLOB"))));
    }

    @Test
    public void testRenderColumn2() {
        assertEquals("Name null NULL", SQLUtil.renderColumn(new DBColumn("Name", new DBTable("Name"), null)));
    }

    @Test
    public void testRenderColumn3() {
        DBTable table = new DBTable("Name");
        DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        dbColumn.setDefaultValue(" NULL");
        assertEquals("Name BLOB DEFAULT  NULL NULL", SQLUtil.renderColumn(dbColumn));
    }

    @Test
    public void testRenderColumn4() {
        DBTable table = new DBTable("Name");
        DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        dbColumn.setSize(3);
        assertEquals("Name BLOB NULL", SQLUtil.renderColumn(dbColumn));
    }

    @Test
    public void testRenderColumn5() {
        DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), null);
        dbColumn.setSize(3);
        assertEquals("Name null(3) NULL", SQLUtil.renderColumn(dbColumn));
    }

    @Test
    public void testRenderColumn6() {
        DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), null);
        dbColumn.setFractionDigits(0);
        dbColumn.setSize(3);
        assertEquals("Name null(3,0) NULL", SQLUtil.renderColumn(dbColumn));
    }

    @Test
    public void testRenderColumnTypeWithSize() {
        DBTable table = new DBTable("Name");
        assertEquals("BLOB", SQLUtil.renderColumnTypeWithSize(new DBColumn("Name", table, DBDataType.getInstance("BLOB"))));
    }

    @Test
    public void testRenderColumnTypeWithSize2() {
        assertEquals("null", SQLUtil.renderColumnTypeWithSize(new DBColumn("Name", new DBTable("Name"), null)));
    }

    @Test
    public void testRenderColumnTypeWithSize3() {
        DBTable table = new DBTable("Name");
        DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        dbColumn.setSize(3);
        assertEquals("BLOB", SQLUtil.renderColumnTypeWithSize(dbColumn));
    }

    @Test
    public void testRenderColumnTypeWithSize4() {
        DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), null);
        dbColumn.setSize(3);
        assertEquals("null(3)", SQLUtil.renderColumnTypeWithSize(dbColumn));
    }

    @Test
    public void testRenderColumnTypeWithSize5() {
        DBColumn dbColumn = new DBColumn("Name", new DBTable("Name"), null);
        dbColumn.setFractionDigits(0);
        dbColumn.setSize(3);
        assertEquals("null(3,0)", SQLUtil.renderColumnTypeWithSize(dbColumn));
    }

    @Test
    public void testParseColumnTypeAndSize() {
        checkParsing("int", "int");
        checkParsing("number(11)", "number", 11);
        checkParsing("number(8, 2)", "number", 8, 2);
    }

    @Test
    public void testIsQuery() {
        assertFalse(SQLUtil.isQuery(CREATE_TABLE));
        assertFalse(SQLUtil.isQuery(DROP_TABLE));
        assertFalse(SQLUtil.isQuery(ALTER_TABLE));
        assertTrue(SQLUtil.isQuery(QUERY));
        assertTrue(SQLUtil.isQuery(WITH));
        assertFalse(SQLUtil.isQuery(ALTER_SESSION));
        assertFalse(SQLUtil.isQuery(INSERT));
        assertFalse(SQLUtil.isQuery(UPDATE));
        assertFalse(SQLUtil.isQuery(DELETE));
        assertFalse(SQLUtil.isQuery(TRUNCATE));
        assertFalse(SQLUtil.isQuery(CALL));
        assertFalse(SQLUtil.isQuery("Sql"));
    }

    @Test
    public void testConstraintSpec() {
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("UNIQUE (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), null, true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("CONSTRAINT Name PRIMARY KEY (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
        assertEquals("CONSTRAINT Name PRIMARY KEY (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.ALWAYS));
        assertEquals("CONSTRAINT \"PRIMARY KEY \" PRIMARY KEY (foo, foo, foo)",
                SQLUtil.constraintSpec(
                        new DBPrimaryKeyConstraint(new DBTable("Name"), "PRIMARY KEY ", true, "foo", "foo", "foo"),
                        NameSpec.IF_REPRODUCIBLE));
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil.constraintSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", false, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testConstraintSpec2() {
        DBTable owner = new DBTable("Name");
        assertEquals("FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)", SQLUtil.constraintSpec(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                NameSpec.NEVER));
    }

    @Test
    public void testPkSpec() {
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil
                .pkSpec(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil
                .pkSpec(new DBPrimaryKeyConstraint(new DBTable("Name"), null, true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("CONSTRAINT Name PRIMARY KEY (foo, foo, foo)", SQLUtil.pkSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
        assertEquals("CONSTRAINT Name PRIMARY KEY (foo, foo, foo)", SQLUtil
                .pkSpec(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.ALWAYS));
        assertEquals("CONSTRAINT \"PRIMARY KEY \" PRIMARY KEY (foo, foo, foo)",
                SQLUtil.pkSpec(new DBPrimaryKeyConstraint(new DBTable("Name"), "PRIMARY KEY ", true, "foo", "foo", "foo"),
                        NameSpec.IF_REPRODUCIBLE));
        assertEquals("PRIMARY KEY (foo, foo, foo)", SQLUtil.pkSpec(
                new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", false, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testUkSpec() {
        assertEquals("UNIQUE (foo, foo, foo)",
                SQLUtil.ukSpec(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("UNIQUE (foo, foo, foo)",
                SQLUtil.ukSpec(new DBUniqueConstraint(new DBTable("Name"), null, true, "foo", "foo", "foo"), NameSpec.NEVER));
        assertEquals("UNIQUE (foo, foo, foo)",
                SQLUtil.ukSpec(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), null));
        assertEquals("CONSTRAINT Name UNIQUE (foo, foo, foo)", SQLUtil.ukSpec(
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
        assertEquals("CONSTRAINT Name UNIQUE (foo, foo, foo)", SQLUtil
                .ukSpec(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"), NameSpec.ALWAYS));
        assertEquals("CONSTRAINT \"UNIQUE \" UNIQUE (foo, foo, foo)", SQLUtil.ukSpec(
                new DBUniqueConstraint(new DBTable("Name"), "UNIQUE ", true, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
        assertEquals("UNIQUE (foo, foo, foo)", SQLUtil.ukSpec(
                new DBUniqueConstraint(new DBTable("Name"), "Name", false, "foo", "foo", "foo"), NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testFkSpec() {
        DBTable owner = new DBTable("Name");
        assertEquals("FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)", SQLUtil.fkSpec(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                NameSpec.NEVER));
    }

    @Test
    public void testFkSpec2() {
        DBTable owner = new DBTable("Name");
        assertEquals("FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                SQLUtil.fkSpec(
                        new DBForeignKeyConstraint(null, true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                        NameSpec.NEVER));
    }

    @Test
    public void testFkSpec3() {
        DBTable owner = new DBTable("Name");
        assertEquals("FOREIGN KEY (foo, foo, foo) REFERENCES Name(foo, foo, foo)",
                SQLUtil.fkSpec(new DBForeignKeyConstraint("Name", true, owner, new String[]{"foo", "foo", "foo"},
                        new DBTable("Name"), new String[]{"foo", "foo", "foo"}), NameSpec.NEVER));
    }

    @Test
    public void testFkSpec4() {
        DBTable owner = new DBTable("Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)", SQLUtil.fkSpec(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testFkSpec5() {
        DBTable owner = new DBTable("Name");
        assertEquals("CONSTRAINT Name FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)", SQLUtil.fkSpec(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                NameSpec.ALWAYS));
    }

    @Test
    public void testFkSpec6() {
        DBTable owner = new DBTable("Name");
        assertEquals("CONSTRAINT \"FOREIGN KEY \" FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)",
                SQLUtil.fkSpec(new DBForeignKeyConstraint("FOREIGN KEY ", true, owner, "Fk Column Name", new DBTable("Name"),
                        "Referee Column Name"), NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testFkSpec7() {
        DBTable owner = new DBTable("Name");
        assertEquals("FOREIGN KEY (Fk Column Name) REFERENCES Name(Referee Column Name)", SQLUtil.fkSpec(
                new DBForeignKeyConstraint("Name", false, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"),
                NameSpec.IF_REPRODUCIBLE));
    }

    @Test
    public void testJoinFKPath() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name Join Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias"
                        + " Base_1__.Fk Column Name = Intermediate Alias Base_2__.Referee Column Name Join Type JOIN Name End"
                        + " Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1, new DBForeignKeyConstraint("Name", true,
                                owner2, "Fk Column Name", new DBTable("Name"), "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath10() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name ="
                        + " Intermediate Alias Base_2__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name End Alias ON Intermediate Alias Base_2__.foo = End Alias.foo AND Intermediate"
                        + " Alias Base_2__.foo = End Alias.foo AND Intermediate Alias Base_2__.foo = End Alias.foo",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                new DBForeignKeyConstraint("Name", true, owner2, new String[]{"foo", "foo", "foo"}, new DBTable("Name"),
                                        new String[]{"foo", "foo", "foo"})),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testJoinFKPath11() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias Base_1__.Referee"
                        + " Column Name \n"
                        + "\tJOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name = Intermediate"
                        + " Alias Base_2__.Referee Column Name \n"
                        + "\tJOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil
                        .joinFKPath(
                                new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                        new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                                "Referee Column Name")),
                                "INNER", "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testJoinFKPath12() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias Base_1__.Referee"
                        + " Column Name \n"
                        + "\tJOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name = Intermediate"
                        + " Alias Base_2__.Referee Column Name \n"
                        + "\tJOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil
                        .joinFKPath(
                                new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                        new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                                "Referee Column Name")),
                                null, "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testJoinFKPath13() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name Join Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias"
                        + " Base_1__.Fk Column Name = Intermediate Alias Base_2__.Referee Column Name Join Type JOIN Name End"
                        + " Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil
                        .joinFKPath(
                                new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                        new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                                "Referee Column Name")),
                                "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base", null));
    }

    @Test
    public void testJoinFKPath2() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.foo = Intermediate Alias Base_1__.foo"
                        + " AND Start Alias.foo = Intermediate Alias Base_1__.foo AND Start Alias.foo = Intermediate Alias"
                        + " Base_1__.foo Join Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column"
                        + " Name = Intermediate Alias Base_2__.Referee Column Name Join Type JOIN Name End Alias ON Intermediate"
                        + " Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1, new DBForeignKeyConstraint("Name", true,
                                owner2, "Fk Column Name", new DBTable("Name"), "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath3() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name Join Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias"
                        + " Base_1__.foo = Intermediate Alias Base_2__.foo AND Intermediate Alias Base_1__.foo = Intermediate"
                        + " Alias Base_2__.foo AND Intermediate Alias Base_1__.foo = Intermediate Alias Base_2__.foo Join Type"
                        + " JOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1, new DBForeignKeyConstraint("Name", true,
                                owner2, "Fk Column Name", new DBTable("Name"), "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath4() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name Join Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias"
                        + " Base_1__.Fk Column Name = Intermediate Alias Base_2__.Referee Column Name Join Type JOIN Name End"
                        + " Alias ON Intermediate Alias Base_2__.foo = End Alias.foo AND Intermediate Alias Base_2__.foo = End"
                        + " Alias.foo AND Intermediate Alias Base_2__.foo = End Alias.foo",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                new DBForeignKeyConstraint("Name", true, owner2, new String[]{"foo", "foo", "foo"}, new DBTable("Name"),
                                        new String[]{"foo", "foo", "foo"})),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath5() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias Base_1__.Referee"
                        + " Column Name JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name ="
                        + " Intermediate Alias Base_2__.Referee Column Name JOIN Name End Alias ON Intermediate Alias Base_2__.Fk"
                        + " Column Name = End Alias.Referee Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1, new DBForeignKeyConstraint("Name", true,
                                owner2, "Fk Column Name", new DBTable("Name"), "Referee Column Name")),
                        "INNER", "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath6() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias Base_1__.Referee"
                        + " Column Name JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name ="
                        + " Intermediate Alias Base_2__.Referee Column Name JOIN Name End Alias ON Intermediate Alias Base_2__.Fk"
                        + " Column Name = End Alias.Referee Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1, new DBForeignKeyConstraint("Name", true,
                                owner2, "Fk Column Name", new DBTable("Name"), "Referee Column Name")),
                        null, "Start Alias", "End Alias", "Intermediate Alias Base"));
    }

    @Test
    public void testJoinFKPath7() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name ="
                        + " Intermediate Alias Base_2__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee"
                        + " Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                        "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testJoinFKPath8() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner2 = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.foo = Intermediate Alias Base_1__.foo"
                        + " AND Start Alias.foo = Intermediate Alias Base_1__.foo AND Start Alias.foo = Intermediate Alias"
                        + " Base_1__.foo \n"
                        + "\tJoin Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.Fk Column Name ="
                        + " Intermediate Alias Base_2__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee"
                        + " Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                        "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testJoinFKPath9() {
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable owner1 = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint1 = new DBForeignKeyConstraint("Name", true, owner1,
                new String[]{"foo", "foo", "foo"}, new DBTable("Name"), new String[]{"foo", "foo", "foo"});
        DBTable owner2 = new DBTable("Name");
        assertEquals("Join Type JOIN Name Intermediate Alias Base_1__ ON Start Alias.Fk Column Name = Intermediate Alias"
                        + " Base_1__.Referee Column Name \n"
                        + "\tJoin Type JOIN Name Intermediate Alias Base_2__ ON Intermediate Alias Base_1__.foo = Intermediate"
                        + " Alias Base_2__.foo AND Intermediate Alias Base_1__.foo = Intermediate Alias Base_2__.foo AND Intermediate"
                        + " Alias Base_1__.foo = Intermediate Alias Base_2__.foo \n"
                        + "\tJoin Type JOIN Name End Alias ON Intermediate Alias Base_2__.Fk Column Name = End Alias.Referee"
                        + " Column Name",
                SQLUtil.joinFKPath(
                        new ForeignKeyPath(dbForeignKeyConstraint, dbForeignKeyConstraint1,
                                new DBForeignKeyConstraint("Name", true, owner2, "Fk Column Name", new DBTable("Name"),
                                        "Referee Column Name")),
                        "Join Type", "Start Alias", "End Alias", "Intermediate Alias Base", "Indent"));
    }

    @Test
    public void testIsDDL() {
        assertTrue(SQLUtil.isDDL(CREATE_TABLE));
        assertTrue(SQLUtil.isDDL(DROP_TABLE));
        assertTrue(SQLUtil.isDDL(ALTER_TABLE));
        assertFalse(SQLUtil.isDDL(QUERY));
        assertFalse(SQLUtil.isDDL(ALTER_SESSION));
        assertFalse(SQLUtil.isDDL(INSERT));
        assertFalse(SQLUtil.isDDL(UPDATE));
        assertFalse(SQLUtil.isDDL(DELETE));
        assertFalse(SQLUtil.isDDL(TRUNCATE));
        assertFalse(SQLUtil.isDDL(CALL));
        assertFalse(SQLUtil.isDDL("Sql"));
    }

    @Test
    public void testIsDML() {
        assertFalse(SQLUtil.isDML(CREATE_TABLE));
        assertFalse(SQLUtil.isDML(DROP_TABLE));
        assertFalse(SQLUtil.isDML(ALTER_TABLE));
        assertFalse(SQLUtil.isDML(QUERY));
        assertFalse(SQLUtil.isDML(ALTER_SESSION));
        assertTrue(SQLUtil.isDML(INSERT));
        assertTrue(SQLUtil.isDML(UPDATE));
        assertTrue(SQLUtil.isDML(DELETE));
        assertTrue(SQLUtil.isDML(TRUNCATE));
        assertFalse(SQLUtil.isDML(CALL));
        assertFalse(SQLUtil.isDML("Sql"));
    }

    @Test
    public void testIsProcedureCall() {
        assertFalse(SQLUtil.isProcedureCall(CREATE_TABLE));
        assertFalse(SQLUtil.isProcedureCall(DROP_TABLE));
        assertFalse(SQLUtil.isProcedureCall(ALTER_TABLE));
        assertFalse(SQLUtil.isProcedureCall(QUERY));
        assertFalse(SQLUtil.isProcedureCall(ALTER_SESSION));
        assertFalse(SQLUtil.isProcedureCall(INSERT));
        assertFalse(SQLUtil.isProcedureCall(UPDATE));
        assertFalse(SQLUtil.isProcedureCall(DELETE));
        assertFalse(SQLUtil.isProcedureCall(TRUNCATE));
        assertTrue(SQLUtil.isProcedureCall(CALL));
        assertFalse(SQLUtil.isProcedureCall("Sql"));
    }

    @Test
    public void testMutatesStructure() {
        assertTrue(SQLUtil.mutatesStructure(CREATE_TABLE));
        assertTrue(SQLUtil.mutatesStructure(DROP_TABLE));
        assertTrue(SQLUtil.mutatesStructure(ALTER_TABLE));
        assertFalse(SQLUtil.mutatesStructure(QUERY));
        assertFalse(SQLUtil.mutatesStructure(ALTER_SESSION));
        assertFalse(SQLUtil.mutatesStructure(INSERT));
        assertFalse(SQLUtil.mutatesStructure(UPDATE));
        assertFalse(SQLUtil.mutatesStructure(DELETE));
        assertFalse(SQLUtil.mutatesStructure(TRUNCATE));
        assertFalse(SQLUtil.mutatesStructure(CALL));
        assertFalse(SQLUtil.mutatesStructure("Sql"));
    }

    @Test
    public void testMutatesDataOrStructure() {
        assertTrue(SQLUtil.mutatesDataOrStructure(CREATE_TABLE));
        assertTrue(SQLUtil.mutatesDataOrStructure(DROP_TABLE));
        assertTrue(SQLUtil.mutatesDataOrStructure(ALTER_TABLE));
        assertFalse(SQLUtil.mutatesDataOrStructure(QUERY));
        assertFalse(SQLUtil.mutatesDataOrStructure(ALTER_SESSION));
        assertTrue(SQLUtil.mutatesDataOrStructure(INSERT));
        assertTrue(SQLUtil.mutatesDataOrStructure(UPDATE));
        assertTrue(SQLUtil.mutatesDataOrStructure(DELETE));
        assertTrue(SQLUtil.mutatesDataOrStructure(TRUNCATE));
        assertNull(SQLUtil.mutatesDataOrStructure(CALL));
        assertNull(SQLUtil.mutatesDataOrStructure("Sql"));
    }

    @Test
    public void testRemoveComments() {
        assertEquals("select a from b", SQLUtil.removeComments("select a from b"));
        assertEquals("select a from b", SQLUtil.removeComments("select a/*, x, y */ from b"));
        assertEquals("select a from b", SQLUtil.removeComments("select a /*, x, y */from b/* join c on ref=id*/"));
        assertEquals("Sql", SQLUtil.removeComments("Sql"));
    }

    @Test
    public void testNormalize() {
        assertEquals("select x from t", SQLUtil.normalize("select x from t", false));
        assertEquals("select min (x) from t", SQLUtil.normalize("select min(x) from t", false));
        assertEquals("select min (x) - 2 from t", SQLUtil.normalize("select min(x)-2 from t", false));
        assertEquals("select 3.141 * 2 - 6.0 from t", SQLUtil.normalize("select 3.141*2-6.0 from t", false));
        assertEquals("select t_id from s.t", SQLUtil.normalize("select t_id from s.t", false));
        assertEquals("select 'id', id from \"x\".\"t\"", SQLUtil.normalize("select 'id',id from \"x\".\"t\"", false));
        assertEquals("select a /* x, y */ from b", SQLUtil.normalize("select a /*x,y*/ from b", false));
        assertEquals("select a from b", SQLUtil.normalize("select a /*x,y*/ from b", true));
        assertEquals("select a from b -- ignore this", SQLUtil.normalize("select a from b--ignore this", false));
        assertEquals("select a from b", SQLUtil.normalize("select a from b--ignore this", true));
        assertEquals("Sql", SQLUtil.normalize("Sql", true));
        assertEquals(")", SQLUtil.normalize(")", true));
        assertEquals("*/", SQLUtil.normalize("*/", true));
        assertEquals("Sql", SQLUtil.normalize("Sql", false));
        assertEquals("--", SQLUtil.normalize("--", false));
        assertEquals("/*", SQLUtil.normalize("/*", false));
        assertEquals("/ /", SQLUtil.normalize("//", false));
        assertEquals("com.rapiddweller.jdbacl.model.DBCheckConstraint",
                SQLUtil.normalize("com.rapiddweller.jdbacl.model.DBCheckConstraint", true));
    }

    @Test
    public void testRenderNumber() {
        assertEquals("- 0", SQLUtil.renderNumber(new StreamTokenizer(InputStream.nullInputStream())));
    }

    @Test
    public void testRenderNumber2() {
        StreamTokenizer streamTokenizer = new StreamTokenizer(InputStream.nullInputStream());
        streamTokenizer.lowerCaseMode(true);
        assertEquals("- 0", SQLUtil.renderNumber(streamTokenizer));
    }

    @Test
    public void testRenderColumnListWithTableName() {
        assertEquals("t.x, t.y", SQLUtil.renderColumnListWithTableName("t", "x", "y"));
        assertEquals("Table.Columns", SQLUtil.renderColumnListWithTableName("Table", "Columns"));
        assertEquals("Table.Columns, Table.Columns", SQLUtil.renderColumnListWithTableName("Table", "Columns", "Columns"));
    }

    @Test
    public void testEquals() {
        assertEquals("Table Alias1.Col Names1 = Table Alias2.Col Names2",
                SQLUtil.equals("Table Alias1", new String[]{"Col Names1"}, "Table Alias2", new String[]{"Col Names2"}));
        assertEquals("Col Names1 = Table Alias2.Col Names2",
                SQLUtil.equals(null, new String[]{"Col Names1"}, "Table Alias2", new String[]{"Col Names2"}));
        assertEquals("", SQLUtil.equals("Table Alias1", new String[]{}, "Table Alias2", new String[]{"Col Names2"}));
        assertEquals("Table Alias1.Col Names1 = Col Names2",
                SQLUtil.equals("Table Alias1", new String[]{"Col Names1"}, null, new String[]{"Col Names2"}));
    }

    @Test(expected = ArrayIndexOutOfBoundsException.class)
    public void testEquals2() {
        SQLUtil.equals("Table Alias1", new String[]{" = ", " = "}, "Table Alias2", new String[]{"Col Names2"});
    }

    @Test
    public void testJoinFK() {
        DBTable a = new DBTable("a");
        new DBColumn("id", a, DBDataType.getInstance("INT"));

        DBTable b = new DBTable("b");
        new DBColumn("id", b, DBDataType.getInstance("INT"));
        new DBColumn("a_id", b, DBDataType.getInstance("INT"));
        DBForeignKeyConstraint ba = new DBForeignKeyConstraint("ba_fk", true, b, "a_id", a, "id");

        String sql = SQLUtil.joinFK(ba, "inner", "_start", "_end");
        assertEquals("JOIN a _end ON _start.a_id = _end.id", sql);
    }

    @Test
    public void testJoinFK2() {
        DBTable owner = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Referee Alias ON Referer Alias.Fk Column Name = Referee Alias.Referee" + " Column Name",
                SQLUtil.joinFK(new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"),
                        "Referee Column Name"), "Join Type", "Referer Alias", "Referee Alias"));
    }

    @Test
    public void testJoinFK3() {
        DBTable owner = new DBTable("Name");
        assertEquals(
                "Join Type JOIN Name Referee Alias ON Referer Alias.foo = Referee Alias.foo AND Referer Alias.foo ="
                        + " Referee Alias.foo AND Referer Alias.foo = Referee Alias.foo",
                SQLUtil.joinFK(new DBForeignKeyConstraint("Name", true, owner, new String[]{"foo", "foo", "foo"},
                        new DBTable("Name"), new String[]{"foo", "foo", "foo"}), "Join Type", "Referer Alias", "Referee Alias"));
    }

    @Test
    public void testJoinFK4() {
        DBTable owner = new DBTable("Name");
        assertEquals("JOIN Name Referee Alias ON Referer Alias.Fk Column Name = Referee Alias.Referee Column Name",
                SQLUtil.joinFK(new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"),
                        "Referee Column Name"), "INNER", "Referer Alias", "Referee Alias"));
    }

    @Test
    public void testJoinFK5() {
        DBTable owner = new DBTable("Name");
        assertEquals("JOIN Name Referee Alias ON Referer Alias.Fk Column Name = Referee Alias.Referee Column Name",
                SQLUtil.joinFK(new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"),
                        "Referee Column Name"), "", "Referer Alias", "Referee Alias"));
    }

    @Test(expected =IllegalArgumentException.class)
            public void testJoinFK6() {
        DBTable owner = new DBTable("Name");
        SQLUtil.joinFK(new DBForeignKeyConstraint("Name", true, owner, new String[]{"INNER"}, new DBTable("Name"),
                new String[]{"foo", "foo", "foo"}), "Join Type", "Referer Alias", "Referee Alias");
    }

    @Test
    public void testLeftJoin() {
        assertEquals("LEFT JOIN Right Table Right Alias ON Left Alias.Left Columns = Right Alias.Right Columns",
                SQLUtil.leftJoin("Left Alias", new String[]{"Left Columns"}, "Right Table", "Right Alias",
                        new String[]{"Right Columns"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLeftJoin2() {
        SQLUtil.leftJoin("Left Alias", new String[]{}, "Right Table", "Right Alias", new String[]{"Right Columns"});
    }

    @Test
    public void testInnerJoin() {
        assertEquals("JOIN Right Table Right Alias ON Left Alias.Left Columns = Right Alias.Right Columns",
                SQLUtil.innerJoin("Left Alias", new String[]{"Left Columns"}, "Right Table", "Right Alias",
                        new String[]{"Right Columns"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInnerJoin2() {
        SQLUtil.innerJoin("Left Alias", new String[]{}, "Right Table", "Right Alias", new String[]{"Right Columns"});
    }

    @Test
    public void testJoin() {
        assertEquals("Type JOIN Right Table Right Alias ON Left Alias.Left Columns = Right Alias.Right Columns",
                SQLUtil.join("Type", "Left Alias", new String[]{"Left Columns"}, "Right Table", "Right Alias",
                        new String[]{"Right Columns"}));
        assertEquals("JOIN Right Table Right Alias ON Left Alias.Left Columns = Right Alias.Right Columns",
                SQLUtil.join("INNER", "Left Alias", new String[]{"Left Columns"}, "Right Table", "Right Alias",
                        new String[]{"Right Columns"}));
        assertEquals("JOIN Right Table Right Alias ON Left Alias.Left Columns = Right Alias.Right Columns", SQLUtil.join("",
                "Left Alias", new String[]{"Left Columns"}, "Right Table", "Right Alias", new String[]{"Right Columns"}));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testJoin2() {
        SQLUtil.join("Type", "Left Alias", new String[]{}, "Right Table", "Right Alias", new String[]{"Right Columns"});
    }

    @Test
    public void testAddRequiredCondition() {
        StringBuilder stringBuilder = new StringBuilder();
        assertSame(stringBuilder, SQLUtil.addRequiredCondition("Condition", stringBuilder));
    }

    @Test
    public void testAddRequiredCondition2() {
        StringBuilder stringBuilder = new StringBuilder("Str");
        assertSame(stringBuilder, SQLUtil.addRequiredCondition("Condition", stringBuilder));
    }

    @Test
    public void testAddRequiredCondition3() {
        StringBuilder stringBuilder = new StringBuilder("Str");
        assertSame(stringBuilder, SQLUtil.addRequiredCondition(" AND ", stringBuilder));
    }

    @Test
    public void testAddOptionalCondition() {
        StringBuilder stringBuilder = new StringBuilder();
        assertSame(stringBuilder, SQLUtil.addOptionalCondition("Condition", stringBuilder));
    }

    @Test
    public void testAddOptionalCondition2() {
        StringBuilder stringBuilder = new StringBuilder("Str");
        assertSame(stringBuilder, SQLUtil.addOptionalCondition("Condition", stringBuilder));
    }

    @Test
    public void testAddOptionalCondition3() {
        StringBuilder stringBuilder = new StringBuilder("Str");
        assertSame(stringBuilder, SQLUtil.addOptionalCondition(" OR ", stringBuilder));
    }

    @Test
    public void testOwnerDotComponent() {
        assertEquals("null", SQLUtil.ownerDotComponent(new DBCatalog()));
        assertEquals("Name", SQLUtil.ownerDotComponent(new DBCatalog("Name")));
        assertEquals("null.Name", SQLUtil.ownerDotComponent(new DBSchema("Name", new DBCatalog())));
    }

    @Test
    public void testConstraintName() {
        assertEquals("CONSTRAINT Name ",
                SQLUtil.constraintName(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertEquals("",
                SQLUtil.constraintName(new DBPrimaryKeyConstraint(new DBTable("Name"), null, true, "foo", "foo", "foo")));
    }

    @Test
    public void testTypeAndName() {
        assertEquals("catalog null", SQLUtil.typeAndName(new DBCatalog()));
        assertNull(SQLUtil.typeAndName(null));
        assertEquals("catalog Name", SQLUtil.typeAndName(new DBCatalog("Name")));
    }

    @Test
    public void testJoinFKRoute() {
        DBTable a = new DBTable("a");
        new DBColumn("id", a, DBDataType.getInstance("INT"));

        DBTable b = new DBTable("b");
        new DBColumn("id", b, DBDataType.getInstance("INT"));
        new DBColumn("a_id", b, DBDataType.getInstance("INT"));
        DBForeignKeyConstraint ba = new DBForeignKeyConstraint("ba_fk", true, b, "a_id", a, "id");

        DBTable c = new DBTable("c");
        new DBColumn("id", c, DBDataType.getInstance("INT"));
        new DBColumn("b_id", c, DBDataType.getInstance("INT"));
        DBForeignKeyConstraint cb = new DBForeignKeyConstraint("cb_fk", true, c, "b_id", b, "id");

        ForeignKeyPath route = new ForeignKeyPath(cb, ba);
        String sql = SQLUtil.joinFKPath(route, "inner", "start__", "end__", "tmp");
        assertEquals("JOIN b tmp_1__ ON start__.b_id = tmp_1__.id JOIN a end__ ON tmp_1__.a_id = end__.id", sql);
    }

    @Test
    public void testAllNull() {
        assertEquals("c IS NULL", SQLUtil.allNull(new String[]{"c"}, null));
        assertEquals("c1 IS NULL AND c2 IS NULL", SQLUtil.allNull(new String[]{"c1", "c2"}, null));
        assertEquals("t.c IS NULL", SQLUtil.allNull(new String[]{"c"}, "t"));
        assertEquals("t.c1 IS NULL AND t.c2 IS NULL", SQLUtil.allNull(new String[]{"c1", "c2"}, "t"));
        assertEquals("Table Alias.Columns IS NULL", SQLUtil.allNull(new String[]{"Columns"}, "Table Alias"));
        assertEquals("", SQLUtil.allNull(new String[]{}, "Table Alias"));
        assertEquals("Table Alias. IS NULL IS NULL AND Table Alias. IS NULL IS NULL",
                SQLUtil.allNull(new String[]{" IS NULL", " IS NULL"}, "Table Alias"));
        assertEquals("Columns IS NULL", SQLUtil.allNull(new String[]{"Columns"}, null));
    }

    // helpers ---------------------------------------------------------------------------------------------------------

    public void checkParsing(String spec, Object... expected) {
        Object[] actual = SQLUtil.parseColumnTypeAndSize(spec);
        String message = "Expected: [" + ArrayFormat.format(expected) + "], " +
                "found: [" + ArrayFormat.format(actual) + "]";
        assertTrue(message, Arrays.deepEquals(expected, actual));
    }

}
