/*
 * (c) Copyright 2008-2011 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
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

import java.sql.Connection;
import java.util.Arrays;

import com.rapiddweller.commons.ArrayUtil;
import com.rapiddweller.commons.Encodings;
import com.rapiddweller.commons.ErrorHandler;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;

import org.junit.Test;
import static junit.framework.Assert.*;

/**
 * Tests the {@link DBUtil} class.<br/><br/>
 * Created at 03.05.2008 15:53:49
 * @since 0.5.3
 * @author Volker Bergmann
 */
public class DBUtilTest {

	final String SCRIPT_FILE = "com/rapiddweller/jdbacl/create_tables.hsql.sql";

	@Test
	public void testRunScript() throws Exception {
		Connection connection = HSQLUtil.connectInMemoryDB(getClass().getSimpleName());
		ErrorHandler errorHandler = new ErrorHandler(getClass());
		DBExecutionResult result = DBUtil.executeScriptFile(SCRIPT_FILE, Encodings.ISO_8859_1, connection, true, errorHandler);
		assertTrue(result.changedStructure);
		Object[][] rows = (Object[][]) DBUtil.queryAndSimplify("select * from T1", connection);
		assertEquals(1, rows.length);
		assertTrue(Arrays.equals(ArrayUtil.buildObjectArrayOfType(Object.class, 1, "R&B"), rows[0]));
		int count = (Integer) DBUtil.queryAndSimplify("select count(*) from T1", connection);
		assertEquals(1, count);
	}
	
	@Test
	public void testConnectionCount() throws Exception {
		DBUtil.resetMonitors();
		assertEquals(0, DBUtil.getOpenConnectionCount());
		Connection connection = HSQLUtil.connectInMemoryDB(getClass().getSimpleName());
		assertEquals(1, DBUtil.getOpenConnectionCount());
		connection.close();
		assertEquals(0, DBUtil.getOpenConnectionCount());
	}
	
	// testing checkReadOnly() -----------------------------------------------------------------------------------------

	@Test
	public void testReadOnly_false() {
		DBUtil.checkReadOnly("insert into xyz (id) values (3)", false);
		DBUtil.checkReadOnly("update xyz set id = 3", false);
		DBUtil.checkReadOnly("select * from xyz", false);
		DBUtil.checkReadOnly("select into xyz2 from xyz", false);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testReadOnly_true_insert() {
		DBUtil.checkReadOnly("insert into xyz (id) values (3)", true);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testReadOnly_true_update() {
		DBUtil.checkReadOnly("update xyz set id = 3", true);
	}
	
	@Test
	public void testReadOnly_true_select() {
		DBUtil.checkReadOnly("select * from xyz", true);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testReadOnly_true_select_into() {
		DBUtil.checkReadOnly("select into xyz2 from xyz", true);
	}

	@Test
	public void testReadOnly_alter_session() {
		DBUtil.checkReadOnly("ALTER SESSION SET NLS_LENGTH_SEMANTICS=CHAR", true);
	}
	
}
