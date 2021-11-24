/*
 * (c) Copyright 2009-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a JDBC driver and related information.<br/><br/>
 * Created at 23.02.2009 09:40:31
 * @author Volker Bergmann
 * @since 0.4.8
 */
public class JDBCDriverInfo {

  private static final String DB_DEFINITION_FILE = "com/rapiddweller/jdbacl/jdbc-driver-info.xml";

  private String id;
  private String name;
  private String dbSystem;
  private String downloadUrl;
  private String driverClass;
  private String defaultDatabase;
  private String defaultSchema;
  private String defaultPort;
  private String urlPattern;
  private String defaultUser;
  private String[] jars;

  public JDBCDriverInfo() {
    this(null, null, null);
  }

  public JDBCDriverInfo(String id, String name, String dbSystem) {
    this.id = id;
    this.name = name;
    this.dbSystem = dbSystem;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = normalizeNull(id);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = normalizeNull(name);
  }

  public String getDbSystem() {
    return dbSystem;
  }

  public void setDbSystem(String dbSystem) {
    this.dbSystem = normalizeNull(dbSystem);
  }

  public String getUrlPattern() {
    return urlPattern;
  }

  public void setUrlPattern(String urlPattern) {
    this.urlPattern = normalizeNotNull(urlPattern);
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = normalizeNull(downloadUrl);
  }

  public String getDefaultPort() {
    return defaultPort;
  }

  public void setDefaultPort(String defaultPort) {
    this.defaultPort = normalizeNull(defaultPort);
  }

  public String[] getJars() {
    return jars;
  }

  public void setJars(String[] jars) {
    this.jars = jars;
  }

  public String getDriverClass() {
    return driverClass;
  }

  public void setDriverClass(String driverClass) {
    this.driverClass = normalizeNull(driverClass);
  }

  public String getDefaultUser() {
    return defaultUser;
  }

  public void setDefaultUser(String defaultUser) {
    this.defaultUser = normalizeNull(defaultUser);
  }

  public String getDefaultDatabase() {
    return defaultDatabase;
  }

  public void setDefaultDatabase(String defaultDatabase) {
    this.defaultDatabase = normalizeNull(defaultDatabase);
  }

  public String getDefaultSchema() {
    return defaultSchema;
  }

  public void setDefaultSchema(String defaultSchema) {
    this.defaultSchema = defaultSchema;
  }

  public String getUrlPrefix() {
    int check = urlPattern.indexOf('{');
    return (check > 0 ? urlPattern.substring(0, check) : urlPattern);
  }

  // operations ------------------------------------------------------------------------------------------------------

  public String jdbcURL(String host, String port, String database) {
    return MessageFormat.format(urlPattern, host, port, database);
  }

  // private helpers -------------------------------------------------------------------------------------------------

  private static String normalizeNull(String value) {
    return (value == null || value.trim().length() == 0 ? null : value.trim());
  }

  private static String normalizeNotNull(String value) {
    return (value == null || value.trim().length() == 0 ? "" : value.trim());
  }

  private static final HashMap<String, JDBCDriverInfo> instances = new HashMap<>();

  static {
    Document document = XMLUtil.parse(DB_DEFINITION_FILE);
    Element root = document.getDocumentElement();
    Element[] driverElements = XMLUtil.getChildElements(root);
    for (Element driverElement : driverElements) {
      JDBCDriverInfo driver = new JDBCDriverInfo();
      driver.setId(driverElement.getAttribute("id"));
      driver.setName(driverElement.getAttribute("name"));
      driver.setDbSystem(driverElement.getAttribute("system"));
      driver.setDriverClass(driverElement.getAttribute("class"));
      driver.setDefaultPort(driverElement.getAttribute("port"));
      driver.setDefaultDatabase(driverElement.getAttribute("defaultDatabase"));
      driver.setDefaultSchema(driverElement.getAttribute("defaultSchema"));
      driver.setUrlPattern(driverElement.getAttribute("url"));
      driver.setDownloadUrl(driverElement.getAttribute("info"));
      driver.setDefaultUser(driverElement.getAttribute("user"));
      ArrayBuilder<String> builder = new ArrayBuilder<>(String.class);
      for (Element dependencyElement : XMLUtil.getChildElements(driverElement, false, "dependency")) {
        builder.add(dependencyElement.getAttribute("lib"));
      }
      driver.setJars(builder.toArray());
      instances.put(driver.getId(), driver);
    }
  }

  @Override
  public String toString() {
    return dbSystem;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    JDBCDriverInfo that = (JDBCDriverInfo) obj;
    return this.id.equals(that.id);
  }

  public static final JDBCDriverInfo HSQL = getInstance("HSQL");
  public static final JDBCDriverInfo FIREBIRD = getInstance("FIREBIRD");
  public static final JDBCDriverInfo ORACLE = getInstance("ORACLE");

  public static Collection<JDBCDriverInfo> getInstances() {
    return instances.values();
  }

  public static JDBCDriverInfo getInstance(String name) {
    return instances.get(name);
  }

}
