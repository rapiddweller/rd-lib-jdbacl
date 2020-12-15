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

package com.rapiddweller.jdbacl.dialect;

import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.sql.Query;

/**
 * Space holder for unknown databases.<br/><br/>
 * Created: 26.01.2008 07:13:59
 * @since 0.4.0
 * @author Volker Bergmann
 */
public class UnknownDialect extends DatabaseDialect {

	private static final String DATE_PATTERN = "''dd-MMM-yyyy''";
	private static final String TIME_PATTERN = "''HH-mm-ss''";
	private static final String DATETIME_PATTERN = "''dd-MMM-yyyy HH-mm-ss''";

    public UnknownDialect(String system) {
	    super(system, false, false, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
    }

	@Override
    public boolean isDefaultCatalog(String catalog, String user) {
	    return true; // hope that the first catalog found is the correct one
    }

	@Override
    public boolean isDefaultSchema(String schema, String user) {
	    return true; // hope that the first schema found is the correct one
    }

	@Override
	public boolean isDeterministicPKName(String pkName) {
		return true; // on unknown database systems, assume the name is reproducible - that's the safer choice
	}

	@Override
	public boolean isDeterministicUKName(String pkName) {
		return true; // on unknown database systems, assume the name is reproducible - that's the safer choice
	}

	@Override
	public boolean isDeterministicFKName(String pkName) {
		return true; // on unknown database systems, assume the name is reproducible - that's the safer choice
	}

	@Override
	public boolean isDeterministicIndexName(String indexName) {
		return true; // on unknown database systems, assume the name is reproducible - that's the safer choice
	}

	@Override
	public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
		throw new UnsupportedOperationException("UnknownDialect.applyRownumRestriction() is not implemented"); // TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
	}

}