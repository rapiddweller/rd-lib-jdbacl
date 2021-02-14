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
import com.rapiddweller.common.ConfigurationError;
import com.rapiddweller.common.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;

/**
 * Represents a JDBC driver and related information.<br/>
 * <br/>
 * Created at 23.02.2009 09:40:31
 *
 * @author Volker Bergmann
 * @since 0.4.8
 */
public class JDBCDriverInfo implements Serializable {

  private static final long serialVersionUID = 190436633421519236L;

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

  /**
   * Instantiates a new Jdbc driver info.
   */
  public JDBCDriverInfo() {
    this(null, null, null);
  }

  /**
   * Instantiates a new Jdbc driver info.
   *
   * @param id       the id
   * @param name     the name
   * @param dbSystem the db system
   */
  public JDBCDriverInfo(String id, String name, String dbSystem) {
    this.id = id;
    this.name = name;
    this.dbSystem = dbSystem;
  }

  /**
   * Gets id.
   *
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets id.
   *
   * @param id the id
   */
  public void setId(String id) {
    this.id = normalizeNull(id);
  }

  /**
   * Gets name.
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets name.
   *
   * @param name the name
   */
  public void setName(String name) {
    this.name = normalizeNull(name);
  }

  /**
   * Gets db system.
   *
   * @return the db system
   */
  public String getDbSystem() {
    return dbSystem;
  }

  /**
   * Sets db system.
   *
   * @param dbSystem the db system
   */
  public void setDbSystem(String dbSystem) {
    this.dbSystem = normalizeNull(dbSystem);
  }

  /**
   * Gets url pattern.
   *
   * @return the url pattern
   */
  public String getUrlPattern() {
    return urlPattern;
  }

  /**
   * Sets url pattern.
   *
   * @param urlPattern the url pattern
   */
  public void setUrlPattern(String urlPattern) {
    this.urlPattern = normalizeNotNull(urlPattern);
  }

  /**
   * Gets download url.
   *
   * @return the download url
   */
  public String getDownloadUrl() {
    return downloadUrl;
  }

  /**
   * Sets download url.
   *
   * @param downloadUrl the download url
   */
  public void setDownloadUrl(String downloadUrl) {
    this.downloadUrl = normalizeNull(downloadUrl);
  }

  /**
   * Gets default port.
   *
   * @return the default port
   */
  public String getDefaultPort() {
    return defaultPort;
  }

  /**
   * Sets default port.
   *
   * @param defaultPort the default port
   */
  public void setDefaultPort(String defaultPort) {
    this.defaultPort = normalizeNull(defaultPort);
  }

  /**
   * Get jars string [ ].
   *
   * @return the string [ ]
   */
  public String[] getJars() {
    return jars;
  }

  /**
   * Sets jars.
   *
   * @param jars the jars
   */
  public void setJars(String[] jars) {
    this.jars = jars;
  }

  /**
   * Gets driver class.
   *
   * @return the driver class
   */
  public String getDriverClass() {
    return driverClass;
  }

  /**
   * Sets driver class.
   *
   * @param driverClass the driver class
   */
  public void setDriverClass(String driverClass) {
    this.driverClass = normalizeNull(driverClass);
  }

  /**
   * Gets default user.
   *
   * @return the default user
   */
  public String getDefaultUser() {
    return defaultUser;
  }

  /**
   * Sets default user.
   *
   * @param defaultUser the default user
   */
  public void setDefaultUser(String defaultUser) {
    this.defaultUser = normalizeNull(defaultUser);
  }

  /**
   * Gets default database.
   *
   * @return the default database
   */
  public String getDefaultDatabase() {
    return defaultDatabase;
  }

  /**
   * Sets default database.
   *
   * @param defaultDatabase the default database
   */
  public void setDefaultDatabase(String defaultDatabase) {
    this.defaultDatabase = normalizeNull(defaultDatabase);
  }

  /**
   * Gets default schema.
   *
   * @return the default schema
   */
  public String getDefaultSchema() {
    return defaultSchema;
  }

  /**
   * Sets default schema.
   *
   * @param defaultSchema the default schema
   */
  public void setDefaultSchema(String defaultSchema) {
    this.defaultSchema = defaultSchema;
  }

  /**
   * Gets url prefix.
   *
   * @return the url prefix
   */
  public String getUrlPrefix() {
    int check = urlPattern.indexOf('{');
    return (check > 0 ? urlPattern.substring(0, check) : urlPattern);
  }

  // operations ------------------------------------------------------------------------------------------------------

  /**
   * Jdbc url string.
   *
   * @param host     the host
   * @param port     the port
   * @param database the database
   * @return the string
   */
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
    try {
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
    } catch (IOException e) {
      throw new ConfigurationError("Unable to read database info file", e);
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

  /**
   * The constant HSQL.
   */
  public static final JDBCDriverInfo HSQL = getInstance("HSQL");
  /**
   * The constant FIREBIRD.
   */
  public static final JDBCDriverInfo FIREBIRD = getInstance("FIREBIRD");
  /**
   * The constant ORACLE.
   */
  public static final JDBCDriverInfo ORACLE = getInstance("ORACLE");

  /**
   * Gets instances.
   *
   * @return the instances
   */
  public static Collection<JDBCDriverInfo> getInstances() {
    return instances.values();
  }

  /**
   * Gets instance.
   *
   * @param name the name
   * @return the instance
   */
  public static JDBCDriverInfo getInstance(String name) {
    return instances.get(name);
  }

}
