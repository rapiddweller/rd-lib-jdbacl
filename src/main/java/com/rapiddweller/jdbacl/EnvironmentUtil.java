/* (c) Copyright 2021 by Volker Bergmann. All rights reserved. */

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.version.VersionNumber;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Provides utilities related to database environment definition files.<br/><br/>
 * Created: 20.10.2021 07:50:59
 * @author Volker Bergmann
 * @since 1.1.12
 */
public class EnvironmentUtil {

  private EnvironmentUtil() {
    // private constructor to prevent instantiation of this utility class
  }

  public static String getProductDescription(String environment) {
    try (Connection connection = DBUtil.connect(environment, ".", true)) {
      DatabaseMetaData metaData = connection.getMetaData();
      return metaData.getDatabaseProductName() + " "
          + VersionNumber.valueOf(metaData.getDatabaseProductVersion());
    } catch (ConnectFailedException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

  public static DatabaseDialect getDialect(String environment) {
    try (Connection connection = DBUtil.connect(environment, ".", true)) {
      DatabaseMetaData metaData = connection.getMetaData();
      String databaseProductName = metaData.getDatabaseProductName();
      VersionNumber databaseProductVersion = VersionNumber.valueOf(metaData.getDatabaseProductVersion());
      return DatabaseDialectManager.getDialectForProduct(databaseProductName, databaseProductVersion);
    } catch (ConnectFailedException | SQLException e) {
      throw new RuntimeException(e);
    }
  }

}
