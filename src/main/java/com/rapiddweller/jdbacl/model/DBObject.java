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

package com.rapiddweller.jdbacl.model;

import java.io.Serializable;

import com.rapiddweller.commons.Named;

/**
 * Abstract parent interface for assembling database objects in a tree using the Composite design pattern. 
 * The corresponding interface for Composite objects is {@link CompositeDBObject}.<br/><br/>
 * Created: 09.11.2010 11:41:09
 * @since 0.6.4
 * @author Volker Bergmann
 */
public interface DBObject extends Named, Serializable {
	
	/** @return the type of the DBObject as used in DDL in lower case letters. */
    String getObjectType();
    
    /** @return documentation of the DBObject if available, otherwise null. */
    String getDoc();
    
    /** @return the owner of the DBObject instance or null if no owner has been set. */
    CompositeDBObject<?> getOwner();
    
    /** sets the owner of the DBObject instance. */
    void setOwner(CompositeDBObject<?> owner);
	
	/** tells if an object has the same definition as another one. */
    boolean isIdentical(DBObject other);
	
}
