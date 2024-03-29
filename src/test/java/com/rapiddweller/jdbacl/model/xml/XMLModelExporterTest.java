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

package com.rapiddweller.jdbacl.model.xml;

import com.rapiddweller.common.IOUtil;
import com.rapiddweller.jdbacl.model.AbstractModelTest;
import com.rapiddweller.jdbacl.model.Database;
import org.junit.After;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertArrayEquals;

/**
 * Tests the {@link XMLModelExporter}.<br/><br/>
 * Created: 28.11.2010 09:55:52
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class XMLModelExporterTest extends AbstractModelTest {

  @After
  public void cleanUp() throws Exception {
    try {
      dropTables();
    } catch (Exception e) {
      // this may happen if a former test broke
    }
  }

  @Test
  public void testLazy() throws Exception {
    Database db = createTestModel();
    File file = new File("target", getClass().getSimpleName() + ".xml");
    new XMLModelExporter(file).export(db);
    String[] expectedLines = IOUtil.readTextLines(LAZY_TEST_MODEL_FILENAME, false);
    String[] actualLines = IOUtil.readTextLines(file.getCanonicalPath(), false);
    assertArrayEquals(expectedLines, actualLines);
  }

  @Test
  public void testEager() throws Exception {
    Database db = createTestModel();
    File file = new File("target", getClass().getSimpleName() + ".xml");
    new XMLModelExporter(file, false).export(db);
    String[] expectedLines = IOUtil.readTextLines(EAGER_TEST_MODEL_FILENAME, false);
    String[] actualLines = IOUtil.readTextLines(file.getCanonicalPath(), false);
    assertArrayEquals(expectedLines, actualLines);
  }

}
