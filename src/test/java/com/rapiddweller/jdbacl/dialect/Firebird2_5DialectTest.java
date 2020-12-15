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

package com.rapiddweller.jdbacl.dialect;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests the {@link Firebird2_5Dialect}.<br/><br/>
 * Created: 20.10.2011 19:54:11
 * @since 0.6.12
 * @author Volker Bergmann
 */
public class Firebird2_5DialectTest extends DatabaseDialectTest<Firebird2_5Dialect> {

	public Firebird2_5DialectTest() {
	    super(new Firebird2_5Dialect());
    }
	
	@Test
	public void testRegex() {
		assertTrue(dialect.supportsRegex());
		assertEquals("code SIMILAR TO '[A-Z]{5}'", dialect.regexQuery("code", false, "[A-Z]{5}"));
		assertEquals("code NOT SIMILAR TO '[A-Z]{5}'", dialect.regexQuery("code", true, "[A-Z]{5}"));
	}
	
}
