/*
 * (c) Copyright 2010-2021 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.cache;

import com.rapiddweller.common.Assert;
import com.rapiddweller.common.ConfigUtil;
import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.FileUtil;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.Period;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.jdbacl.model.DBMetaDataImporter;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;
import com.rapiddweller.jdbacl.model.xml.XMLModelExporter;
import com.rapiddweller.jdbacl.model.xml.XMLModelImporter;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

/**
 * {@link DBMetaDataImporter} that acts as a proxy to another DBMetaDataImporter,
 * adding the feature of caching its output. The data file is named '&lt;environment&gt;.meta.xml'
 * and expires after 12 hrs.<br/><br/>
 * Created: 10.01.2011 14:48:00
 * @author Volker Bergmann
 * @since 0.6.5
 */
public class CachingDBImporter implements DBMetaDataImporter, Closeable {

  private static final Logger logger = LoggerFactory.getLogger(CachingDBImporter.class);

  public static final String TIME_TO_LIVE_SYSPROP = "jdbacl.cache.timetolive";
  public static final long DEFAULT_TIME_TO_LIVE = Period.HOUR.getMillis() * 12;

  private static final String CACHE_FILE_SUFFIX = ".meta.xml";

  /** URL of the connected database. Most data is stored in the {@link #realImporter},
   *  but since if that has been initialized with a connection, its URL is null. */
  protected final String url;
  protected final JDBCDBImporter realImporter;

  public CachingDBImporter(String url, JDBCDBImporter realImporter) {
    this.url = url;
    this.realImporter = realImporter;
  }

  public JDBCDBImporter getRealImporter() {
    return realImporter;
  }

  public void invalidate() {
    deleteCacheFile(getCacheFile());
  }

  public static void deleteCacheFile(String url, String user, String catalog, String schema) {
    deleteCacheFile(getCacheFile(url, user, catalog, schema));
  }

  private static void deleteCacheFile(File file) {
    FileUtil.deleteIfExists(file);
    if (file.exists()) {
      if (!file.delete()) {
        logger.error("Deleting {} failed", file);
      } else {
        logger.info("Deleted meta data cache file: {}", file);
      }
    }
  }

  @Override
  public Database importDatabase() throws ConnectFailedException, ImportFailedException {
    File file = getCacheFile();
    long now = System.currentTimeMillis();
    long timeToLive = getTimeToLive();
    if (file.exists() && (timeToLive < 0 || now - file.lastModified() < timeToLive)) {
      return readCachedData(file);
    } else {
      return importFreshData(file);
    }
  }

  private static long getTimeToLive() {
    String sysProp = System.getProperty(TIME_TO_LIVE_SYSPROP);
    if (!StringUtil.isEmpty(sysProp)) {
      long scale = 1;
      if (sysProp.endsWith("d")) {
        scale = Period.DAY.getMillis();
        sysProp = sysProp.substring(0, sysProp.length() - 1);
      }
      return Long.parseLong(sysProp) * scale;
    } else {
      return DEFAULT_TIME_TO_LIVE;
    }
  }

  @Override
  public void close() throws IOException {
    if (realImporter != null) {
      ((Closeable) realImporter).close();
    }
  }

  // non-public helpers ----------------------------------------------------------------------------------------------

  protected Database readCachedData(File cacheFile) throws ConnectFailedException, ImportFailedException {
    try {
      logger.info("Importing database meta data from cache file {}", cacheFile.getPath());
      Database database = new XMLModelImporter(cacheFile, realImporter).importDatabase();
      logger.info("Database meta data import completed");
      return database;
    } catch (Exception e) {
      logger.info("Error reading cache file, reparsing database", e);
      return importFreshData(cacheFile);
    }
  }

  protected Database importFreshData(File file) throws ConnectFailedException, ImportFailedException {
    Database database = realImporter.importDatabase();
    return writeCacheFile(file, database);
  }

  public static Database writeCacheFile(File file, Database database) {
    logger.info("Exporting Database meta data of {} to cache file", database.getName());
    try {
      FileUtil.ensureDirectoryExists(file.getParentFile());
      new XMLModelExporter(file).export(database);
      logger.debug("Database meta data export completed");
    } catch (Exception e) {
      logger.error("Error writing database meta data file " + ": " + e.getMessage(), e);
    }
    return database;
  }

  public static File getCacheFile(String url, String user, String catalog, String schema) {
    return new File(getMetaCacheFolder(), getCacheFileName(url, user, catalog, schema));
  }

  protected File getCacheFile() {
    File cacheFile = new File(getMetaCacheFolder(), getCacheFileName(url, getRealImporter()));
    return FileUtil.getFileIgnoreCase(cacheFile, false);
  }

  private static File getMetaCacheFolder() {
    return new File(ConfigUtil.commonCacheFolder(), "db-meta-data");
  }

  private String getCacheFileName(String url, JDBCDBImporter imp) {
    return getCacheFileName(url, imp.getUser(), imp.getCatalogName(), imp.getSchemaName());
  }

  static String getCacheFileName(String url, String user, String catalog, String schema) {
    Assert.notNull(url, "url");
    String result = normalize(url);
    if (!StringUtil.isEmpty(user)) {
      result += "-usr_" + user;
    }
    if (!StringUtil.isEmpty(catalog)) {
      result += "-cat_" + catalog;
    }
    if (!StringUtil.isEmpty(schema)) {
      result += "-sch_" + schema;
    }
    result += CACHE_FILE_SUFFIX;
    return result.toLowerCase();
  }

  static String normalize(String url) {
    return url.replace(":", "_").replace("/", "_").toLowerCase();
  }

}
