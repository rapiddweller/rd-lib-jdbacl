/*
 * (c) Copyright 2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.CollectionUtil;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link DBCheckConstraint}.<br/><br/>
 * Created: 08.06.2011 14:04:49
 *
 * @author Volker Bergmann
 * @since 0.1
 */
public class DBCheckConstraintTest {

  /**
   * Test get column names.
   */
  @Test
  public void testGetColumnNames() {
    check("\"col1\" is null", "col1");
    check("col1 is null", "col1");
    check("col1 is null or col2 is not null", "col1", "col2");
    check("col1 is not null and length(col2) >= 3", "col1", "col2");
    check("((col1 in ('X', 'Y') and col2 is not null) or (col1='Z' and col3 is not null))", "col1", "col2", "col3");
    check("col1 not in ('a', 'b', 'c', 'd')", "col1");
    check("(col1 in ('a', 'b', 'c', 'd') and col2 ='d') or (col1 not in ('a', 'b', 'c', 'd') and col2 is not null)", "col1", "col2");
    check("col1=1 and (col2 IS not null and (col3 is not null or col3 is not null or col4 is not null)) or col5=0", "col1", "col2", "col3", "col4",
        "col5");
  }

  private static void check(String condition, String... expectedColumnNames) {
    DBCheckConstraint constraint = new DBCheckConstraint("ck", false, "tbl", condition);
    Set<String> expectedSet = CollectionUtil.toSet(expectedColumnNames);
    Set<String> actualSet = CollectionUtil.toSet(constraint.getColumnNames());
    assertEquals(expectedSet, actualSet);
  }

}
