/*
 * (c) Copyright 2006-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.jdbc;

import com.rapiddweller.common.ObjectNotFoundException;
import com.rapiddweller.jdbacl.model.DBCatalog;
import com.rapiddweller.jdbacl.model.DBSchema;
import com.rapiddweller.jdbacl.model.DBTable;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created: 13.01.2007 23:22:55
 * @author Volker Bergmann
 */
class ImportedKey {
    
    private static final Logger logger = LogManager.getLogger(ImportedKey.class);
    
    private DBTable pkTable;

    /** primary key table catalog being imported (may be null) */
    public String pktable_cat;

    /** primary key table schema being imported (may be null) */
    public String pktable_schem;

    /** primary key table name being imported */
    public String pktable_name;

    /** primary key column name being imported */
    public String pkcolumn_name;

    /** foreign key table catalog (may be null) */
    public String fktable_cat;

    /** foreign key table schema (may be null) */
    public String fktable_schem;

    /** foreign key table name */
    public String fktable_name;

    /** foreign key column name */
    public String fkcolumn_name;

    /** sequence number within a foreign key */
    public short key_seq;

    /**
     * What happens to a foreign key when the primary key is updated:
     * <UL>
     *   <LI>importedNoAction - do not allow update of primary key if it has been imported</LI>
     *   <LI>importedKeyCascade - change imported key to agree with primary key update</LI>
     *   <LI>importedKeySetNull - change imported key to NULL if its primary key has been updated</LI>
     *   <LI>importedKeySetDefault - change imported key to default values if its primary key has been updated</LI>
     *   <LI>importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)</LI>
     * </UL>
     */
     public short update_rule;

    /**
     * What happens to the foreign key when primary is deleted.
     * <UL>
     *   <LI>importedKeyNoAction - do not allow delete of primary key if it has been imported</LI>
     *   <LI>importedKeyCascade - delete rows that import a deleted key</LI>
     *   <LI>importedKeySetNull - change imported key to NULL if its primary key has been deleted</LI>
     *   <LI>importedKeyRestrict - same as importedKeyNoAction (for ODBC 2.x compatibility)</LI>
     *   <LI>importedKeySetDefault - change imported key to default if its primary key has been deleted</LI>
     * </UL>
     */
    public short delete_rule;

    /** foreign key name (may be null) */
    public String fk_name;

    /** primary key name (may be null) */
    public String pk_name;

    /**
     * can the evaluation of foreign key constraints be deferred until commit
     * <UL>
     *   <LI>importedKeyInitiallyDeferred - see SQL92 for definition</LI>
     *   <LI>importedKeyInitiallyImmediate - see SQL92 for definition</LI>
     *   <LI>importedKeyNotDeferrable - see SQL92 for definition</LI>
     * </UL>
     */
    public short deferrablibity;

    private final List<String> foreignKeyColumnNames = new ArrayList<>();
    private final List<String> refereeColumnNames = new ArrayList<>();

    public void addForeignKeyColumn(String foreignKeyColumnName, String targetColumnName) {
        foreignKeyColumnNames.add(foreignKeyColumnName);
        refereeColumnNames.add(targetColumnName);
    }

    public List<String> getForeignKeyColumnNames() {
        return foreignKeyColumnNames;
    }

    public List<String> getRefereeColumnNames() {
        return refereeColumnNames;
    }

    public static ImportedKey parse(ResultSet resultSet, DBCatalog catalog, DBSchema schema, DBTable fkTable) throws SQLException {
        ImportedKey key = new ImportedKey();
        key.pktable_cat = resultSet.getString(1);
        key.pktable_schem = resultSet.getString(2);
        key.pktable_name = resultSet.getString(3);
        key.pkcolumn_name = resultSet.getString(4);
        key.fktable_cat = resultSet.getString(5);
        key.fktable_schem = resultSet.getString(6);
        key.fktable_name = resultSet.getString(7);
        assert key.fktable_name.equals(fkTable.getName());
        key.fkcolumn_name = resultSet.getString(8);
        key.key_seq = resultSet.getShort(9);
        key.update_rule = resultSet.getShort(10);
        key.delete_rule = resultSet.getShort(11);
        key.fk_name = resultSet.getString(12);
        key.pk_name = resultSet.getString(13);
        key.deferrablibity = resultSet.getShort(14);
        if (logger.isDebugEnabled())
            logger.debug("found imported key " 
                    + key.pktable_cat + ", " + key.pktable_schem + ", " + key.pktable_name + ", " + key.pkcolumn_name + ", " 
                    + key.fktable_cat + ", " + key.fktable_schem + ", " + key.fktable_name + ", " + key.fkcolumn_name + ", "
                    + key.key_seq + ", " + key.update_rule + ", " + key.delete_rule + ", " 
                    + key.fk_name + ", " + key.pk_name + ", "
                    + key.deferrablibity
            );
        if (!key.fktable_name.equalsIgnoreCase(fkTable.getName()))	// Fix for Firebird:  
        	return null;											// When querying X, it returns the foreign keys of XY to

        key.pkTable = null;
        try {
        if (catalog != null)
        	key.pkTable = catalog.getTable(key.pktable_name);
        else
        	key.pkTable = schema.getTable(key.pktable_name);
        } catch (ObjectNotFoundException e) {
        	throw new ObjectNotFoundException("Table " + key.pktable_name + " is referenced by table " + 
        			key.fktable_name + " but not found in the database. Possibly it was filtered out?");
        }
        key.addForeignKeyColumn(key.fkcolumn_name, key.pkcolumn_name);
        return key;
    }
    
    public DBTable getPkTable() {
	    return pkTable;
    }

    @Override
    public String toString() {
        return fktable_cat + "." + fktable_schem + "." + fktable_name + "." + fkcolumn_name +
        	" -> " + pktable_cat + "." + pktable_schem + "." + pktable_name + "." + pkcolumn_name; 
    }

}
