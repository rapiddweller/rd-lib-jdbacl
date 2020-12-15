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

import static org.junit.Assert.*;

import java.util.Arrays;

import com.rapiddweller.commons.ArrayFormat;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.ForeignKeyPath;
import org.junit.Test;

/**
 * Tests the {@link SQLUtil} class.<br/><br/>
 * Created: 28.11.2010 10:27:54
 * @since 0.6.4
 * @author Volker Bergmann
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
	public void testParseColumnTypeAndSize() {
		checkParsing("int", "int" );
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
	}
	
	@Test
	public void testRemoveComments() {
		assertEquals("select a from b", SQLUtil.removeComments("select a from b"));
		assertEquals("select a from b", SQLUtil.removeComments("select a/*, x, y */ from b"));
		assertEquals("select a from b", SQLUtil.removeComments("select a /*, x, y */from b/* join c on ref=id*/"));
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
	}
	
	@Test
	public void testRenderColumnListWithTableName() {
		assertEquals("t.x, t.y", SQLUtil.renderColumnListWithTableName("t", "x", "y"));
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
		assertEquals("c IS NULL", SQLUtil.allNull(new String[] {"c"}, null));
		assertEquals("c1 IS NULL AND c2 IS NULL", SQLUtil.allNull(new String[] {"c1", "c2"}, null));
		assertEquals("t.c IS NULL", SQLUtil.allNull(new String[] {"c"}, "t"));
		assertEquals("t.c1 IS NULL AND t.c2 IS NULL", SQLUtil.allNull(new String[] {"c1", "c2"}, "t"));
	}
	
	// helpers ---------------------------------------------------------------------------------------------------------

	public void checkParsing(String spec, Object... expected) {
		Object[] actual = SQLUtil.parseColumnTypeAndSize(spec);
		String message = "Expected: [" + ArrayFormat.format(expected) + "], " +
				"found: [" + ArrayFormat.format(actual) + "]";
		assertTrue(message, Arrays.deepEquals(expected, actual));
	}
	
}
