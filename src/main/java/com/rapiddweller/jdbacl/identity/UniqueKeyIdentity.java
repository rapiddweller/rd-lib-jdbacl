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

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.ArrayUtil;
import com.rapiddweller.common.converter.ThreadSafeConverter;
import com.rapiddweller.common.exception.ExceptionFactory;
import com.rapiddweller.common.iterator.ConvertingIterator;
import com.rapiddweller.common.iterator.TabularIterator;
import com.rapiddweller.jdbacl.model.Database;

import java.sql.Connection;

/**
 * {@link IdentityModel} implementation based on a unique-key-constraint.<br/><br/>
 * Created: 06.12.2010 09:10:05
 * @author Volker Bergmann
 * @since 0.6.4
 */
public class UniqueKeyIdentity extends IdentityModel {

  private String[] columnNames;

  public UniqueKeyIdentity(String tableName, String... columnNames) {
    super(tableName);
    setColumns(columnNames);
  }

  public void setColumns(String[] columnNames) {
    this.columnNames = columnNames;
  }

  @Override
  public TabularIterator createNkPkIterator(
      Connection connection, String dbId, KeyMapper mapper, Database database) {
    if (ArrayUtil.isEmpty(columnNames)) {
      throw ExceptionFactory.getInstance().configurationError("No unique key columns defined");
    }
    StringBuilder builder = new StringBuilder("select ");
    builder.append(columnNames[0]);
    for (int i = 1; i < columnNames.length; i++) {
      builder.append(", ").append(columnNames[i]);
    }
    String[] pkColumnNames = database.getTable(tableName).getPKColumnNames();
    for (String columnName : pkColumnNames) {
      builder.append(", ").append(columnName);
    }

    builder.append(" from ").append(tableName);
    String query = builder.toString();
    TabularIterator rawIterator = query(query, connection);
    ColumnToNkConverter converter = new ColumnToNkConverter(dbId, mapper);
    return new UniqueKeyNkPkIterator(rawIterator, converter, pkColumnNames);
  }

  @Override
  public String getDescription() {
    return "Identity definition by unique key: " + ArrayFormat.format(columnNames);
  }

  public class UniqueKeyNkPkIterator extends ConvertingIterator<Object[], Object[]> implements TabularIterator {

    final String[] pkColumnNames;

    public UniqueKeyNkPkIterator(TabularIterator rawIterator, ColumnToNkConverter converter, String[] pkColumnNames) {
      super(rawIterator, converter);
      this.pkColumnNames = columnNames;
    }

    @Override
    public String[] getColumnNames() {
      String[] labels = new String[1 + pkColumnNames.length];
      labels[0] = "NK";
      System.arraycopy(pkColumnNames, 0, labels, 1, labels.length - 1);
      return labels;
    }

  }

  /**
   * The type Column to nk converter.
   */
  public class ColumnToNkConverter extends ThreadSafeConverter<Object[], Object[]> {

    /**
     * The Db id.
     */
    final String dbId;
    /**
     * The Mapper.
     */
    final KeyMapper mapper;

    /**
     * Instantiates a new Column to nk converter.
     *
     * @param dbId   the db id
     * @param mapper the mapper
     */
    public ColumnToNkConverter(String dbId, KeyMapper mapper) {
      super(Object[].class, Object[].class);
      this.dbId = dbId;
      this.mapper = mapper;
    }

    @Override
    public Object[] convert(Object[] raw) {
      NKBuilder nkBuilder = new NKBuilder();
      for (int i = 0; i < columnNames.length; i++) {
        Object value = raw[i];
        nkBuilder.addComponent(value);
      }
      ArrayBuilder<Object> arrayBuilder = new ArrayBuilder<>(Object.class);
      arrayBuilder.add(nkBuilder.toString());
      for (int i = columnNames.length; i < raw.length; i++) {
        arrayBuilder.add(raw[i]);
      }
      return arrayBuilder.toArray();
    }

  }

}
