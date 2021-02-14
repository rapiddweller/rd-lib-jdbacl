/*
 * (c) Copyright 2007-2010 by Volker Bergmann. All rights reserved.
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

package com.rapiddweller.jdbacl;

import com.rapiddweller.common.ArrayFormat;
import com.rapiddweller.common.ConversionException;
import com.rapiddweller.common.converter.UnsafeConverter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Converts a ResultSet's current cursor position to an array of objects or, if it is of size 1, to a single object.<br/>
 * <br/>
 * Created: 15.08.2007 18:19:25
 *
 * @param <E> the type parameter
 * @author Volker Bergmann
 */
public class ResultSetConverter<E> extends UnsafeConverter<ResultSet, E> {

  private final Class<E> targetType;
  private final boolean simplifying;

  /**
   * Instantiates a new Result set converter.
   *
   * @param targetType the target type
   */
  public ResultSetConverter(Class<E> targetType) {
    this(targetType, true);
  }

  /**
   * Instantiates a new Result set converter.
   *
   * @param targetType  the target type
   * @param simplifying the simplifying
   */
  public ResultSetConverter(Class<E> targetType, boolean simplifying) {
    super(ResultSet.class, targetType);
    this.targetType = targetType;
    this.simplifying = simplifying;
  }

  // Converter interface ---------------------------------------------------------------------------------------------

  @Override
  @SuppressWarnings("unchecked")
  public E convert(ResultSet resultSet) throws ConversionException {
    Object[] tmp = convertToArray(resultSet);
    if (targetType.isArray()) {
      return (E) tmp;
    } else {
      return (E) (!simplifying || tmp.length > 1 ? tmp : tmp[0]);
    }
  }

  // static convenience methods --------------------------------------------------------------------------------------

  /**
   * Convert object.
   *
   * @param resultSet   the result set
   * @param simplifying the simplifying
   * @return the object
   * @throws ConversionException the conversion exception
   */
  public static Object convert(ResultSet resultSet, boolean simplifying) throws ConversionException {
    Object[] tmp = convertToArray(resultSet);
    return (!simplifying || tmp.length > 1 ? tmp : tmp[0]);
  }

  // java.lang.Object overrides --------------------------------------------------------------------------------------

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  // private helpers -------------------------------------------------------------------------------------------------

  private static Object[] convertToArray(ResultSet resultSet) throws ConversionException {
    try {
      int columnCount = resultSet.getMetaData().getColumnCount();
      Object[] cells = new Object[columnCount];
      for (int i = 0; i < columnCount; i++) {
        cells[i] = resultSet.getObject(i + 1);
      }
      if (logger.isDebugEnabled()) {
        logger.debug("Converted: " + ArrayFormat.format(cells));
      }
      return cells;
    } catch (SQLException e) {
      throw new ConversionException(e);
    }
  }

  private static final Logger logger = LogManager.getLogger(ResultSetConverter.class);
}
