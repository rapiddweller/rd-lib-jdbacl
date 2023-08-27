/*
 * (c) Copyright 2011-2022 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ExceptionUtil;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.format.DataIterator;
import com.rapiddweller.format.util.DataIteratorProxy;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * {@link DataIterator} which provides the results of a SQL query.<br/><br/>
 * Created: 03.08.2011 19:25:54
 * @author Volker Bergmann
 * @since 0.6.10
 */
public class QueryDataIterator extends DataIteratorProxy<ResultSet> {

  private String query;

  public QueryDataIterator(String query, Connection connection, int fetchSize) {
    super(createSource(query, connection, fetchSize));
    this.query = query;
  }

  private static DataIterator<ResultSet> createSource(String query, Connection connection, int fetchSize) {
    try {
      int _resultSetHoldability = connection.getMetaData().getResultSetHoldability();
      Statement statement = connection.createStatement(
          ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, _resultSetHoldability);
      statement.setFetchSize(fetchSize);
      ResultSet resultSet = statement.executeQuery(query);
      return new ResultSetDataIterator(resultSet, query);
    } catch (Exception e) {
      throw handleException(query, e);
    }
  }

  private static RuntimeException handleException(String query, Exception e) {
    Throwable root = ExceptionUtil.getRootCause(e);
    String message;
    if (root instanceof SQLException) {
      message = "Error in database query: '" + query + "'";
    } else {
      message = "Error in query: '" + query + "'";
    }
    message += ": " + root.getMessage();
    return ExceptionFactory.getInstance().dbQueryFailed(message, e);
  }

  public String[] getColumnLabels() {
    return ((ResultSetIterator) source).getColumnLabels();
  }

  @Override
  public String toString() {
    return "QueryDataIterator{" + query + '}';
  }

}
