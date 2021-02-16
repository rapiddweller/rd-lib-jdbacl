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

package com.rapiddweller.jdbacl.identity.mem;

import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.jdbacl.model.Database;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of the mapping functionality needed for a target database.<br/><br/>
 * Created: 24.08.2010 11:15:53
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class TargetDatabaseMapper {

  /**
   * The Root.
   */
  final KeyMapper root;
  /**
   * The Target.
   */
  final Connection target;
  /**
   * The Target db id.
   */
  final String targetDbId;
  /**
   * The Database.
   */
  final Database database;
  /**
   * The Table mappers.
   */
  final Map<String, TargetTableMapper> tableMappers;

  /**
   * Instantiates a new Target database mapper.
   *
   * @param root       the root
   * @param target     the target
   * @param targetDbId the target db id
   * @param database   the database
   */
  public TargetDatabaseMapper(KeyMapper root, Connection target, String targetDbId, Database database) {
    this.root = root;
    this.target = target;
    this.targetDbId = targetDbId;
    this.database = database;
    tableMappers = new HashMap<>(500);
  }

  // interface -------------------------------------------------------------------------------------------------------

  /**
   * Gets db id.
   *
   * @return the db id
   */
  public String getDbId() {
    return targetDbId;
  }

  /**
   * Store.
   *
   * @param identity   the identity
   * @param naturalKey the natural key
   * @param targetPK   the target pk
   */
  public void store(IdentityModel identity, String naturalKey, Object targetPK) {
    getOrCreateTableMapper(target, targetDbId, identity).store(targetPK, naturalKey);
  }

  /**
   * Gets natural key.
   *
   * @param identity the identity
   * @param sourcePK the source pk
   * @return the natural key
   */
  public String getNaturalKey(IdentityModel identity, Object sourcePK) {
    return getOrCreateTableMapper(target, targetDbId, identity).getNaturalKey(sourcePK);
  }

  /**
   * Gets target pk.
   *
   * @param identity   the identity
   * @param naturalKey the natural key
   * @return the target pk
   */
  public Object getTargetPK(IdentityModel identity, String naturalKey) {
    return getOrCreateTableMapper(target, targetDbId, identity).getTargetId(naturalKey);
  }

  // helpers ---------------------------------------------------------------------------------------------------------

  private TargetTableMapper getOrCreateTableMapper(Connection target, String targetDbId, IdentityModel identity) {
    String tableName = identity.getTableName();
    TargetTableMapper result = tableMappers.get(tableName);
    if (result == null) {
      result = new TargetTableMapper(root, target, targetDbId, identity, database);
      tableMappers.put(tableName, result);
    }
    return result;
  }

}
