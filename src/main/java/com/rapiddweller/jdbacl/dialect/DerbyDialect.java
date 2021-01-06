/*
 * (c) Copyright 2010-2012 by Volker Bergmann. All rights reserved.
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

import java.util.regex.Pattern;

import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.sql.Query;

/**
 * Implements generic database concepts for Derby.<br/><br/>
 * Created: 26.01.2010 07:14:34
 * @since 0.6.0
 * @author Volker Bergmann
 */
public class DerbyDialect extends DatabaseDialect {

	private static final String DATE_PATTERN = "'DATE('''yyyy-MM-dd''')'";
	private static final String TIME_PATTERN = "'TIME('''HH:mm:ss''')'";
	private static final String DATETIME_PATTERN = "'TIMESTAMP('''yyyy-MM-dd HH:mm:ss''')'";

	final Pattern randomPKNamePattern = Pattern.compile("SQL[0-9A-F]{15}");
	final Pattern randomUKNamePattern = Pattern.compile("SQL[0-9A-F]{15}");
	final Pattern randomFKNamePattern = Pattern.compile("FK[0-9A-F]{15,16}");
	final Pattern randomIndexNamePattern = Pattern.compile("SQL\\d+");

    public DerbyDialect() {
	    this(false);
    }

    public DerbyDialect(boolean sequenceSupported) {
	    super("derby", true, sequenceSupported, DATE_PATTERN, TIME_PATTERN, DATETIME_PATTERN);
    }

	@Override
    public boolean isDefaultCatalog(String catalog, String user) {
	    return true;
    }

	@Override
    public boolean isDefaultSchema(String schema, String user) {
		schema = schema.toUpperCase();
	    return schema.equalsIgnoreCase("APP") || schema.equalsIgnoreCase(user);
    }

	@Override
	public boolean isDeterministicPKName(String pkName) {
		return !randomPKNamePattern.matcher(pkName).matches();
	}

	@Override
	public boolean isDeterministicUKName(String ukName) {
		return !randomUKNamePattern.matcher(ukName).matches();
	}

	@Override
	public boolean isDeterministicFKName(String fkName) {
		return !randomFKNamePattern.matcher(fkName).matches();
	}

	@Override
	public boolean isDeterministicIndexName(String indexName) {
		return !randomIndexNamePattern.matcher(indexName).matches();
	}

	@Override
	public void restrictRownums(int firstRowIndex, int rowCount, Query query) {
		throw new UnsupportedOperationException("DerbyDialect.applyRownumRestriction() is not implemented"); // TODO v0.8.2 implement DatabaseDialect.applyRownumRestriction()
	}

}
