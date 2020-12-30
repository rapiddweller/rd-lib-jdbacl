/*
 * (c) Copyright 2007-2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import com.rapiddweller.common.BeanUtil;
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.Converter;
import com.rapiddweller.common.LogCategories;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.converter.ArrayConverter;
import com.rapiddweller.common.converter.ToStringConverter;
import com.rapiddweller.common.debug.Debug;
import com.rapiddweller.common.debug.ResourceMonitor;
import com.rapiddweller.jdbacl.DBUtil;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Provides database related utility methods.<br/>
 * <br/>
 * Created: 28.06.2007 12:30:02
 * @author Volker Bergmann
 */
@SuppressWarnings("unchecked")
public class LoggingPreparedStatementHandler implements InvocationHandler {
	
    private static final Logger SQL_LOGGER = LogManager.getLogger(LogCategories.SQL);
    private static final Logger JDBC_LOGGER = LogManager.getLogger(LogCategories.JDBC);

	private static final AtomicInteger openStatementCount;
    private static ResourceMonitor openStatementMonitor;
    
    private static final Converter<Object[], String[]> toStringArrayConverter;
	private boolean closed;
    
    static {
    	openStatementCount = new AtomicInteger();
    	if (Debug.active())
    		openStatementMonitor = new ResourceMonitor();
    	ToStringConverter toStringConverter = new ToStringConverter("null");
    	toStringConverter.setCharQuote("'");
    	toStringConverter.setStringQuote("'");
    	toStringArrayConverter = new ArrayConverter<>(Object.class, String.class, toStringConverter);
    }

	private final String sql;
	private final PreparedStatement realStatement;
	Object[] params;
	
	public LoggingPreparedStatementHandler(PreparedStatement realStatement, String sql) {
		this.sql = sql;
		this.realStatement = realStatement;
		this.closed = false;
		int paramCount = StringUtil.countChars(sql, '?');
		params = new Object[paramCount];
		openStatementCount.incrementAndGet();
		if (openStatementMonitor != null)
			openStatementMonitor.register(this);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			String methodName = method.getName();
			Method localMethod = BeanUtil.findMethod(this.getClass(), methodName, method.getParameterTypes());
			if (localMethod != null)
				return BeanUtil.invoke(this, localMethod, args);
			else {
				if ("setNull".equals(methodName) && args != null && args.length >= 2)
					params[(Integer) args[0] - 1] = null;
				else if (methodName.startsWith("set") && args != null && args.length >= 2 && args[0] instanceof Integer)
					params[(Integer) args[0] - 1] = args[1];
				Object result = BeanUtil.invoke(realStatement, method, args);
				if (result instanceof ResultSet)
					result = DBUtil.createLoggingResultSet((ResultSet) result, (PreparedStatement) proxy);
				return result;
			}
		} catch (ConfigurationError e) {
			if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof SQLException)
				throw e.getCause().getCause();
			else
				throw e;
		}
	}

	public void addBatch() throws SQLException {
		logAll("addBatch", sql);
		realStatement.addBatch();
	}

	public void addBatch(String sql) throws SQLException {
		logAll("addBatch", sql);
		realStatement.addBatch(sql);
	}

	// execute methods -------------------------------------------------------------------------------------------------

	public boolean execute() throws SQLException {
		logAll("execute", this.sql);
		clearParams();
		return realStatement.execute();
	}

	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
		logAll("execute", sql);
		return realStatement.execute(sql, autoGeneratedKeys);
	}

	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
		logAll("execute", sql);
		return realStatement.execute(sql, columnIndexes);
	}

	public boolean execute(String sql, String[] columnNames) throws SQLException {
		logAll("execute", sql);
		return realStatement.execute(sql, columnNames);
	}

	public boolean execute(String sql) throws SQLException {
		logAll("execute", sql);
		return realStatement.execute(sql);
	}

	public int[] executeBatch() throws SQLException {
		JDBC_LOGGER.debug("executeBatch()");
		clearParams();
		return realStatement.executeBatch();
	}

	public ResultSet executeQuery() throws SQLException {
		logAll("executeQuery", sql);
		clearParams();
		return realStatement.executeQuery();
	}

	public ResultSet executeQuery(String sql) throws SQLException {
		logAll("executeQuery", sql);
		clearParams();
		return realStatement.executeQuery(sql);
	}
	
	public int executeUpdate() throws SQLException {
		logAll("executeUpdate", sql);
		clearParams();
		return realStatement.executeUpdate();
	}

	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
		logAll("executeUpdate", sql);
		return realStatement.executeUpdate(sql, autoGeneratedKeys);
	}

	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
		logAll("executeUpdate", sql);
		return realStatement.executeUpdate(sql, columnIndexes);
	}

	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
		logAll("executeUpdate", sql);
		return realStatement.executeUpdate(sql, columnNames);
	}

	public int executeUpdate(String sql) throws SQLException {
		logAll("executeUpdate", sql);
		return realStatement.executeUpdate(sql);
	}
	
	public void close() throws SQLException {
		if (closed)
			return;
		logAll("close", sql);
		this.closed = true;
		realStatement.close();
		openStatementCount.decrementAndGet();
		if (openStatementMonitor != null)
			openStatementMonitor.unregister(this);
	}
	
    public static int getOpenStatementCount() {
    	return openStatementCount.get();
    }
    
	public static void resetMonitors() {
		openStatementCount.set(0);
		if (openStatementMonitor != null)
			openStatementMonitor.reset();
	}
    
	public static boolean assertAllStatementsClosed(boolean critical) {
		return openStatementMonitor.assertNoRegistrations(critical);
	}

	// private helpers -------------------------------------------------------------------------------------------------
	
	private void clearParams() {
		this.params = new Object[this.params.length];
	}

	private void logAll(String method, String sql) {
		if (JDBC_LOGGER.isDebugEnabled())
			JDBC_LOGGER.debug(method + ": " + sql);
		SQL_LOGGER.debug("{}", this);
	}

	// java.lang.Object overrides --------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		String[] paramStrings = toStringArrayConverter.convert(params);
		// TODO v1.0 use DatabaseDialect to render arbitrary data types
		return "PreparedStatement (" + StringUtil.replaceTokens(sql, "?", paramStrings) + ")";
	}

}
