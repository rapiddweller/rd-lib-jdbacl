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

import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.jdbacl.identity.IdentityModel;
import com.rapiddweller.jdbacl.identity.IdentityProvider;
import com.rapiddweller.jdbacl.identity.KeyMapper;
import com.rapiddweller.jdbacl.model.Database;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * In-memory implementation of a {@link KeyMapper}.<br/><br/>
 * Created: 23.08.2010 16:55:53
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class MemKeyMapper extends KeyMapper {

  TargetDatabaseMapper targetDBMapper;
  private final Database database;
  private final Map<String, SourceDatabaseMapper> sourceDBMappers;

  public MemKeyMapper(Connection source, String sourceDbId, Connection target, String targetDbId, IdentityProvider identityProvider,
                      Database database) {
    super(identityProvider);
    sourceDBMappers = new HashMap<>();
    setTarget(target, targetDbId);
    createSourceDBMapper(source, sourceDbId);
    this.database = database;
  }

  // KeyMapper interface implementation ------------------------------------------------------------------------------

  @Override
  public void store(String sourceDbId, IdentityModel identity, String naturalKey, Object sourcePK, Object targetPK) {
    if (targetPK != null) {
      getTargetDBMapper().store(identity, naturalKey, targetPK);
    }
    getSourceDBMapper(sourceDbId).store(identity, sourcePK, naturalKey, targetPK);
  }

  @Override
  public Object getTargetPK(String sourceDbId, IdentityModel table, Object sourcePK) {
    return getSourceDBMapper(sourceDbId).getTargetPK(table, sourcePK);
  }

  @Override
  public String getNaturalKey(String dbId, IdentityModel identity, Object sourcePK) {
    if (targetDBMapper != null && dbId.equals(targetDBMapper.getDbId())) {
      return getTargetDBMapper().getNaturalKey(identity, sourcePK);
    } else {
      return getSourceDBMapper(dbId).getNaturalKey(identity, sourcePK);
    }
  }

  @Override
  public Object getTargetPK(IdentityModel table, String naturalKey) {
    return getTargetDBMapper().getTargetPK(table, naturalKey);
  }

  // helpers ---------------------------------------------------------------------------------------------------------

  private TargetDatabaseMapper getTargetDBMapper() {
    if (targetDBMapper == null) {
      throw ExceptionFactory.getInstance().configurationError("'target' is undefined. " +
          "Use MemKeyMapper.setTarget() to register the target database");
    }
    return targetDBMapper;
  }

  private void setTarget(Connection target, String targetDbId) {
    if (targetDBMapper == null) {
      targetDBMapper = new TargetDatabaseMapper(this, target, targetDbId, database);
    } else if (!(targetDBMapper.getDbId()).equals(targetDbId)) {
      throw ExceptionFactory.getInstance().configurationError("'target' has already been set to a different database: " +
          targetDBMapper.getDbId());
    }
  }

  @Override
  public void registerSource(String sourceDbId, Connection connection) {
    SourceDatabaseMapper mapper = sourceDBMappers.get(sourceDbId);
    if (mapper == null) {
      mapper = new SourceDatabaseMapper(this, connection, sourceDbId, database);
      sourceDBMappers.put(sourceDbId, mapper);
    }
  }

  private void createSourceDBMapper(Connection connection, String sourceDbId) {
    SourceDatabaseMapper mapper = new SourceDatabaseMapper(this, connection, sourceDbId, database);
    sourceDBMappers.put(sourceDbId, mapper);
  }

  private SourceDatabaseMapper getSourceDBMapper(String sourceId) {
    SourceDatabaseMapper mapper = sourceDBMappers.get(sourceId);
    if (mapper == null) {
      throw ExceptionFactory.getInstance().configurationError("Database not registered: " + sourceId);
    }
    return mapper;
  }

}
