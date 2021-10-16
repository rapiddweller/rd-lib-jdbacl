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

package com.rapiddweller.jdbacl.identity;

import com.rapiddweller.common.ConnectFailedException;
import com.rapiddweller.common.HeavyweightIterator;
import com.rapiddweller.common.ImportFailedException;
import com.rapiddweller.common.SystemInfo;
import com.rapiddweller.jdbacl.DBUtil;
import com.rapiddweller.jdbacl.dialect.HSQLUtil;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.jdbc.JDBCMetaDataUtil;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Parent class for {@link IdentityModel} tests.<br/><br/>
 * Created: 06.12.2010 06:45:34
 * @author Volker Bergmann
 * @since 0.4
 */
public abstract class AbstractIdentityTest {

  protected static final String LF = SystemInfo.getLineSeparator();

  protected static final String CREATE_COUNTRY_TABLE =
      "create table country (" + LF +
          "	code char(2)," + LF +
          "	name varchar(20)," + LF +
          "	constraint country_pk primary key (code)" + LF +
          ")";

  protected static final String DROP_COUNTRY_TABLE = "drop table country";

  protected static final String INSERT_COUNTRY_DE = "insert into country values ('DE', 'GERMANY')";
  protected static final String INSERT_COUNTRY_FR = "insert into country values ('FR', 'FRANCE')";
  protected static final String INSERT_COUNTRY_UK = "insert into country values ('UK', 'UNITED KINGDOM')";

  protected static final String CREATE_STATE_TABLE =
      "create table state (" + LF +
          "	id int," + LF +
          "	country char(2)," + LF +
          "	code char(2)," + LF +
          "	constraint state_pk primary key (id)," + LF +
          "	constraint state_country_fk foreign key (country) references country (code)" + LF +
          ")";

  protected static final String DROP_STATE_TABLE = "drop table state";

  protected static final String INSERT_STATE_BY = "insert into state values (1, 'DE', 'BY')";

  protected void createTables(Connection source) throws SQLException {
    DBUtil.executeUpdate(CREATE_COUNTRY_TABLE, source);
    DBUtil.executeUpdate(CREATE_STATE_TABLE, source);
  }

  protected void dropTables(Connection source) throws SQLException {
    DBUtil.executeUpdate(DROP_STATE_TABLE, source);
    DBUtil.executeUpdate(DROP_COUNTRY_TABLE, source);
  }

  protected void insertData(Connection source) throws SQLException {
    DBUtil.executeUpdate(INSERT_COUNTRY_DE, source);
    DBUtil.executeUpdate(INSERT_COUNTRY_FR, source);
    DBUtil.executeUpdate(INSERT_COUNTRY_UK, source);
    DBUtil.executeUpdate(INSERT_STATE_BY, source);
  }

  protected IdentityProvider createIdentities(Database database) {
    IdentityProvider identityProvider = new IdentityProvider();

    NkPkQueryIdentity countryIdentity = new NkPkQueryIdentity(
        "country", "select code, code from country");
    identityProvider.registerIdentity(countryIdentity, "country");

    SubNkPkQueryIdentity stateIdentity = new SubNkPkQueryIdentity(
        "state", new String[] {"country"}, identityProvider);
    stateIdentity.setSubNkPkQuery("select code, id from state where country = ?");
    identityProvider.registerIdentity(stateIdentity, "state");

    return identityProvider;
  }

  protected Database importDatabase(Connection target) throws ConnectFailedException, ImportFailedException {
    return JDBCMetaDataUtil.getMetaData(target, null, "sa", null, "PUBLIC");
  }

  protected Connection connectDB(String dbName, int port) throws ConnectFailedException {
    return HSQLUtil.connectInMemoryDB(dbName, port);
  }

  protected void expectCountryNkPk(String nk, String pk, HeavyweightIterator<Object[]> iterator) {
    assertTrue(iterator.hasNext());
    Object[] cells = iterator.next();
    assertEquals(nk, cells[0]);
    assertEquals(pk, cells[1]);
  }

  protected void expectStateNkPk(HeavyweightIterator<Object[]> iterator) {
    assertTrue(iterator.hasNext());
    Object[] cells = iterator.next();
    assertEquals("DE|BY", cells[0]);
    assertEquals(1, cells[1]);
  }

}
