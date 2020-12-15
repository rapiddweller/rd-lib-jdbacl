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

package com.rapiddweller.jdbacl.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import com.rapiddweller.commons.BeanUtil;
import com.rapiddweller.commons.ConfigurationError;
import com.rapiddweller.commons.LogCategories;
import com.rapiddweller.commons.debug.Debug;
import com.rapiddweller.commons.debug.ResourceMonitor;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * {@link InvocationHandler} for the {@link ResultSet} interface 
 * that logs certain calls to the category {@link LogCategories#JDBC}.<br/>
 * <br/>
 * Created: 12.04.2011 14:02:38
 * @since 0.6.8
 * @author Volker Bergmann
 */
public class LoggingResultSetHandler implements InvocationHandler {

    private static final Logger JDBC_LOGGER = LogManager.getLogger(LogCategories.JDBC);
    
    private static volatile AtomicInteger openResultSetCount;
    private static ResourceMonitor openResultSetMonitor;

	// attributes ------------------------------------------------------------------------------------------------------

	private ResultSet realResultSet;
	private Statement statement;
	
	// constructor -----------------------------------------------------------------------------------------------------

	static {
		openResultSetCount = new AtomicInteger();
    	if (Debug.active())
    		openResultSetMonitor = new ResourceMonitor();
	}
	
	public LoggingResultSetHandler(ResultSet realResultSet, Statement statement) {
		this.realResultSet = realResultSet;
		this.statement = statement;
		openResultSetCount.incrementAndGet();
		if (openResultSetMonitor != null)
			openResultSetMonitor.register(this);
		JDBC_LOGGER.debug("created result set {}", this);
	}
	
	// InvocationHandler interface implementation ----------------------------------------------------------------------

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		try {
			String methodName = method.getName();
			if ("close".equals(methodName)) {
				openResultSetCount.decrementAndGet();
				if (openResultSetMonitor != null)
					openResultSetMonitor.unregister(this);
				JDBC_LOGGER.debug("closing result set {}", this);
			} else if ("toString".equals(methodName)) {
				return "ResultSet (" + statement + ")";
			} else if ("getStatement".equals(methodName)) {
				return statement;
			}
			return BeanUtil.invoke(realResultSet, method, args);
		} catch (ConfigurationError e) {
			if (e.getCause() instanceof InvocationTargetException && e.getCause().getCause() instanceof SQLException)
				throw e.getCause().getCause();
			else
				throw e;
		}
	}

	// tracking methods ------------------------------------------------------------------------------------------------
	
	public static int getOpenResultSetCount() {
		return openResultSetCount.get();
	}

	public static void resetMonitors() {
		openResultSetCount.set(0);
		if (openResultSetMonitor != null)
			openResultSetMonitor.reset();
	}

	public static boolean assertAllResultSetsClosed(boolean critical) {
		return openResultSetMonitor.assertNoRegistrations(critical);
	}
	
}
