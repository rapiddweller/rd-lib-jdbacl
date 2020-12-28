/*
 * (c) Copyright 2010-2011 by Volker Bergmann. All rights reserved.
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

import com.rapiddweller.commons.ConfigurationError;
import com.rapiddweller.jdbacl.SQLUtil;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBRow;
import com.rapiddweller.jdbacl.model.DBTable;

/**
 * Simple implementation of a transcoding mechanism.<br/><br/>
 * Created: 08.12.2010 18:45:49
 * @since 0.6.4
 * @author Volker Bergmann
 */
public class SimpleTranscoder {

	public static void transcode(DBRow row, String nk, Object newPK, String sourceDbId, IdentityProvider identityProvider, KeyMapper mapper) {
		DBTable table = row.getTable();
		String tableName = table.getName();
		IdentityModel identity = identityProvider.getIdentity(tableName);
		if (identity == null)
			throw new ConfigurationError("No identity defined for table " + tableName);
		
		// transcode primary key
		mapper.store("s", identity, nk, row.getPKValue(), newPK);
		row.setPKValue(newPK);
		
		// transcode references
		for (DBForeignKeyConstraint fk : table.getForeignKeyConstraints()) {
			String refereeTable = fk.getRefereeTable().getName();
			Object sourceRef = row.getFKValue(fk);
			if (sourceRef != null) {
				IdentityModel sourceTable = identityProvider.getIdentity(refereeTable);
				String sourceRefNK = mapper.getNaturalKey(sourceDbId, sourceTable, sourceRef);
				Object targetRef = mapper.getTargetPK(sourceTable, sourceRefNK);
				if (targetRef == null) {
					String message = "No mapping found for " + sourceDbId + '.' + refereeTable + "#" + sourceRef + 
						" referred in " + table.getName() + SQLUtil.renderColumnNames(fk.getColumnNames()) + ". " +
						"Probably has not been in the result set of the former '" + refereeTable + "' nk query.";
					throw new RuntimeException(message);
				}
				row.setFKValue(fk, targetRef);
			}
		}

    }

	/* TODO v1.0 use this for merging
	@Override
    public void merge(Connection source, Connection target, int pageSize,
			KeyMapper mapper, Context context) {
		String activity = "Merging " + name + " from " + source.getId() + " to " + target.getId();
		startActivity(activity);
		// iterate owners
		TypedIterable<Entity> ownerEntities = source.queryEntities(ownerTableName, null, context);
		Iterator<Entity> ownerIterator = ownerEntities.iterator();
	    while (ownerIterator.hasNext()) {
	    	Entity ownerEntity = ownerIterator.next();
	    	Object ownerId = ownerEntity.idComponentValues();
			String ownerNK = mapper.getNaturalKey(source, getOwnerTable(), ownerId);
			
	    	// iterate subset
	    	String sql = subNkPkQuery.replace("?", String.valueOf(ownerId)); // TODO v1.1 use prep stmt & handle composite keys
	    	TypedIterable<Object[]> ownedEntities = source.queryRows(sql, context);
	    	Iterator<Object[]> ownedIterator = ownedEntities.iterator();
	    	while (ownedIterator.hasNext()) {
	    		Object[] ownedNkPkRow = ownedIterator.next();
	    		Entity ownedEntity = source.queryEntityById(name, extractPK(ownedNkPkRow));
		    	Object sourceId = ownedEntity.idComponentValues();
		    	if (ownerNK != null) {
		    		String nk = ownerNK + '|' + String.valueOf(ownedNkPkRow[0]);
					Object targetId = mapper.getTargetPK(this, nk);
					if (targetId == null) {
		                handleNKNotFound(nk, name, source, target);
						continue;
	                } else {
						Entity targetEntity = target.queryEntityById(name, targetId);
						String message = checkEquivalence(ownedEntity, targetEntity, source, nk, mapper);
						if (message != null) {
							handleNonEquivalence(message, source.getId(), ownedEntity);
						} else
				        count++;

	                }
					mapper.store(source, this, nk, sourceId, targetId);
			        if (count % pageSize == 0)
			        	target.flush();
		    	} else {
		    		handleMissingOwner(name, ownedEntity, ownerTableName, ownerId, source);	    	
	    		}
	    	}
			IOUtil.close((Closeable) ownedIterator);
	    }
		IOUtil.close((Closeable) ownerIterator);
    	target.flush();
		endActivity(activity, source.countEntities(name));
    }

    protected String checkEquivalence(DBRow sourceEntity, DBRow targetEntity, String sourceDbId, String nk, KeyMapper mapper) {
		sourceEntity = mapper.transcode(sourceEntity, table, sourceDbId, module);
		if (sourceEntity == null)
			return null;
		StringBuilder message = new StringBuilder();
	    for (String columnName : NameUtil.getNames(table.getColumns())) {
	    	if (irrelevantColumns.contains(columnName) || pkColumns.contains(columnName))
	    		continue;
	    	Object sourceValue = sourceEntity.getCellValue(columnName);
	    	Object targetValue = targetEntity.getCellValue(columnName);
	    	if (!equals(sourceValue, targetValue)) {
	    		if (message.length() == 0)
	    			message.append("Inconsistent columns: ");
	    		else
	    			message.append(", ");
	    		
	    		message.append(table.getName() + '[' + nk + "]." + columnName + " " +
	    				sourceDbId + ':' + sourceValue + ", target:" + targetValue);
	    	}
	    }
	    return (message.length() > 0 ? message.toString() : null);
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean equals(Object o1, Object o2) {
	    if (o1 == null)
	    	return o2 == null;
	    else if (o2 == null)
	    	return false;
	    else if (o1 instanceof Comparable) // this is needed since for BigDecimals 1 != 1.0
	    	return ((Comparable) o1).compareTo(o2) == 0;
	    else
	    	return o1.equals(o2);
    }

*/

}
