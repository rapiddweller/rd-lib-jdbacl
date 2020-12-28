/*
 * (c) Copyright 2012 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.test;

import java.util.Map;

import com.rapiddweller.commons.collection.NameMap;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;

/**
 * Helper class which provides typical models for testing.<br/><br/>
 * Created: 07.06.2012 21:39:08
 * @since 0.8.3
 * @author Volker Bergmann
 */
public class ModelTestUtil {
	
	public static Map<String, DBTable> createCountryStateCityWithCompositePK() {
		DBDataType INT = DBDataType.getInstance("INT");
		
		DBTable country = new DBTable("country");
		new DBColumn("co_id1", country, INT);
		new DBColumn("co_id2", country, INT);
		new DBPrimaryKeyConstraint(country, "COUNTRY_PK", true, "co_id1", "co_id2");
		
		DBTable state = new DBTable("state");
		new DBColumn("st_id1", state, INT);
		new DBColumn("st_id2", state, INT);
		new DBColumn("co_fk1", state, INT);
		new DBColumn("co_fk2", state, INT);
		new DBPrimaryKeyConstraint(state, "STATE_PK", true, "st_id1", "st_id2");
		new DBForeignKeyConstraint("ST_CO_FK", true, state, new String[] { "co_fk1", "co_fk2" }, country, new String[] { "co_id1", "co_id2" });
		
		DBTable city = new DBTable("city");
		new DBColumn("id", city, INT);
		new DBPrimaryKeyConstraint(state, "CITY_PK", true, "ci_id");
		new DBForeignKeyConstraint("CI_ST_FK", true, city, new String[] { "st_fk1", "st_fk2" }, state, new String[] { "st_id1", "st_id2" });
		new DBForeignKeyConstraint("CI_CO_FK", true, city, new String[] { "co_fk1", "co_fk2" }, country, new String[] { "co_id1", "co_id2" });
		
		return new NameMap<>(country, state, city);
	}
}
