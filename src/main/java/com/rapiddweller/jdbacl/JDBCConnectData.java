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

  public final String driver;
  public final String url;
  public final String user;
  public final String password;

  public final String catalog;
  public final String schema;

  public final boolean readOnly;

  public JDBCConnectData(String driver, String url, String user, String password) {
    this(driver, url, user, password, null, null, false);
  }

  public JDBCConnectData(String driver, String url, String user, String password, String catalog, String schema, boolean readOnly) {
    this.driver = driver;
    this.url = url;
    this.user = user;
    this.password = password;
    this.schema = schema;
    this.catalog = catalog;
    this.readOnly = readOnly;
  }

  public static JDBCConnectData parseSingleDbProperties(String filename) throws IOException {
    Map<String, String> properties = IOUtil.readProperties(filename);
    String readOnlyConfig = properties.get("db_readonly");
    boolean readOnly = (!StringUtil.isEmpty(readOnlyConfig) && ParseUtil.parseBoolean(readOnlyConfig, true));
    return new JDBCConnectData(
        properties.get("db_driver"), properties.get("db_url"),
        properties.get("db_user"), properties.get("db_password"),
        properties.get("db_catalog"), properties.get("db_schema"),
        readOnly);
  }

}
