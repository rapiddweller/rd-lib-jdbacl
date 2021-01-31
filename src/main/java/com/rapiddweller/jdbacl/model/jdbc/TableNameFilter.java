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

package com.rapiddweller.jdbacl.model.jdbc;

import com.rapiddweller.common.Filter;

import java.util.regex.Pattern;

/**
 * Filters.<br/><br/>
 * Created: 29.01.2012 22:08:43
 * @since 0.8.0
 * @author Volker Bergmann
 */
public class TableNameFilter implements Filter<String> {
	
	private final Pattern tableInclusionPattern;
	private final Pattern tableExclusionPattern;

	public TableNameFilter(String tableInclusionPattern, String tableExclusionPattern) {
		this.tableInclusionPattern = Pattern.compile(tableInclusionPattern != null ? tableInclusionPattern : ".*");
		this.tableExclusionPattern = (tableExclusionPattern != null ? Pattern.compile(tableExclusionPattern) : null);
	}

	@Override
	public boolean accept(String tableName) {
		if (tableName.contains("$") || (tableExclusionPattern != null && tableExclusionPattern.matcher(tableName).matches()))
			return false;
	    return (tableInclusionPattern == null || tableInclusionPattern.matcher(tableName).matches());
    }
	
}