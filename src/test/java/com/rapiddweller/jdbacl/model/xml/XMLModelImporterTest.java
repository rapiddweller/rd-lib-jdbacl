/*
 * (c) Copyright 2010-2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.xml;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.List;

import com.rapiddweller.commons.IOUtil;
import com.rapiddweller.commons.tree.TreeLogger;
import com.rapiddweller.jdbacl.model.AbstractModelTest;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTreeModel;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the {@link XMLModelImporter}.<br/><br/>
 * Created: 28.11.2010 18:23:12
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class XMLModelImporterTest extends AbstractModelTest {

	@Before
	public void setUpTables() throws Exception {
		createTables();
	}
	
	@After
	public void tearDownTables() throws Exception {
		dropTables();
	}
	
	@Test
	public void testOffline() throws Exception {
		XMLModelImporter importer = new XMLModelImporter(EAGER_TEST_MODEL_FILENAME, null);
		try {
			Database actual = importer.importDatabase();
			assertTrue(actual instanceof Database);
			new TreeLogger().log(new DBTreeModel(actual));
			Database expected = createTestModel(false);
			assertTrue(expected.isIdentical(actual));
		} finally {
			IOUtil.close(importer);
		}
	}
	
	@Test
	public void testOnline() throws Exception {
		XMLModelImporter importer = new XMLModelImporter(LAZY_TEST_MODEL_FILENAME, new JDBCDBImporter(ENVIRONMENT));
		try {
			Database db = importer.importDatabase();
			new TreeLogger().log(new DBTreeModel(db));
			assertFalse(db.isSequencesImported());
			List<DBSequence> sequences = db.getSequences();
			assertEquals(1, sequences.size());
			assertEquals("SEQ1", sequences.get(0).getName());
			assertEquals(BigInteger.valueOf(1000), sequences.get(0).getStart());
			assertTrue(db.isSequencesImported());
		} finally {
			IOUtil.close(importer);
		}
	}
	
}
