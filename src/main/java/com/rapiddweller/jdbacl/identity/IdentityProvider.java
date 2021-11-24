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

import com.rapiddweller.common.collection.OrderedNameMap;
import com.rapiddweller.common.exception.ExceptionFactory;

/**
 * Manages {@link IdentityModel}s.<br/><br/>
 * Created: 10.12.2010 20:10:15
 * @author Volker Bergmann
 * @since 0.6.8
 */
public class IdentityProvider {

  private final OrderedNameMap<IdentityModel> identities = OrderedNameMap.createCaseIgnorantMap();

  public IdentityModel getIdentity(String tableName) {
    return getIdentity(tableName, true);
  }

  public IdentityModel getIdentity(String tableName, boolean required) {
    IdentityModel result = identities.get(tableName);
    if (required && (result == null || result instanceof NoIdentity)) {
      throw ExceptionFactory.getInstance().objectNotFound("No identity defined for table '" + tableName + "'");
    }
    return result;
  }

  public void registerIdentity(IdentityModel identity, String tableName) {
    identities.put(tableName, identity);
  }

}
