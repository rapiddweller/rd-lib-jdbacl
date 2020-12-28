/*
 * (c) Copyright 2011-2014 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl.model.csv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import com.rapiddweller.commons.FileUtil;
import com.rapiddweller.commons.IOUtil;
import com.rapiddweller.formats.csv.CSVUtil;
import com.rapiddweller.jdbacl.SQLUtil;
import com.rapiddweller.jdbacl.model.DBCheckConstraint;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBForeignKeyConstraint;
import com.rapiddweller.jdbacl.model.DBIndex;
import com.rapiddweller.jdbacl.model.DBMetaDataExporter;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBSequence;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.model.DBUniqueConstraint;
import com.rapiddweller.jdbacl.model.Database;
import com.rapiddweller.jdbacl.model.FKChangeRule;

/**
 * Exports a {@link Database} meta data structure into a group of CSV files:
 * columns.csv, primary_keys.csv, unique_keys.csv, foreign_keys.csv, indexes.csv, 
 * checks.csv and sequences.csv.<br/><br/>
 * Created: 25.10.2011 14:40:37
 * @since 0.6.13
 * @author Volker Bergmann
 */
public class CSVModelExporter implements DBMetaDataExporter {
	
	final File rootDirectory;
	
	public CSVModelExporter(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	@Override
	public void export(Database database) throws IOException {
		FileUtil.ensureDirectoryExists(rootDirectory);
		exportColumns(database);
		exportPrimaryKeys(database);
		exportUniqueKeys(database);
		exportForeignKeys(database);
		exportChecks(database);
		exportIndexes(database);
		exportSequences(database);
	}

	private void exportColumns(Database database) throws IOException {
		File file = new File(rootDirectory, "colums.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', "catalog", "schema", "table", "column_name", 
					"type", "jdbc_type", "nullable", "defaultValue"));
			for (DBTable table : database.getTables()) {
				for (DBColumn column : table.getColumns()) {
					CSVUtil.writeRow(out, ',', 
						table.getCatalog().getName(),
						table.getSchema().getName(),
						table.getName(),
						column.getName(),
						SQLUtil.renderColumnTypeWithSize(column),
						String.valueOf(column.getType().getJdbcType()),
						String.valueOf(column.isNullable()),
						column.getDefaultValue());
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private void exportPrimaryKeys(Database database) throws IOException {
		File file = new File(rootDirectory, "primary_keys.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', "catalog", "schema", "table", "pk_name", "column_name"));
			for (DBTable table : database.getTables()) {
				DBPrimaryKeyConstraint pk = table.getPrimaryKeyConstraint();
				if (pk == null)
					continue;
				for (String columnName : pk.getColumnNames()) {
					CSVUtil.writeRow(out, ',', 
						table.getCatalog().getName(),
						table.getSchema().getName(),
						table.getName(),
						pk.getName(),
						columnName);
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private void exportUniqueKeys(Database database) throws IOException {
		File file = new File(rootDirectory, "unique_keys.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', "catalog", "schema", "table", "uk_name", "column_name"));
			for (DBTable table : database.getTables()) {
				for (DBUniqueConstraint uk : table.getUniqueConstraints(false)) {
					for (String columnName : uk.getColumnNames()) {
						CSVUtil.writeRow(out, ',', 
							table.getCatalog().getName(),
							table.getSchema().getName(),
							table.getName(),
							uk.getName(),
							columnName);
					}
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private void exportForeignKeys(Database database) throws IOException {
		File file = new File(rootDirectory, "foreign_keys.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', 
					"catalog", "schema", "table", "fk_name", "column", 
					"refereeCatalog", "refereeSchema", "refereeTable", "refereeColumn",
					"updateRule", "deleteRule"));
			for (DBTable table : database.getTables()) {
				for (DBForeignKeyConstraint fk : table.getForeignKeyConstraints()) {
					String[] columnNames = fk.getColumnNames();
					DBTable refereeTable = fk.getRefereeTable();
					String[] refereeColumnNames = fk.getRefereeColumnNames();
					for (int i = 0; i < columnNames.length; i++) {
						String columnName = columnNames[i];
						String refereeColumnName = refereeColumnNames[i];
						CSVUtil.writeRow(out, ',', 
							table.getCatalog().getName(),
							table.getSchema().getName(),
							table.getName(),
							fk.getName(),
							columnName,
							refereeTable.getCatalog().getName(),
							refereeTable.getSchema().getName(),
							refereeTable.getName(),
							refereeColumnName,
							renderChangeRule(fk.getUpdateRule()),
							renderChangeRule(fk.getDeleteRule()));
					}
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private static String renderChangeRule(FKChangeRule rule) {
		switch (rule) {
			case NO_ACTION   : return "";
			case CASCADE     : return "CASCADE";
			case SET_NULL    : return "SET NULL";
			case SET_DEFAULT : return "SET DEFAULT";
			default: throw new IllegalArgumentException("Not a supported change rule: " + rule);
		}
	}

	private void exportChecks(Database database) throws IOException {
		File file = new File(rootDirectory, "checks.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', "catalog", "schema", "table", "check"));
			for (DBTable table : database.getTables()) {
				for (DBCheckConstraint check : table.getCheckConstraints()) {
					CSVUtil.writeRow(out, ',', 
						table.getCatalog().getName(),
						table.getSchema().getName(),
						table.getName(),
						check.getName(),
						SQLUtil.normalize(check.getConditionText(), true));
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private void exportIndexes(Database database) throws IOException {
		File file = new File(rootDirectory, "indexes.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', 
					"catalog", "schema", "table", "index_name", "index_unique", "column_name"));
			for (DBTable table : database.getTables()) {
				for (DBIndex index : table.getIndexes()) {
					for (String columnName : index.getColumnNames()) {
						CSVUtil.writeRow(out, ',', 
							table.getCatalog().getName(),
							table.getSchema().getName(),
							table.getName(),
							index.getName(),
							String.valueOf(index.isUnique()),
							columnName);
					}
				}
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private void exportSequences(Database database) throws IOException {
		File file = new File(rootDirectory, "sequences.csv");
		PrintWriter out = null;
		try {
			out = createPrintWriter(file);
			out.print(CSVUtil.formatHeaderWithLineFeed(',', 
					"catalog", "schema", "name", 
					"start", "increment", "maxValue", "minValue", "cycle", "cache", "order", "lastNumber"));
			for (DBSequence sequence : database.getSequences()) {
				CSVUtil.writeRow(out, ',', 
					sequence.getCatalogName(),
					sequence.getSchemaName(),
					sequence.getName(),
					String.valueOf(sequence.getStart()),
					String.valueOf(sequence.getIncrement()),
					String.valueOf(sequence.getMaxValue()),
					String.valueOf(sequence.getMinValue()),
					String.valueOf(sequence.isCycle()),
					String.valueOf(sequence.getCache()),
					String.valueOf(sequence.isOrder()),
					String.valueOf(sequence.getLastNumber())
				);
			}
		} finally {
			IOUtil.close(out);
		}
	}

	private static PrintWriter createPrintWriter(File file) throws IOException {
		return new PrintWriter(new BufferedWriter(new FileWriter(file)));
	}

}
