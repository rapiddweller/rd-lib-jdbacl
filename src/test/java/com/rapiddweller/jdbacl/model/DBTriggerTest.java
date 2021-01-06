package com.rapiddweller.jdbacl.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class DBTriggerTest {
    @Test
    public void testConstructor() {
        DBTrigger actualDbTrigger = new DBTrigger("Name", null);
        assertEquals("trigger", actualDbTrigger.getObjectType());
        assertEquals("Name", actualDbTrigger.toString());
        assertNull(actualDbTrigger.getOwner());
    }

    @Test
    public void testConstructor2() {
        DBTrigger actualDbTrigger = new DBTrigger("Name",
                new DBSchema("Name", new DBCatalog("Name", AbstractModelTest.createTestModel())));
        assertEquals("trigger", actualDbTrigger.getObjectType());
        assertEquals("Name", actualDbTrigger.toString());
        CompositeDBObject<?> expectedOwner = actualDbTrigger.owner;
        assertSame(expectedOwner, actualDbTrigger.getOwner());
    }

    @Test
    public void testSetTriggerType() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setTriggerType("Trigger Type");
        assertEquals("Trigger Type", dbTrigger.getTriggerType());
    }

    @Test
    public void testSetTriggeringEvent() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setTriggeringEvent("Triggering Event");
        assertEquals("Triggering Event", dbTrigger.getTriggeringEvent());
    }

    @Test
    public void testSetTableOwner() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setTableOwner("Table Owner");
        assertEquals("Table Owner", dbTrigger.getTableOwner());
    }

    @Test
    public void testSetBaseObjectType() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setBaseObjectType("Base Object Type");
        assertEquals("Base Object Type", dbTrigger.getBaseObjectType());
    }

    @Test
    public void testSetTableName() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setTableName("Table Name");
        assertEquals("Table Name", dbTrigger.getTableName());
    }

    @Test
    public void testSetColumnName() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setColumnName("Column Name");
        assertEquals("Column Name", dbTrigger.getColumnName());
    }

    @Test
    public void testSetReferencingNames() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setReferencingNames("Referencing Names");
        assertEquals("Referencing Names", dbTrigger.getReferencingNames());
    }

    @Test
    public void testSetWhenClause() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setWhenClause("When Clause");
        assertEquals("When Clause", dbTrigger.getWhenClause());
    }

    @Test
    public void testSetStatus() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setStatus("Status");
        assertEquals("Status", dbTrigger.getStatus());
    }

    @Test
    public void testSetDescription() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setDescription("The characteristics of someone or something");
        assertEquals("The characteristics of someone or something", dbTrigger.getDescription());
    }

    @Test
    public void testSetActionType() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setActionType("Action Type");
        assertEquals("Action Type", dbTrigger.getActionType());
    }

    @Test
    public void testSetTriggerBody() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setTriggerBody("Not all who wander are lost");
        assertEquals("Not all who wander are lost", dbTrigger.getTriggerBody());
    }

    @Test
    public void testGetPriority() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setPriority(10.0);
        assertEquals(10.0, dbTrigger.getPriority(), 0.0);
    }

    @Test
    public void testSetPriority() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setPriority(10.0);
        assertEquals(10.0, dbTrigger.getPriority(), 0.0);
    }

    @Test
    public void testSetStaticColumn() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setStaticColumn(true);
        assertTrue(dbTrigger.getStaticColumn());
    }

    @Test
    public void testSetConditionTime() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setConditionTime("Condition Time");
        assertEquals("Condition Time", dbTrigger.getConditionTime());
    }

    @Test
    public void testGetNormalizedDescription() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        dbTrigger.setDescription("The characteristics of someone or something");
        assertEquals("The characteristics of someone or something", dbTrigger.getNormalizedDescription());
    }

    @Test
    public void testIsIdentical() {
        DBTrigger dbTrigger = new DBTrigger("Name", null);
        assertFalse(dbTrigger.isIdentical(new DBCatalog()));
    }
}

