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

package com.rapiddweller.jdbacl.identity;

import java.sql.Connection;

/**
 * Parent for classes that map between primary key and natural keys
 * of table rows in different tables in one or more source databases and one target database.<br/><br/>
 * Created: 23.08.2010 16:48:21
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public abstract class KeyMapper {

  /**
   * The Identity provider.
   */
  final IdentityProvider identityProvider;

  /**
   * Instantiates a new Key mapper.
   *
   * @param identityProvider the identity provider
   */
  public KeyMapper(IdentityProvider identityProvider) {
    this.identityProvider = identityProvider;
  }

  /**
   * Gets identity provider.
   *
   * @return the identity provider
   */
  public IdentityProvider getIdentityProvider() {
    return identityProvider;
  }

  /**
   * Register source.
   *
   * @param dbId       the db id
   * @param connection the connection
   */
  public abstract void registerSource(String dbId, Connection connection);

  /**
   * Store.
   *
   * @param sourceDbId the source db id
   * @param identity   the identity
   * @param naturalKey the natural key
   * @param sourcePK   the source pk
   * @param targetPK   the target pk
   */
  public abstract void store(String sourceDbId, IdentityModel identity, String naturalKey, Object sourcePK, Object targetPK);

  /**
   * Gets target pk.
   *
   * @param sourceDbId the source db id
   * @param identity   the identity
   * @param sourcePK   the source pk
   * @return the target pk
   */
  public abstract Object getTargetPK(String sourceDbId, IdentityModel identity, Object sourcePK);

  /**
   * Gets target pk.
   *
   * @param identity   the identity
   * @param naturalKey the natural key
   * @return the target pk
   */
  public abstract Object getTargetPK(IdentityModel identity, String naturalKey);

  /**
   * Gets natural key.
   *
   * @param dbId     the db id
   * @param identity the identity
   * @param sourcePK the source pk
   * @return the natural key
   */
  public abstract String getNaturalKey(String dbId, IdentityModel identity, Object sourcePK);

}
