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

package com.rapiddweller.jdbacl;

import java.util.List;

import com.rapiddweller.common.TreeModel;
import com.rapiddweller.jdbacl.model.CompositeDBObject;
import com.rapiddweller.jdbacl.model.DBObject;
import com.rapiddweller.jdbacl.model.Database;

/**
 * {@link TreeModel} adapter for DBObject hierarchies.<br/><br/>
 * Created: 02.12.2010 06:56:28
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class DatabaseTreeModel implements TreeModel<DBObject> {
	
	final Database database;

	public DatabaseTreeModel(Database database) {
		this.database = database;
	}

	@Override
	public DBObject getRoot() {
		return database;
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
		List<?> components = ((CompositeDBObject<?>) parent).getComponents();
		return components.indexOf(child);
	}

}
