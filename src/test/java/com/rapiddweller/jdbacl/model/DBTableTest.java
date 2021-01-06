package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.jdbacl.model.jdbc.DBIndexInfo;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class DBTableTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testFKRecReceiveFK() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        DBTable dbTable1 = new DBTable("Name");
        (dbTable.new FKRec()).receiveFK(dbForeignKeyConstraint, dbTable1);
        assertSame(dbTable1, dbForeignKeyConstraint.getTable());
    }

    @Test
    public void testGetComponents() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getComponents().isEmpty());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetComponents2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        assertTrue(dbTable.getComponents().isEmpty());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetComponents3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        List<DBTableComponent> actualComponents = dbTable.getComponents();
        assertEquals(1, actualComponents.size());
        assertTrue(((DBTable) actualComponents.get(0).getOwner()).isPKImported());
    }

    @Test
    public void testGetComponents4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(1, dbTable.getComponents().size());
    }

    @Test
    public void testGetCatalog() {
        assertNull((new DBTable("Name", TableType.TABLE, new DBSchema("Name"))).getCatalog());
    }

    @Test
    public void testGetSchema() {
        assertNull((new DBTable("Name")).getSchema());
    }

    @Test
    public void testIdxReceiverReceiveIndex() {
        DBTable.IdxReceiver idxReceiver = (new DBTable("Name")).new IdxReceiver();
        DBIndexInfo indexInfo = new DBIndexInfo("Name", "Table Name", (short) 1, "Catalog Name", true, (short) 1,
                "Column Name", true, 1, 1, "Filter Condition");
        DBTable dbTable = new DBTable("Name");
        idxReceiver.receiveIndex(indexInfo, true, dbTable, new DBSchema("Name"));
        List<DBTableComponent> components = dbTable.getComponents();
        DBTableComponent getResult = components.get(0);
        DBTableComponent getResult1 = components.get(1);
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
        boolean actualIsNameDeterministicResult = ((DBUniqueConstraint) getResult).isNameDeterministic();
        assertTrue(actualIsNameDeterministicResult);
        assertTrue(((DBUniqueIndex) getResult1).isNameDeterministic());
        assertEquals(1, ((DBUniqueIndex) getResult1).getColumnNames().length);
        assertSame(dbTable, getResult1.getOwner());
        assertEquals("Name", getResult1.getName());
        assertEquals("index", getResult1.getObjectType());
        assertEquals(1, ((DBUniqueConstraint) getResult).getColumnNames().length);
        assertSame(dbTable, getResult.getTable());
        assertEquals("Name", getResult.getName());
        assertEquals("unique constraint", getResult.getObjectType());
    }

    @Test
    public void testIdxReceiverReceiveIndex2() {
        DBTable.IdxReceiver idxReceiver = (new DBTable("Name")).new IdxReceiver();
        DBIndexInfo indexInfo = new DBIndexInfo("Name", "Table Name", (short) 1, "Catalog Name", false, (short) 1,
                "Column Name", true, 1, 1, "Filter Condition");
        DBTable dbTable = new DBTable("Name");
        idxReceiver.receiveIndex(indexInfo, true, dbTable, new DBSchema("Name"));
        DBTableComponent getResult = dbTable.getComponents().get(0);
        assertEquals(0, dbTable.getColumnNames().length);
        boolean actualIsNameDeterministicResult = ((DBNonUniqueIndex) getResult).isNameDeterministic();
        assertEquals("index", getResult.getObjectType());
        assertTrue(actualIsNameDeterministicResult);
        assertSame(dbTable, getResult.getTable());
        assertEquals("Name (Column Name)", getResult.toString());
        assertEquals("Name", getResult.getName());
    }

    @Test
    public void testIdxReceiverReceiveIndex3() {
        DBTable.IdxReceiver idxReceiver = (new DBTable("Name")).new IdxReceiver();
        DBIndexInfo indexInfo = new DBIndexInfo("Name", "Table Name", (short) 1, "Catalog Name", true, (short) 1,
                "Column Name", true, 1, 1, "Filter Condition");
        DBTable dbTable = new DBTable("Name", TableType.TABLE, new DBSchema("Name"));
        idxReceiver.receiveIndex(indexInfo, true, dbTable, new DBSchema("Name"));
        List<DBTableComponent> components = dbTable.getComponents();
        DBTableComponent getResult = components.get(0);
        DBTableComponent getResult1 = components.get(1);
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
        assertSame(dbTable, getResult.getOwner());
        assertEquals("Name", getResult.getName());
        assertEquals("unique constraint", getResult.getObjectType());
        assertSame(dbTable, getResult1.getOwner());
        assertEquals("Name", getResult1.getName());
        assertEquals("index", getResult1.getObjectType());
        assertEquals(1, ((DBUniqueIndex) getResult1).getColumnNames().length);
        assertTrue(((DBUniqueIndex) getResult1).isNameDeterministic());
    }

    @Test
    public void testIdxReceiverReceiveIndex4() {
        DBTable.IdxReceiver idxReceiver = (new DBTable("Name")).new IdxReceiver();
        DBIndexInfo indexInfo = new DBIndexInfo("Name", "Table Name", (short) 1, "Catalog Name", true, (short) 1,
                "Column Name", true, 1, 1, "Filter Condition");
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        idxReceiver.receiveIndex(indexInfo, true, dbTable, new DBSchema("Name"));
        List<DBTableComponent> components = dbTable.getComponents();
        DBTableComponent getResult = components.get(0);
        DBTableComponent getResult1 = components.get(1);
        boolean actualIsNameDeterministicResult = ((DBUniqueConstraint) getResult).isNameDeterministic();
        assertTrue(actualIsNameDeterministicResult);
        assertTrue(((DBUniqueIndex) getResult1).isNameDeterministic());
        assertEquals(1, ((DBUniqueIndex) getResult1).getColumnNames().length);
        assertSame(dbTable, getResult1.getOwner());
        assertEquals("Name", getResult1.getName());
        assertEquals("index", getResult1.getObjectType());
        assertEquals(1, ((DBUniqueConstraint) getResult).getColumnNames().length);
        assertSame(dbTable, getResult.getTable());
        assertEquals("Name", getResult.getName());
        assertEquals("unique constraint", getResult.getObjectType());
    }

    @Test
    public void testIdxReceiverReceiveIndex5() {
        DBTable.IdxReceiver idxReceiver = (new DBTable("Name")).new IdxReceiver();
        DBIndexInfo indexInfo = new DBIndexInfo("Name", "Table Name", (short) 1, "Catalog Name", false, (short) 1,
                "Column Name", true, 1, 1, "Filter Condition");
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        idxReceiver.receiveIndex(indexInfo, true, dbTable, new DBSchema("Name"));
        DBTableComponent getResult = dbTable.getComponents().get(0);
        boolean actualIsNameDeterministicResult = ((DBNonUniqueIndex) getResult).isNameDeterministic();
        assertEquals("index", getResult.getObjectType());
        assertTrue(actualIsNameDeterministicResult);
        assertSame(dbTable, getResult.getTable());
        assertEquals("Name (Column Name)", getResult.toString());
        assertEquals("Name", getResult.getName());
    }

    @Test
    public void testSetSchema() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setSchema(new DBSchema("Name"));
        assertNull(dbTable.getCatalog());
    }

    @Test
    public void testGetColumnNames() {
        assertEquals(0, (new DBTable("Name")).getColumnNames().length);
    }

    @Test
    public void testGetColumnNames2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetColumns() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getColumns().isEmpty());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetColumns2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertTrue(dbTable.getColumns().isEmpty());
    }

    @Test
    public void testGetColumns3() {
        thrown.expect(ObjectNotFoundException.class);
        (new DBTable("Name")).getColumns(new String[]{"foo", "foo", "foo"});
    }

    @Test
    public void testGetColumns4() {
        DBTable dbTable = new DBTable("Name");
        assertEquals(0, dbTable.getColumns(new String[]{}).length);
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testAddColumn() {
        DBTable dbTable = new DBTable("Name");
        DBTable table = new DBTable("Name");
        DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        dbTable.addColumn(dbColumn);
        assertFalse(dbColumn.isPKComponent());
        assertEquals(1, ((DBTable) dbColumn.getOwner()).getColumnNames().length);
    }

    @Test
    public void testColReceiverReceiveColumn() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        DBDataType dataType = DBDataType.getInstance("BLOB");
        DBTable dbTable1 = new DBTable("Name");
        (dbTable.new ColReceiver()).receiveColumn("Column Name", dataType, 3, 1, true, "Default Value", "Comment",
                dbTable1);
        DBTableComponent getResult = dbTable1.getComponents().get(0);
        assertEquals(1, dbTable1.getColumnNames().length);
        assertFalse(((DBColumn) getResult).isPKComponent());
        assertEquals(3, ((DBColumn) getResult).getSize().intValue());
        assertEquals("column", getResult.getObjectType());
        assertTrue(((DBColumn) getResult).isNullable());
        assertEquals("Default Value", ((DBColumn) getResult).getDefaultValue());
        assertEquals("Comment", getResult.getDoc());
        assertFalse(((DBColumn) getResult).isVersionColumn());
        assertEquals(1, ((DBColumn) getResult).getFractionDigits().intValue());
        assertFalse(((DBColumn) getResult).isUnique());
        assertEquals("Column Name", getResult.getName());
        assertEquals("Column Name : BLOB", getResult.toString());
    }

    @Test
    public void testReceiveColumn() {
        DBTable dbTable = new DBTable("Name");
        DBTable table = new DBTable("Name");
        DBColumn dbColumn = new DBColumn("Name", table, DBDataType.getInstance("BLOB"));
        dbTable.receiveColumn(dbColumn);
        assertFalse(dbColumn.isPKComponent());
        assertEquals(1, ((DBTable) dbColumn.getOwner()).getColumnNames().length);
    }

    @Test
    public void testAreColumnsImported() {
        assertFalse((new DBTable("Name")).areColumnsImported());
    }

    @Test
    public void testAreColumnsImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertTrue(dbTable.areColumnsImported());
    }

    @Test
    public void testSetColumnsImported() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testSetColumnsImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(false);
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testHaveColumnsImported() {
        DBTable dbTable = new DBTable("Name");
        dbTable.haveColumnsImported();
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testSetPrimaryKey() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertTrue(dbTable.isPKImported());
        assertEquals(3, dbTable.getPKColumnNames().length);
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testSetPrimaryKey2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertTrue(dbTable.isPKImported());
        assertEquals(3, dbTable.getPKColumnNames().length);
    }

    @Test
    public void testSetPrimaryKey3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPKImported(true);
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(3, dbTable.getPKColumnNames().length);
    }

    @Test
    public void testGetPrimaryKeyConstraint() {
        DBTable dbTable = new DBTable("Name");
        assertNull(dbTable.getPrimaryKeyConstraint());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetPrimaryKeyConstraint2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertNull(dbTable.getPrimaryKeyConstraint());
        assertTrue(dbTable.isPKImported());
    }

    @Test
    public void testGetPrimaryKeyConstraint3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        assertNull(dbTable.getPrimaryKeyConstraint());
    }

    @Test
    public void testGetPKColumnNames() {
        DBTable dbTable = new DBTable("Name");
        assertEquals(0, dbTable.getPKColumnNames().length);
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetPKColumnNames2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertEquals(0, dbTable.getPKColumnNames().length);
        assertTrue(dbTable.isPKImported());
    }

    @Test
    public void testGetPKColumnNames3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        assertEquals(0, dbTable.getPKColumnNames().length);
    }

    @Test
    public void testGetPKColumnNames4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(3, dbTable.getPKColumnNames().length);
    }

    @Test
    public void testSetPKImported() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPKImported(true);
        assertTrue(dbTable.isPKImported());
    }

    @Test
    public void testHavePKImported() {
        DBTable dbTable = new DBTable("Name");
        dbTable.havePKImported();
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testHavePKImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        dbTable.havePKImported();
        assertTrue(dbTable.isPKImported());
    }

    @Test
    public void testGetUniqueConstraints() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getUniqueConstraints(true).isEmpty());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetUniqueConstraints2() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getUniqueConstraints(false).isEmpty());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetUniqueConstraints3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertTrue(dbTable.getUniqueConstraints(true).isEmpty());
    }

    @Test
    public void testGetUniqueConstraints4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertTrue(dbTable.getUniqueConstraints(true).isEmpty());
    }

    @Test
    public void testGetUniqueConstraints5() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addUniqueConstraint(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(1, dbTable.getUniqueConstraints(true).size());
    }

    @Test
    public void testGetUniqueConstraints6() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(1, dbTable.getUniqueConstraints(true).size());
    }

    @Test
    public void testGetUniqueConstraints7() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(null, "Name", true, "foo", "foo", "foo"));
        assertEquals(1, dbTable.getUniqueConstraints(true).size());
    }

    @Test
    public void testGetUniqueConstraint() {
        DBTable dbTable = new DBTable("Name");
        DBPrimaryKeyConstraint dbPrimaryKeyConstraint = new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo",
                "foo", "foo");
        dbTable.setPrimaryKey(dbPrimaryKeyConstraint);
        assertSame(dbPrimaryKeyConstraint, dbTable.getUniqueConstraint("Name"));
    }

    @Test
    public void testGetUniqueConstraint2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(new DBPrimaryKeyConstraint(new DBTable("Name"), null, true, "foo", "foo", "foo"));
        assertNull(dbTable.getUniqueConstraint("Name"));
    }

    @Test
    public void testGetUniqueConstraint3() {
        DBTable dbTable = new DBTable("Name");
        assertNull(dbTable.getUniqueConstraint(new String[]{"foo", "foo", "foo"}));
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetUniqueConstraint4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertNull(dbTable.getUniqueConstraint(new String[]{"foo", "foo", "foo"}));
    }

    @Test
    public void testAddUniqueConstraint() {
        DBTable dbTable = new DBTable("Name");
        DBUniqueConstraint dbUniqueConstraint = new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo",
                "foo");
        dbTable.addUniqueConstraint(dbUniqueConstraint);
        CompositeDBObject<?> expectedTable = dbUniqueConstraint.owner;
        DBTable table = dbUniqueConstraint.getTable();
        assertSame(expectedTable, table);
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testAddUniqueConstraint2() {
        DBTable dbTable = new DBTable("Name");
        DBPrimaryKeyConstraint dbPrimaryKeyConstraint = new DBPrimaryKeyConstraint(new DBTable("Name"), "Name", true, "foo",
                "foo", "foo");
        dbTable.addUniqueConstraint(dbPrimaryKeyConstraint);
        CompositeDBObject<?> expectedTable = dbPrimaryKeyConstraint.owner;
        DBTable table = dbPrimaryKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertTrue(table.isPKImported());
        assertEquals(3, table.getPKColumnNames().length);
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testRemoveUniqueConstraint() {
        DBTable dbTable = new DBTable("Name");
        dbTable.removeUniqueConstraint(new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetIndexes() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getIndexes().isEmpty());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetIndexes2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertTrue(dbTable.getIndexes().isEmpty());
    }

    @Test
    public void testGetIndexes3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertEquals(1, dbTable.getIndexes().size());
    }

    @Test
    public void testGetIndex() {
        DBTable dbTable = new DBTable("Name");
        assertNull(dbTable.getIndex("Index Name"));
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetIndex2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setColumnsImported(true);
        assertNull(dbTable.getIndex("Index Name"));
    }

    @Test
    public void testGetIndex3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertNull(dbTable.getIndex("Index Name"));
    }

    @Test
    public void testAddIndex() {
        DBTable dbTable = new DBTable("Name");
        DBUniqueIndex dbUniqueIndex = new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo"));
        dbTable.addIndex(dbUniqueIndex);
        CompositeDBObject<?> expectedOwner = dbUniqueIndex.owner;
        CompositeDBObject<?> owner = dbUniqueIndex.getOwner();
        assertSame(expectedOwner, owner);
        assertEquals(0, ((DBTable) owner).getColumnNames().length);
    }

    @Test
    public void testRemoveIndex() {
        DBTable dbTable = new DBTable("Name");
        dbTable.removeIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testAreIndexesImported() {
        assertFalse((new DBTable("Name")).areIndexesImported());
    }

    @Test
    public void testAreIndexesImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addIndex(new DBUniqueIndex("Name", true,
                new DBUniqueConstraint(new DBTable("Name"), "Name", true, "foo", "foo", "foo")));
        assertTrue(dbTable.areIndexesImported());
    }

    @Test
    public void testGetForeignKeyConstraints() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getForeignKeyConstraints().isEmpty());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetForeignKeyConstraints2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        assertTrue(dbTable.getForeignKeyConstraints().isEmpty());
    }

    @Test
    public void testGetForeignKeyConstraints3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        assertTrue(dbTable.getForeignKeyConstraints().isEmpty());
    }

    @Test
    public void testGetForeignKeyConstraints4() {
        DBTable dbTable = new DBTable("Name");
        DBTable owner = new DBTable("Name");
        dbTable.addForeignKey(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
        assertEquals(1, dbTable.getForeignKeyConstraints().size());
    }

    @Test
    public void testAddForeignKey() {
        DBTable dbTable = new DBTable("Name");
        DBTable owner = new DBTable("Name");
        DBForeignKeyConstraint dbForeignKeyConstraint = new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name",
                new DBTable("Name"), "Referee Column Name");
        dbTable.addForeignKey(dbForeignKeyConstraint);
        CompositeDBObject<?> expectedTable = dbForeignKeyConstraint.owner;
        DBTable table = dbForeignKeyConstraint.getTable();
        assertSame(expectedTable, table);
        assertTrue(table.isPKImported());
        assertEquals(0, table.getColumnNames().length);
    }

    @Test
    public void testRemoveForeignKeyConstraint() {
        DBTable dbTable = new DBTable("Name");
        dbTable.removeForeignKeyConstraint(null);
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testAreFKsImported() {
        assertFalse((new DBTable("Name")).areFKsImported());
    }

    @Test
    public void testAreFKsImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        assertTrue(dbTable.areFKsImported());
    }

    @Test
    public void testGetCheckConstraints() {
        assertTrue((new DBTable("Name", TableType.TABLE,
                new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())))).getCheckConstraints()
                .isEmpty());
    }

    @Test
    public void testGetCheckConstraints2() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setChecksImported(true);
        assertTrue(
                (new DBTable("Name", TableType.TABLE, new DBSchema("Name", new DBCatalog("Name", createTestModelResult))))
                        .getCheckConstraints()
                        .isEmpty());
    }

    @Test
    public void testGetCheckConstraints3() {
        DBTable dbTable = new DBTable("Name", TableType.TABLE,
                new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())));
        dbTable.receiveCheckConstraint(null);
        assertEquals(1, dbTable.getCheckConstraints().size());
    }

    @Test
    public void testAreChecksImported() {
        assertFalse((new DBTable("Name", TableType.TABLE,
                new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())))).areChecksImported());
    }

    @Test
    public void testAreChecksImported2() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setChecksImported(true);
        assertTrue(
                (new DBTable("Name", TableType.TABLE, new DBSchema("Name", new DBCatalog("Name", createTestModelResult))))
                        .areChecksImported());
    }

    @Test
    public void testGetReferrers() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.getReferrers().isEmpty());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testGetReferrers2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.receiveReferrer(null);
        assertEquals(1, dbTable.getReferrers().size());
    }

    @Test
    public void testGetReferrers3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        assertTrue(dbTable.getReferrers().isEmpty());
    }

    @Test
    public void testGetReferrers4() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        assertTrue(dbTable.getReferrers().isEmpty());
    }

    @Test
    public void testGetReferrers5() {
        DBTable dbTable = new DBTable("Name");
        dbTable.receiveReferrer(new DBTable("Name"));
        assertEquals(1, dbTable.getReferrers().size());
    }

    @Test
    public void testAddReferrer() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addReferrer(new DBTable("Name"));
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testAddReferrer2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.addReferrer(null);
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testAreReferrersImported() {
        assertTrue((new DBTable("Name")).areReferrersImported());
    }

    @Test
    public void testAreReferrersImported2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.receiveReferrer(null);
        assertFalse(dbTable.areReferrersImported());
    }

    @Test
    public void testCountProviders() {
        DBTable dbTable = new DBTable("Name");
        assertEquals(0, dbTable.countProviders());
        assertTrue(dbTable.isPKImported());
        assertEquals(0, dbTable.getColumnNames().length);
    }

    @Test
    public void testCountProviders2() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setFKsImported(true);
        assertEquals(0, dbTable.countProviders());
    }

    @Test
    public void testCountProviders3() {
        DBTable dbTable = new DBTable("Name");
        dbTable.setPrimaryKey(null);
        assertEquals(0, dbTable.countProviders());
    }

    @Test
    public void testCountProviders4() {
        DBTable dbTable = new DBTable("Name");
        DBTable owner = new DBTable("Name");
        dbTable.addForeignKey(
                new DBForeignKeyConstraint("Name", true, owner, "Fk Column Name", new DBTable("Name"), "Referee Column Name"));
        assertEquals(1, dbTable.countProviders());
    }

    @Test
    public void testHashCode() {
        assertEquals(2420395, (new DBTable("Name")).hashCode());
    }

    @Test
    public void testEquals() {
        assertFalse((new DBTable("Name")).equals("other"));
        assertFalse((new DBTable("Name")).equals(null));
    }

    @Test
    public void testEquals2() {
        DBTable dbTable = new DBTable("Name");
        assertTrue(dbTable.equals(new DBTable("Name")));
    }

    @Test
    public void testEquals3() {
        DBTable dbTable = new DBTable("Name");
        assertFalse(dbTable.equals(new DBTable("Name", TableType.TABLE, new DBSchema("Name"))));
    }
}

