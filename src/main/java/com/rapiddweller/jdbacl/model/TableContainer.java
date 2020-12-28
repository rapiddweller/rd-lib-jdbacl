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

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a database container which may contain tables or other containers.<br/><br/>
 * Created: 05.12.2010 11:06:48
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class TableContainer extends AbstractCompositeDBObject<ContainerComponent> implements ContainerComponent, TableHolder, SequenceHolder {

    private static final long serialVersionUID = 5890222751656809426L;
    
    final TableContainerSupport support;
    
    // constructors ----------------------------------------------------------------------------------------------------

    public TableContainer(String name) {
        this(name, null);
    }

    public TableContainer(String name, CompositeDBObject<? extends DBObject> parent) {
    	super(name, "container");
    	if (parent instanceof TableContainer)
			((TableContainer) parent).addSubContainer(this);
    	this.support = new TableContainerSupport();
    }

    private void addSubContainer(TableContainer subContainer) {
		support.addSubContainer(subContainer);
	}

	public DBSchema getSchema() {
		CompositeDBObject<?> parent = getOwner();
        while (parent != null && !(parent instanceof DBSchema))
        	parent = parent.getOwner();
        return (DBSchema) parent;
    }

	public DBCatalog getCatalog() {
        return getSchema().getCatalog();
    }

    // CompositeDBObject implementation --------------------------------------------------------------------------------

	@Override
	public List<ContainerComponent> getComponents() {
		List<ContainerComponent> result = new ArrayList<>();
		result.addAll(support.getTables());
		result.addAll(support.getSubContainers());
		return result;
	}
	
    // table operations ------------------------------------------------------------------------------------------------

    @Override
	public List<DBTable> getTables() {
        return support.getTables();
    }

    @Override
	public List<DBTable> getTables(boolean recursive) {
		return support.getTables(recursive);
    }

    public void getTables(boolean recursive, List<DBTable> result) {
        support.getTables(recursive, result);
    }

    @Override
	public DBTable getTable(String tableName) {
        return support.getTable(tableName);
    }

    public void addTable(DBTable table) {
        support.addTable(table);
    }

    public void removeTable(DBTable table) {
        support.removeTable(table);
    }

    // sequence operations ---------------------------------------------------------------------------------------------
    
	@Override
	public List<DBSequence> getSequences(boolean recursive) {
		return support.getSequences(recursive);
	}

	public void getSequences(boolean recursive, List<DBSequence> result) {
        support.getSequences(recursive, result);
    }

}
