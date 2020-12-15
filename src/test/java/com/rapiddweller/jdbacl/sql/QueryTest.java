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

import org.junit.Test;

/**
 * Tests the {@link Query} class.<br/><br/>
 * Created: 09.04.2012 10:31:13
 * @since 0.8.1
 * @author Volker Bergmann
 */
public class QueryTest {

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
		Query query = Query.select("col").from("left", "left__").leftJoin("left__", new String[] { "l1", "l2" }, "right", "right__", new String[] {"r1", "r2"});
		assertEquals("SELECT col FROM left left__ LEFT JOIN right right__ ON left__.l1 = right__.r1 AND left__.l2 = right__.r2", query.toString());
	}
	
}
