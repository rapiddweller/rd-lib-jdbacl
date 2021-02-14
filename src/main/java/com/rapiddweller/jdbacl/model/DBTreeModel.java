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

import com.rapiddweller.common.TreeModel;

/**
 * {@link TreeModel} adapter for the hierarchy formed by the Composite pattern of
 * {@link DBObject} and {@link CompositeDBObject}.<br/><br/>
 * Created: 10.11.2010 10:17:14
 *
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class DBTreeModel implements TreeModel<DBObject> {

  /**
   * The Root.
   */
  final CompositeDBObject<?> root;

  /**
   * Instantiates a new Db tree model.
   *
   * @param root the root
   */
  public DBTreeModel(CompositeDBObject<?> root) {
    this.root = root;
  }

  @Override
  public DBObject getRoot() {
    return root;
  }

  @Override
  public DBObject getParent(DBObject child) {
    return child.getOwner();
  }

  @Override
  public boolean isLeaf(DBObject node) {
    return !(node instanceof CompositeDBObject);
  }

  @Override
  public int getChildCount(DBObject parent) {
    return ((CompositeDBObject<?>) parent).getComponents().size();
  }

  @Override
  public DBObject getChild(DBObject parent, int index) {
    return ((CompositeDBObject<?>) parent).getComponents().get(index);
  }

  @Override
  public int getIndexOfChild(DBObject parent, DBObject child) {
    return ((CompositeDBObject<?>) parent).getComponents().indexOf(child);
  }

}
