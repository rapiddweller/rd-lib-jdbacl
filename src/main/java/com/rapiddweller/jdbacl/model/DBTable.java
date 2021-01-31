/*
 * (c) Copyright 2006-2012 by Volker Bergmann. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, is permitted under the terms of the
 * GNU General Public License.
 *
 * For redistributing this software or a derivative work under a license other
 * than the GPL-compatible Free Software License as defined by the Free
 * Software Foundation or approved by OSI, you must first obtain a commercial
 * license to this software product from Volker Bergmann.
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

import com.rapiddweller.common.*;
import com.rapiddweller.common.bean.HashCodeBuilder;
import com.rapiddweller.common.collection.OrderedNameMap;
import com.rapiddweller.common.depend.Dependent;
import com.rapiddweller.common.iterator.ConvertingIterator;
import com.rapiddweller.common.iterator.TabularIterator;
import com.rapiddweller.jdbacl.*;
import com.rapiddweller.jdbacl.model.jdbc.DBIndexInfo;
import com.rapiddweller.jdbacl.model.jdbc.JDBCDBImporter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a database table.<br/><br/>
 * Created: 06.01.2007 08:58:49
 * @author Volker Bergmann
 */
public class DBTable extends AbstractCompositeDBObject<DBTableComponent> 
		implements ContainerComponent, MultiColumnObject, Dependent<DBTable> {

    private static final long serialVersionUID = 1259670951314570432L;

	private static final String[] EMPTY_ARRAY = new String[0];
    
	private TableType tableType;
	private final JDBCDBImporter importer;
	
	private OrderedNameMap<DBColumn> columns;
	private boolean pkImported;
    private DBPrimaryKeyConstraint pk;
    private OrderedSet<DBUniqueConstraint> uniqueConstraints;
    private OrderedSet<DBForeignKeyConstraint> foreignKeyConstraints;
    private OrderedNameMap<DBIndex> indexes;
    private Set<DBTable> referrers;
	private List<DBCheckConstraint> checkConstraints;

	public DBTable(String name) {
        this(name, TableType.TABLE, null);
    }
	
	public DBTable(String name, TableType type, DBSchema schema) {
        this(name, TableType.TABLE, null, schema, null);
    }
	
	public DBTable(String name, TableType type, String doc, DBSchema schema, JDBCDBImporter importer) {
        super(name, "table", schema);
	    this.importer = importer;
	    this.name = name;
	    this.tableType = type;
	    this.doc = doc;
        this.tableType = type;
        this.pkImported = false;
        if (schema != null)
        	schema.addTable(this);
    }
	
	
	
	// CompositeDBObject interface -------------------------------------------------------------------------------------
	
	@Override
	public List<DBTableComponent> getComponents() {
		List<DBTableComponent> result = new ArrayList<>();
		result.addAll(getColumns());
		havePKImported();
		if (pk != null)
			result.add(pk);
		haveIndexesImported();
		result.addAll(uniqueConstraints);
		result.addAll(indexes.values());
		haveFKsImported();
		result.addAll(foreignKeyConstraints);
		return result;
	}
	
	
	
    // table related methods -------------------------------------------------------------------------------------------

	public DBCatalog getCatalog() {
		return getSchema().getCatalog();
	}

	public DBSchema getSchema() {
        return (DBSchema) getOwner();
    }

    public void setSchema(DBSchema schema) {
        setOwner(schema);
    }

    public TableType getTableType() {
		return tableType;
	}
    
    
    
    // column methods --------------------------------------------------------------------------------------------------
	
	@Override
	public String[] getColumnNames() {
		haveColumnsImported();
		return CollectionUtil.toArray(NameUtil.getNames(columns.values()), String.class);
	}
	
    public List<DBColumn> getColumns() {
		haveColumnsImported();
		return columns.values();
    }

    public DBColumn[] getColumns(String[] columnNames) {
		haveColumnsImported();
        List<DBColumn> list = new ArrayList<>(columnNames.length);
        for (String columnName : columnNames) {
            DBColumn column = getColumn(columnName);
            if (column == null)
                throw new IllegalArgumentException("Table '" + name + "' does not have a column '" + columnName + "'");
            list.add(column);
        }
        DBColumn[] array = new DBColumn[columnNames.length];
        return list.toArray(array);
    }

    public DBColumn getColumn(String columnName) {
		haveColumnsImported();
        DBColumn column = columns.get(columnName);
        if (column == null)
            throw new ObjectNotFoundException("Column '" + columnName + 
                    "' not found in table '" + this.getName() + "'");
        return column;
    }

    public void addColumn(DBColumn column) {
		haveColumnsImported();
        receiveColumn(column);
    }

	public void receiveColumn(DBColumn column) {
		if (columns == null)
			columns = OrderedNameMap.createCaseIgnorantMap();
		column.setTable(this);
        columns.put(column.getName(), column);
	}
	
	public boolean areColumnsImported() {
		return (columns != null);
	}

	public void setColumnsImported(boolean columnsImported) {
		if (columnsImported)
			this.columns = OrderedNameMap.<DBColumn>createCaseIgnorantMap();
		else
			this.columns = null;
	}
	
	public void haveColumnsImported() {
		if (columns == null) {
			columns = OrderedNameMap.createCaseIgnorantMap();
			if (importer != null)
				importer.importColumnsOfTable(this, new ColReceiver());
		}
    }
	
	class ColReceiver implements JDBCDBImporter.ColumnReceiver {
		@Override
		public void receiveColumn(String columnName, DBDataType dataType,
				Integer columnSize, Integer fractionDigits, boolean nullable,
				String defaultValue, String comment, DBTable table) {
			DBColumn column = new DBColumn(columnName, table, dataType, columnSize, fractionDigits);
	        column.setTable(DBTable.this);
	        columns.put(column.getName(), column);
	        column.setDoc(comment);
	        column.setNullable(nullable);
	        column.setDefaultValue(defaultValue);
		}
	}
	
	
	
	// primary key -----------------------------------------------------------------------------------------------------
	
    public void setPrimaryKey(DBPrimaryKeyConstraint constraint) {
    	havePKImported();
        this.pk = constraint;
    }

    public DBPrimaryKeyConstraint getPrimaryKeyConstraint() {
    	havePKImported();
    	return pk;
    }

	public String[] getPKColumnNames() {
		DBPrimaryKeyConstraint pk = getPrimaryKeyConstraint();
		return (pk != null ? pk.getColumnNames() : EMPTY_ARRAY);
	}
	
	public boolean isPKImported() {
		return pkImported;
	}
	
	public void setPKImported(boolean pkImported) {
		this.pkImported = pkImported;
	}
	
	public void havePKImported() {
		if (!isPKImported()) {
			haveColumnsImported();
			if (importer != null)
				importer.importPrimaryKeyOfTable(this, new PKRec());
			pkImported = true;
		}
    }

	class PKRec implements JDBCDBImporter.PKReceiver {

		@Override
		public void receivePK(String pkName, boolean deterministicName, String[] columnNames, DBTable table) {
			DBPrimaryKeyConstraint pk = new DBPrimaryKeyConstraint(null, pkName, deterministicName, columnNames);
			DBTable.this.pk = pk;
			pk.setTable(DBTable.this);
			for (String columnName : columnNames) {
				DBColumn column = table.getColumn(columnName);
			    column.addUkConstraint(pk);
			}
		}
		
	}
	
	
	
    // uniqueConstraint operations -------------------------------------------------------------------------------------

    public Set<DBUniqueConstraint> getUniqueConstraints(boolean includePK) {
    	haveIndexesImported();
    	Set<DBUniqueConstraint> result = new HashSet<>(uniqueConstraints);
    	if (includePK && pk != null)
    		result.add(pk);
    	return result;
    }

	public DBUniqueConstraint getUniqueConstraint(String[] columnNames) {
		haveIndexesImported();
		if (pk != null && StringUtil.equalsIgnoreCase(columnNames, pk.getColumnNames()))
			return pk;
		for (DBUniqueConstraint constraint : uniqueConstraints)
			if (StringUtil.equalsIgnoreCase(columnNames, constraint.getColumnNames()))
				return constraint;
		return null;
	}

	public DBUniqueConstraint getUniqueConstraint(String name) {
		haveIndexesImported();
		if (name.equalsIgnoreCase(pk.getName()))
			return pk;
		for (DBUniqueConstraint constraint : uniqueConstraints)
			if (name.equals(constraint.getName()))
				return constraint;
		return null;
	}

	public void addUniqueConstraint(DBUniqueConstraint uk) {
		haveIndexesImported();
		uk.setTable(this);
		if (uk instanceof DBPrimaryKeyConstraint)
			setPrimaryKey((DBPrimaryKeyConstraint) uk);
		uniqueConstraints.add(uk);
    }

    public void removeUniqueConstraint(DBUniqueConstraint constraint) {
    	haveIndexesImported();
        uniqueConstraints.remove(constraint.getName());
    }
    
    
    
    // index operations ------------------------------------------------------------------------------------------------

    public List<DBIndex> getIndexes() {
    	haveIndexesImported();
        return new ArrayList<>(indexes.values());
    }

    public DBIndex getIndex(String indexName) {
    	haveIndexesImported();
        return indexes.get(indexName);
    }

    public void addIndex(DBIndex index) {
    	haveIndexesImported();
    	index.setTable(this);
        indexes.put(index.getName(), index);
    }

    public void removeIndex(DBIndex index) {
    	haveIndexesImported();
        indexes.remove(index.getName());
    }

	private void haveIndexesImported() {
		if (!areIndexesImported()) {
			haveColumnsImported();
			this.uniqueConstraints = new OrderedSet<>();
			this.indexes = OrderedNameMap.createCaseIgnorantMap();
			JDBCDBImporter.IndexReceiver receiver = new IdxReceiver();
			if (importer != null)
				importer.importIndexesOfTable(this, false, receiver);
		}
    }

	public boolean areIndexesImported() {
		return (this.indexes != null);
	}
	
	public void setIndexesImported(boolean indexesImported) {
		if (indexesImported) {
			this.uniqueConstraints = new OrderedSet<>();
			this.indexes = OrderedNameMap.createCaseIgnorantMap();
		} else {
			this.uniqueConstraints = null;
			this.indexes = null;
		}
	}

	static class IdxReceiver implements JDBCDBImporter.IndexReceiver {
		@Override
		public void receiveIndex(DBIndexInfo indexInfo, boolean deterministicName, DBTable table, DBSchema schema) {
			DBIndex index;
		    if (indexInfo.unique) {
		    	DBPrimaryKeyConstraint pk = table.getPrimaryKeyConstraint();
		    	boolean isPK = (pk != null && StringUtil.equalsIgnoreCase(indexInfo.columnNames, pk.getColumnNames()));
		    	DBUniqueConstraint constraint;
		    	if (isPK) {
		    		constraint = pk;
		    	} else {
		    		constraint = new DBUniqueConstraint(table, indexInfo.name, deterministicName, indexInfo.columnNames);
		    		table.addUniqueConstraint(constraint);
		    	}
				index = new DBUniqueIndex(indexInfo.name, deterministicName, constraint);
			} else {
		        index = new DBNonUniqueIndex(indexInfo.name, deterministicName, table, indexInfo.columnNames);
			}
			table.addIndex(index);
		}
	}
	
    // ForeignKeyConstraint operations ---------------------------------------------------------------------------------

    public Set<DBForeignKeyConstraint> getForeignKeyConstraints() {
    	haveFKsImported();
        return new HashSet<>(foreignKeyConstraints);
    }

	public DBForeignKeyConstraint getForeignKeyConstraint(String... columnNames) {
    	haveFKsImported();
		for (DBForeignKeyConstraint fk : foreignKeyConstraints)
			if (StringUtil.equalsIgnoreCase(fk.getColumnNames(), columnNames))
				return fk;
		throw new ObjectNotFoundException("Table '" + name + "' has no foreign key " +
				"with the columns (" + ArrayFormat.format(columnNames) + ")");
	}

    public void addForeignKey(DBForeignKeyConstraint constraint) {
    	haveFKsImported();
    	constraint.setTable(this);
        foreignKeyConstraints.add(constraint);
    }

    public void removeForeignKeyConstraint(DBForeignKeyConstraint constraint) {
    	haveFKsImported();
        foreignKeyConstraints.remove(constraint);
    }

	private void haveFKsImported() {
		if (!areFKsImported()) {
			haveColumnsImported();
	    	havePKImported();
			foreignKeyConstraints = new OrderedSet<>();
			if (importer != null)
				importer.importImportedKeys(this, new FKRec());
		}
    }

	public boolean areFKsImported() {
		return (foreignKeyConstraints != null);
	}
	
	public void setFKsImported(boolean fksImported) {
		this.foreignKeyConstraints = (fksImported ? new OrderedSet<>() : null);
	}
	
	class FKRec implements JDBCDBImporter.FKReceiver {

		@Override
		public void receiveFK(DBForeignKeyConstraint fk, DBTable table) {
			foreignKeyConstraints.add(fk);
			fk.setTable(table);
		}
		
	}
	
    // check constraint operations -------------------------------------------------------------------------------------

	public List<DBCheckConstraint> getCheckConstraints() {
		haveChecksImported();
		if (checkConstraints != null)
			return new ArrayList<>(checkConstraints);
		else
			return new ArrayList<>();
	}

	public void addCheckConstraint(DBCheckConstraint checkConstraint) {
		haveChecksImported();
		checkConstraint.setTable(this);
		receiveCheckConstraint(checkConstraint);
	}

	private void haveChecksImported() {
		if (!areChecksImported())
			getCatalog().getDatabase().haveChecksImported();
    }

	public boolean areChecksImported() {
		return (getCatalog().getDatabase().isChecksImported());
	}
	
	public void setChecksImported(boolean checksImported) {
		if (checksImported) {
			if (checkConstraints == null)
				this.checkConstraints = new ArrayList<>();
		} else
			this.checkConstraints = null;
	}
	
	public void receiveCheckConstraint(DBCheckConstraint check) {
		if (this.checkConstraints == null)
			this.checkConstraints = new ArrayList<>();
		this.checkConstraints.add(check);
	}

	
	
    // referrer operations ---------------------------------------------------------------------------------------------
    
    public Collection<DBTable> getReferrers() {
    	haveReferrersImported();
    	return new HashSet<>(referrers);
    }
    
	public void addReferrer(DBTable referrer) {
    	haveReferrersImported();
		receiveReferrer(referrer);
    }

	public void receiveReferrer(DBTable referrer) {
		if (referrers == null)
			referrers = new OrderedSet<>();
		referrers.add(referrer);
	}
    
	private void haveReferrersImported() {
		if (areReferrersImported()) {
			haveFKsImported();
			referrers = new OrderedSet<>();
			if (importer != null)
				importer.importRefererTables(this, new RefReceiver());
		}
    }

	public boolean areReferrersImported() {
		return (referrers == null);
	}
	
	public void setReferrersImported(boolean referrersImported) {
		if (referrersImported) {
			if (referrers == null)
				referrers = new OrderedSet<>();
		} else
			referrers = null;
	}

	class RefReceiver implements JDBCDBImporter.ReferrerReceiver {
		@Override
		public void receiveReferrer(String fktable_name, DBTable table) {
			DBTable referrer = getSchema().getCatalog().getTable(fktable_name);
			table.addReferrer(referrer);
		}
	}
	
	
	
	// implementation of the 'Dependent' interface ---------------------------------------------------------------------

    @Override
	public int countProviders() {
        return getForeignKeyConstraints().size();
    }

    @Override
	public DBTable getProvider(int index) {
        return foreignKeyConstraints.get(index).getRefereeTable();
    }

    @Override
	public boolean requiresProvider(int index) {
        String firstFkColumnName = foreignKeyConstraints.get(index).getForeignKeyColumnNames()[0];
		return !getColumn(firstFkColumnName).isNullable();
    }
    
    
    
    // row operations --------------------------------------------------------------------------------------------------

    public DBRowIterator allRows(Connection connection) throws SQLException {
        return new DBRowIterator(this, connection, null);
    }
    
	public DBRowIterator queryRows(String whereClause, Connection connection) throws SQLException {
        return new DBRowIterator(this, connection, whereClause);
	}

	public long getRowCount(Connection connection) {
		return DBUtil.countRows(name, connection);
	}

	public DBRow queryByPK(Object pk, Connection connection, DatabaseDialect dialect) throws SQLException {
    	String[] pkColumnNames = getPrimaryKeyConstraint().getColumnNames();
    	if (pkColumnNames.length == 0)
    		throw new ObjectNotFoundException("Table " + name + " has no primary key");
    	Object[] pkComponents = (pk.getClass().isArray() ? (Object[]) pk : new Object[] { pk });
		String whereClause = SQLUtil.renderWhereClause(pkColumnNames, pkComponents, dialect);
        DBRowIterator iterator = new DBRowIterator(this, connection, whereClause);
        if (!iterator.hasNext())
        	throw new ObjectNotFoundException("No " + name + " row with id (" + pkComponents + ")");
		DBRow result = iterator.next();
		iterator.close();
		return result;
    }
/*    
    public DBRowIterator queryRowsByCellValues(String[] columns, Object[] values, Connection connection) throws SQLException {
		String whereClause = SQLUtil.renderWhereClause(columns, values, dialect);
        return new DBRowIterator(this, connection, whereClause);
    }
*/ 
	public HeavyweightIterator<Object> queryPKValues(Connection connection) {
		StringBuilder query = new StringBuilder("select ");
		query.append(ArrayFormat.format(getPKColumnNames()));
		query.append(" from ").append(name);
    	Iterator<ResultSet> rawIterator = new QueryIterator(query.toString(), connection, 100);
        ResultSetConverter<Object> converter = new ResultSetConverter<>(Object.class, true);
    	return new ConvertingIterator<>(rawIterator, converter);
	}

	public TabularIterator query(String query, Connection connection) {
		Assert.notEmpty(query, "query");
		return new ArrayResultSetIterator(connection, query);
	}
	
	
	
	// java.lang.Object overrides --------------------------------------------------------------------------------------
	
	@Override
    public int hashCode() {
		return HashCodeBuilder.hashCode(owner, name);
    }

    @Override
	public boolean equals(Object other) {
	    if (this == other)
		    return true;
	    if (other == null || !(other instanceof DBTable))
		    return false;
	    DBTable that = (DBTable) other;
	    if (!NullSafeComparator.equals(this.owner, that.getSchema()))
	    	return false;
	    return NullSafeComparator.equals(this.name, that.getName());
    }

}
