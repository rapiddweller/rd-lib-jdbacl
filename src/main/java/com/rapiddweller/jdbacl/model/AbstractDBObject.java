/*
 * (c) Copyright 2010-2014 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.common.NullSafeComparator;

/**
 * Abstract implementation of the {@link DBObject} interface which serves as
 * parent class for concrete implementations.<br/><br/>
 * Created: 09.11.2010 11:45:20
 * @author Volker Bergmann
 * @since 0.6.4
 */
public abstract class AbstractDBObject implements DBObject {

  protected String name;
  protected final String objectType;
  protected String doc;
  protected CompositeDBObject<?> owner;

  // constructors ----------------------------------------------------------------------------------------------------

  protected AbstractDBObject(String name, String objectType) {
    this(name, objectType, null);
  }

  @SuppressWarnings({"rawtypes"})
  protected AbstractDBObject(String name, String objectType, CompositeDBObject owner) {
    this.name = name;
    this.objectType = objectType;
    setOwner(owner); // allow child classes to do additional work when setting the owner
  }

  // properties ------------------------------------------------------------------------------------------------------

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getObjectType() {
    return objectType;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getDoc() {
    return doc;
  }

  public void setDoc(String doc) {
    this.doc = doc;
  }

  @Override
  public CompositeDBObject<?> getOwner() {
    return owner;
  }

  @Override
  public void setOwner(CompositeDBObject<?> owner) {
    this.owner = owner;
  }

  // java.lang.Object overrides --------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return name;
  }

  @Override
  public int hashCode() {
    return (name != null ? name.hashCode() : 0);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof DBObject)) {
      return false;
    }
    DBObject that = (DBObject) obj;
    return NullSafeComparator.equals(this.name, that.getName()) &&
        NullSafeComparator.equals(this.objectType, that.getObjectType()) &&
        NullSafeComparator.equals(this.owner, that.getOwner());
  }

}
