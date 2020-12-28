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
 * @since 0.6.4
 * @author Volker Bergmann
 */
public abstract class KeyMapper {
	
	final IdentityProvider identityProvider;
	
	public KeyMapper(IdentityProvider identityProvider) {
		this.identityProvider = identityProvider;
    }

	public IdentityProvider getIdentityProvider() {
		return identityProvider;
	}
	
	public abstract void registerSource(String dbId, Connection connection);
	
	public abstract void store(String sourceDbId, IdentityModel identity, String naturalKey, Object sourcePK, Object targetPK);
	
	public abstract Object getTargetPK(String sourceDbId, IdentityModel identity, Object sourcePK);
	
	public abstract Object getTargetPK(IdentityModel identity, String naturalKey);
	
	public abstract String getNaturalKey(String dbId, IdentityModel identity, Object sourcePK);

}
