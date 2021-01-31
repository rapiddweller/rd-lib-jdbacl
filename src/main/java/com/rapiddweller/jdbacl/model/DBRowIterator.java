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

package com.rapiddweller.jdbacl.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.rapiddweller.common.HeavyweightIterator;
import com.rapiddweller.common.LogCategoriesConstants;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.ResultSetIterator;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Iterates through the rows of a database.<br/><br/>
 * Created: 23.07.2010 07:29:47
 * @since 0.6.3
 * @author Volker Bergmann
 */
public class DBRowIterator implements HeavyweightIterator<DBRow> {
	
    private static final Logger SQL_LOGGER = LogManager.getLogger(LogCategoriesConstants.SQL);

    private DBTable table;
    private ResultSet resultSet;
    private final ResultSetMetaData resultSetMetaData;
    private final ResultSetIterator resultSetIterator;
    private boolean closed;

	public DBRowIterator(DBTable table, Connection connection, String whereClause) throws SQLException {
		this.table = table;
	    String sql = "SELECT * FROM " + table.getName();
	    if (whereClause != null)
	    	sql += " WHERE " + whereClause;
        SQL_LOGGER.debug(sql);
        Statement statement = connection.createStatement(
        		ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
        statement.setFetchSize(1000);
        this.resultSet = statement.executeQuery(sql);
        this.resultSetMetaData = resultSet.getMetaData();
	    this.resultSetIterator = new ResultSetIterator(resultSet, sql);
	    this.closed = false;
    }
	
	public DBRowIterator withTable(DBTable table) {
		this.table = table;
		return this;
	}

	@Override
	public boolean hasNext() {
		if (closed)
			return false;
		boolean result = resultSetIterator.hasNext();
		if (!result)
			close();
		return result;
	}

	@Override
	public DBRow next() {
		try {
			resultSetIterator.next();
	        DBRow row = new DBRow(table);
	        int columnCount = resultSetMetaData.getColumnCount();
	        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
	            String columnName = resultSetMetaData.getColumnName(columnIndex);
	            row.setCellValue(columnName, resultSet.getObject(columnIndex));
	        }
	        return row;
        } catch (SQLException e) {
	        throw new RuntimeException("Error querying table " + table, e);
        }
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("remove() is not supported by " + getClass());
	}

	@Override
	public void close() {
		if (!closed) {
			DBUtil.closeResultSetAndStatement(resultSet);
			resultSet = null;
			closed = true;
		}
	}

}
