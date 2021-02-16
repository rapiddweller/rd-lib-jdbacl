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
 * In-memory implementation of the mapping functionality needed for target database tables.<br/><br/>
 * Created: 24.08.2010 11:13:42
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class TargetTableMapper extends AbstractTableMapper {

  /**
   * The Nk to pk.
   */
  final Map<String, Object> nkToPk;

  /**
   * Instantiates a new Target table mapper.
   *
   * @param root       the root
   * @param target     the target
   * @param targetDbId the target db id
   * @param identity   the identity
   * @param database   the database
   */
  public TargetTableMapper(KeyMapper root, Connection target, String targetDbId, IdentityModel identity, Database database) {
    super(root, target, targetDbId, identity, database);
    this.nkToPk = new HashMap<>(1000);
  }

  @Override
  public void store(Object targetPK, String naturalKey) {
    super.store(targetPK, naturalKey);
    nkToPk.put(naturalKey, targetPK);
  }

  /**
   * Gets target id.
   *
   * @param naturalKey the natural key
   * @return the target id
   */
  public Object getTargetId(String naturalKey) {
    assureInitialized();
    return nkToPk.get(naturalKey);
  }

}
