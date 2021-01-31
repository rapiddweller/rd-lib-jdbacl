package com.rapiddweller.jdbacl.model;

import com.rapiddweller.common.ObjectNotFoundException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Date;

import static org.junit.Assert.*;

public class DatabaseTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testSetImportDate() {
        Date date = new Date(1L);
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setImportDate(date);
        assertSame(date, createTestModelResult.getImportDate());
    }

    @Test
    public void testSetUser() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setUser("User");
        assertEquals("User", createTestModelResult.getUser());
    }

    @Test
    public void testSetTableInclusionPattern() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setTableInclusionPattern("Table Inclusion Pattern");
        assertEquals("Table Inclusion Pattern", createTestModelResult.getTableInclusionPattern());
    }

    @Test
    public void testSetTableExclusionPattern() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setTableExclusionPattern("Table Exclusion Pattern");
        assertEquals("Table Exclusion Pattern", createTestModelResult.getTableExclusionPattern());
    }

    @Test
    public void testIsReservedWord() {
        assertFalse(AbstractModelTest.createTestModel().isReservedWord("Word"));
    }

    @Test
    public void testIsReservedWord2() {
        thrown.expect(RuntimeException.class);
        (new Database("Environment", null, true)).isReservedWord("Word");
    }

    @Test
    public void testGetComponents() {
        assertEquals(1, AbstractModelTest.createTestModel().getComponents().size());
    }

    @Test
    public void testGetCatalogs() {
        assertEquals(1, AbstractModelTest.createTestModel().getCatalogs().size());
    }

    @Test
    public void testGetCatalog() {
        assertNull(AbstractModelTest.createTestModel().getCatalog("Catalog Name"));
    }

    @Test
    public void testAddCatalog() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        DBCatalog dbCatalog = new DBCatalog();
        createTestModelResult.addCatalog(dbCatalog);
        CompositeDBObject<?> expectedOwner = dbCatalog.owner;
        assertSame(expectedOwner, dbCatalog.getOwner());
    }

    @Test
    public void testRemoveCatalog() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        DBCatalog dbCatalog = new DBCatalog();
        createTestModelResult.removeCatalog(dbCatalog);
        assertNull(dbCatalog.getOwner());
    }

    @Test
    public void testGetTables() {
        assertEquals(4, AbstractModelTest.createTestModel().getTables().size());
        assertEquals(4, AbstractModelTest.createTestModel().getTables(true).size());
        assertTrue(AbstractModelTest.createTestModel().getTables(false).isEmpty());
    }

    @Test
    public void testGetTable() {
        thrown.expect(ObjectNotFoundException.class);
        AbstractModelTest.createTestModel().getTable("Name", true);
    }

    @Test
    public void testGetTable2() {
        assertNull(AbstractModelTest.createTestModel().getTable("Name", false));
    }

    @Test
    public void testGetSequences() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        assertTrue(createTestModelResult.getSequences().isEmpty());
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testGetSequences2() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.addCatalog(new DBCatalog());
        assertTrue(createTestModelResult.getSequences().isEmpty());
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testGetSequences3() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        assertTrue(createTestModelResult.getSequences(true).isEmpty());
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testGetSequences4() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        assertTrue(createTestModelResult.getSequences(false).isEmpty());
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testGetSequences5() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.addCatalog(new DBCatalog());
        assertTrue(createTestModelResult.getSequences(true).isEmpty());
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testHaveSequencesImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.haveSequencesImported();
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testSetSequencesImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setSequencesImported(true);
        assertTrue(createTestModelResult.isSequencesImported());
    }

    @Test
    public void testGetTriggers() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        assertTrue(createTestModelResult.getTriggers().isEmpty());
        assertTrue(createTestModelResult.isTriggersImported());
    }

    @Test
    public void testHaveTriggersImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.haveTriggersImported();
        assertTrue(createTestModelResult.isTriggersImported());
    }

    @Test
    public void testSetTriggersImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setTriggersImported(true);
        assertTrue(createTestModelResult.isTriggersImported());
    }

    @Test
    public void testGetPackages() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        assertTrue(createTestModelResult.getPackages().isEmpty());
        assertTrue(createTestModelResult.isPackagesImported());
    }

    @Test
    public void testHavePackagesImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.havePackagesImported();
        assertTrue(createTestModelResult.isPackagesImported());
    }

    @Test
    public void testSetPackagesImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setPackagesImported(true);
        assertTrue(createTestModelResult.isPackagesImported());
    }

    @Test
    public void testSetChecksImported() {
        Database createTestModelResult = AbstractModelTest.createTestModel();
        createTestModelResult.setChecksImported(true);
        assertTrue(createTestModelResult.isChecksImported());
    }
}

