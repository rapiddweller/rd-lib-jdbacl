/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.identity.mem;

import com.rapiddweller.common.HeavyweightIterator;
import com.rapiddweller.common.bean.ObjectOrArray;
import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.jdbacl.model.Database;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Parent for classes that map the primary key values of the rows of one table in one database
 * to their natural keys.<br/><br/>
 * Created: 07.09.2010 14:11:16
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public abstract class AbstractTableMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTableMapper.class);

  /**
   * The Root.
   */
  protected final KeyMapper root;
  /**
   * The Connection.
   */
  protected final Connection connection;
  /**
   * The Db id.
   */
  protected final String dbId;
  /**
   * The Identity.
   */
  protected final IdentityModel identity;
  private final Map<ObjectOrArray, String> pkToNk;
  private MapperState state;
  /**
   * The Database.
   */
  final Database database;

  /**
   * Instantiates a new Abstract table mapper.
   *
   * @param root       the root
   * @param connection the connection
   * @param dbId       the db id
   * @param identity   the identity
   * @param database   the database
   */
  public AbstractTableMapper(KeyMapper root, Connection connection, String dbId, IdentityModel identity, Database database) {
    this.root = root;
    this.connection = connection;
    this.dbId = dbId;
    this.identity = identity;
    this.database = database;
    this.pkToNk = new HashMap<>(1000);
    this.state = MapperState.CREATED;
  }

  // interface -------------------------------------------------------------------------------------------------------

  /**
   * Store.
   *
   * @param pk         the pk
   * @param naturalKey the natural key
   */
  public void store(Object pk, String naturalKey) {
    if (state == MapperState.CREATED) {
      state = MapperState.PASSIVE;
    }
    ObjectOrArray globalRowId = new ObjectOrArray(pk);
    pkToNk.put(globalRowId, naturalKey);
  }

  /**
   * Gets natural key.
   *
   * @param pk the pk
   * @return the natural key
   */
  public String getNaturalKey(Object pk) {
    assureInitialized();
    return pkToNk.get(new ObjectOrArray(pk));
  }

  // helpers ---------------------------------------------------------------------------------------------------------

  private void populate() {
    this.state = MapperState.POPULATING;
    LOGGER.debug("Populating key mapper for table {} on database {}", identity.getTableName(), dbId);
    HeavyweightIterator<Object[]> iterator = identity.createNkPkIterator(connection, dbId, root, database);
    while (iterator.hasNext()) {
      Object[] nkPkTuple = iterator.next();
      Object pk = identity.extractPK(nkPkTuple);
      String nk = identity.extractNK(nkPkTuple);
      store(pk, nk);
    }
    this.state = MapperState.POPULATED;
  }

  /**
   * Assure initialized.
   */
  protected void assureInitialized() {
    if (state == MapperState.CREATED) {
      populate();
    }
  }

}
