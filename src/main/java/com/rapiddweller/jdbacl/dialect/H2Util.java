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

import com.rapiddweller.common.exception.ConnectFailedException;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.jdbacl.DBUtil;

import java.sql.Connection;

/**
 * Provides utility methods for the H2 database.<br/><br/>
 * Created: 21.10.2011 20:40:23
 * @author Volker Bergmann
 * @since 0.6.13
 */
public class H2Util {

  public static final String IN_MEMORY_URL_PREFIX = "jdbc:h2:mem:";
  public static final String DRIVER = "org.h2.Driver";
  public static final String DEFAULT_USER = "sa";
  public static final String DEFAULT_PASSWORD = "";
  public static final String DEFAULT_SCHEMA = "PUBLIC";
  public static final int DEFAULT_PORT = 9001;

  /** private constructor to prevent instantiation of this utility class. */
  private H2Util() {
    // private constructor to prevent instantiation of this utility class
  }

  public static Connection connectInMemoryDB(String dbName, int port) throws ConnectFailedException {
    return connectInMemoryDB(dbName + ":" + port);
  }

  public static Connection connectInMemoryDB(String dbName) throws ConnectFailedException {
    String driver = DRIVER;
    try {
      Class.forName(driver);
      String url = getInMemoryURL(dbName);
      return DBUtil.connect(url, DRIVER, DEFAULT_USER, DEFAULT_PASSWORD, false);
    } catch (ClassNotFoundException e) {
      throw ExceptionFactory.getInstance().configurationError("JDBC driver not found: " + driver, e);
    }
  }

  public static String getInMemoryURL(String dbName) {
    return IN_MEMORY_URL_PREFIX + dbName;
  }

}
