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

package com.rapiddweller.jdbacl.sql;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests the {@link Query} class.<br/><br/>
 * Created: 09.04.2012 10:31:13
 *
 * @author Volker Bergmann
 * @since 0.8.1
 */
public class QueryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testConstructor() {
        assertEquals("SELECT Selection FROM Table", (new Query("Selection", "Table")).toString());
        assertEquals("SELECT Selection FROM ", (new Query("Selection", null)).toString());
        assertEquals("SELECT Selection FROM Table WHERE Where Clause",
                (new Query("Selection", "Table", "Where Clause")).toString());
        assertEquals("SELECT Selection FROM  WHERE Where Clause",
                (new Query("Selection", null, "Where Clause")).toString());
        assertEquals("SELECT Selection FROM Table", (new Query("Selection", "Table", null)).toString());
    }

    @Test
    public void testSelect() {
        assertEquals("SELECT Selection FROM ", Query.select("Selection").toString());
    }

    @Test
    public void testAddSelectCondition() {
        Query selectResult = Query.select("Selection");
        selectResult.addSelectCondition("Select Condition");
        assertEquals("SELECT Select Condition Selection FROM ", selectResult.toString());
    }

    @Test
    public void testFrom() {
        thrown.expect(IllegalArgumentException.class);
        Query.select("Selection").from("Table Name");
    }

    @Test
    public void testFrom2() {
        assertEquals("SELECT Selection FROM ", Query.select("Selection").from("").toString());
    }

    @Test
    public void testFrom3() {
        thrown.expect(IllegalArgumentException.class);
        Query.select("Selection").from("Table Name", "Alias");
    }

    @Test
    public void testFrom4() {
        assertEquals("SELECT Selection FROM  Alias", Query.select("Selection").from("", "Alias").toString());
    }

    @Test
    public void testFrom5() {
        assertEquals("SELECT Selection FROM ", Query.select("Selection").from("", null).toString());
    }

    @Test
    public void testFrom6() {
        thrown.expect(IllegalArgumentException.class);
        Query.select("Selection").from("Table Name", null);
    }

    @Test
    public void testCountStar() {
        Query query = new Query("COUNT(*)", "TEST");
        assertEquals("SELECT COUNT(*) FROM TEST", query.toString());
    }

    @Test
    public void testLiterate() {
        Query query = Query.select("COL").from("TEST").where("ID > 0");
        assertEquals("SELECT COL FROM TEST WHERE ID > 0", query.toString());
    }

    @Test
    public void testLeftJoin() {
        Query query = Query.select("col").from("left", "left__").leftJoin("left__", new String[]{"l1", "l2"}, "right", "right__", new String[]{"r1", "r2"});
        assertEquals("SELECT col FROM left left__ LEFT JOIN right right__ ON left__.l1 = right__.r1 AND left__.l2 = right__.r2", query.toString());
    }

    @Test
    public void testLeftJoin2() {
        assertEquals(
                "SELECT Selection FROM  LEFT JOIN Right Table Right Alias ON Left Alias.foo = Right Alias.foo AND Left"
                        + " Alias.foo = Right Alias.foo AND Left Alias.foo = Right Alias.foo",
                Query.select("Selection")
                        .leftJoin("Left Alias", new String[]{"foo", "foo", "foo"}, "Right Table", "Right Alias",
                                new String[]{"foo", "foo", "foo"})
                        .toString());
    }

    @Test
    public void testWhere() {
        assertEquals("SELECT Selection FROM  WHERE Where", Query.select("Selection").where("Where").toString());
    }

    @Test
    public void testWhere2() {
        thrown.expect(IllegalArgumentException.class);
        (new Query("Selection", "Table", "Where Clause")).where("Where");
    }

    @Test
    public void testAnd() {
        Query selectResult = Query.select("Selection");
        selectResult.and("Condition");
        assertEquals("SELECT Selection FROM  WHERE Condition", selectResult.toString());
    }

    @Test
    public void testAnd2() {
        Query query = new Query("Selection", "Table", "Where Clause");
        query.and("Condition");
        assertEquals("SELECT Selection FROM Table WHERE Where Clause AND Condition", query.toString());
    }

    @Test
    public void testAddOption() {
        Query selectResult = Query.select("Selection");
        selectResult.addOption("Option");
        assertEquals("SELECT Selection FROM  Option", selectResult.toString());
    }

    @Test
    public void testToString() {
        assertEquals("SELECT Selection FROM ", Query.select("Selection").toString());
        assertEquals("SELECT Selection FROM Table", (new Query("Selection", "Table")).toString());
        assertEquals("SELECT Selection FROM Table WHERE Where Clause",
                (new Query("Selection", "Table", "Where Clause")).toString());
    }

    @Test
    public void testToString2() {
        Query selectResult = Query.select("Selection");
        selectResult.addSelectCondition("SELECT ");
        assertEquals("SELECT SELECT  Selection FROM ", selectResult.toString());
    }

    @Test
    public void testToString3() {
        Query selectResult = Query.select("Selection");
        selectResult.addOption("SELECT ");
        assertEquals("SELECT Selection FROM  SELECT ", selectResult.toString());
    }

}
