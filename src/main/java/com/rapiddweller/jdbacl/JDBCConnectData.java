/*
 * (c) Copyright 2010 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.IOUtil;
import com.rapiddweller.common.ParseUtil;
import com.rapiddweller.common.StringUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Data Object that contains the typical data required for a JDBC database login.<br/><br/>
 * Created: 10.02.2010 22:59:02
 *
 * @author Volker Bergmann
 * @since 0.5.0
 */
public class JDBCConnectData {

  /**
   * The Driver.
   */
  public final String driver;
  /**
   * The Url.
   */
  public final String url;
  /**
   * The User.
   */
  public final String user;
  /**
   * The Password.
   */
  public final String password;

  /**
   * The Catalog.
   */
  public final String catalog;
  /**
   * The Schema.
   */
  public final String schema;

  /**
   * The Read only.
   */
  public final boolean readOnly;

  /**
   * Instantiates a new Jdbc connect data.
   *
   * @param driver   the driver
   * @param url      the url
   * @param user     the user
   * @param password the password
   */
  public JDBCConnectData(String driver, String url, String user, String password) {
    this(driver, url, user, password, null, null, false);
  }

  /**
   * Instantiates a new Jdbc connect data.
   *
   * @param driver   the driver
   * @param url      the url
   * @param user     the user
   * @param password the password
   * @param catalog  the catalog
   * @param schema   the schema
   * @param readOnly the read only
   */
  public JDBCConnectData(String driver, String url, String user, String password, String catalog, String schema, boolean readOnly) {
    this.driver = driver;
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
    this.catalog = catalog;
    this.readOnly = readOnly;
  }

  /**
   * Parse single db properties jdbc connect data.
   *
   * @param filename the filename
   * @return the jdbc connect data
   * @throws IOException the io exception
   */
  public static JDBCConnectData parseSingleDbProperties(String filename) throws IOException {
    Map<String, String> properties = IOUtil.readProperties(filename);
    String readOnlyConfig = properties.get("db_readonly");
    boolean readOnly = (!StringUtil.isEmpty(readOnlyConfig) ? ParseUtil.parseBoolean(readOnlyConfig, true) : false);

    return new JDBCConnectData(
        properties.get("db_driver"), properties.get("db_url"),
        properties.get("db_user"), properties.get("db_password"),
        properties.get("db_catalog"), properties.get("db_schema"),
        readOnly);
  }

}
