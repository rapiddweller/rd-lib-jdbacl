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

package com.rapiddweller.jdbacl.sql;

import com.rapiddweller.common.ArrayBuilder;
import com.rapiddweller.common.exception.ParseException;
import com.rapiddweller.common.StringUtil;
import com.rapiddweller.common.exception.SyntaxError;
import com.rapiddweller.jdbacl.DatabaseDialect;
import com.rapiddweller.jdbacl.model.DBColumn;
import com.rapiddweller.jdbacl.model.DBDataType;
import com.rapiddweller.jdbacl.model.DBPrimaryKeyConstraint;
import com.rapiddweller.jdbacl.model.DBTable;
import com.rapiddweller.jdbacl.sql.parser.SQLLexer;
import com.rapiddweller.jdbacl.sql.parser.SQLParser;
import com.rapiddweller.jdbacl.sql.parser.TextHolder;
import com.rapiddweller.script.Expression;
import com.rapiddweller.script.expression.ConcatExpression;
import com.rapiddweller.script.expression.ConditionalAndExpression;
import com.rapiddweller.script.expression.ConditionalOrExpression;
import com.rapiddweller.script.expression.DivisionExpression;
import com.rapiddweller.script.expression.EqualsExpression;
import com.rapiddweller.script.expression.ExclusiveOrExpression;
import com.rapiddweller.script.expression.ExpressionUtil;
import com.rapiddweller.script.expression.GreaterExpression;
import com.rapiddweller.script.expression.GreaterOrEqualsExpression;
import com.rapiddweller.script.expression.LessExpression;
import com.rapiddweller.script.expression.LessOrEqualsExpression;
import com.rapiddweller.script.expression.LogicalComplementExpression;
import com.rapiddweller.script.expression.ModuloExpression;
import com.rapiddweller.script.expression.MultiplicationExpression;
import com.rapiddweller.script.expression.NotEqualsExpression;
import com.rapiddweller.script.expression.NullExpression;
import com.rapiddweller.script.expression.SubtractionExpression;
import com.rapiddweller.script.expression.SumExpression;
import com.rapiddweller.script.expression.UnaryMinusExpression;
import com.rapiddweller.script.expression.ValueCollectionContainsExpression;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.ParserRuleReturnScope;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Provides SQL parsing functionality.<br/><br/>
 * Created: 05.08.2010 10:19:38
 * @author Volker Bergmann
 * @since 0.1
 */
public class SQLParserUtil {

  static final Logger LOGGER = LoggerFactory.getLogger(SQLParserUtil.class);

  public static Object parse(CharStream in, DatabaseDialect dialect) throws ParseException {
    String text = null;
    if (in instanceof TextHolder) {
      text = ((TextHolder) in).getText();
    }
    try {
      SQLParser parser = parser(in);
      SQLParser.commands_return r = parser.commands();
      checkForSyntaxErrors(text, "weightedLiteralList", parser, r);
      if (r != null) {
        return convertNode((CommonTree) r.getTree(), dialect);
      } else {
        return null;
      }
    } catch (RuntimeException e) {
      if (e.getCause() instanceof RecognitionException) {
        throw mapToParseException((RecognitionException) e.getCause(), text);
      } else {
        throw e;
      }
    } catch (RecognitionException e) {
      throw mapToParseException(e, text);
    }
  }

  public static Expression<?> parseExpression(CharStream in) throws ParseException {
    String text = null;
    if (in instanceof TextHolder) {
      text = ((TextHolder) in).getText();
    }
    try {
      SQLParser parser = parser(in);
      SQLParser.expression_return r = parser.expression();
      checkForSyntaxErrors(text, "expression", parser, r);
      if (r != null) {
        return convertExpressionNode((CommonTree) r.getTree());
      } else {
        return null;
      }
    } catch (RuntimeException e) {
      if (e.getCause() instanceof RecognitionException) {
        throw mapToParseException((RecognitionException) e.getCause(), text);
      } else {
        throw e;
      }
    } catch (RecognitionException e) {
      throw mapToParseException(e, text);
    }
  }

  private static Object convertNode(CommonTree node, DatabaseDialect dialect) {
    switch (node.getType()) {
      case SQLLexer.CREATE_TABLE:
        return convertCreateTable(node, dialect);
      case SQLLexer.DROP_TABLE:
        return convertDropTable(node);
      case SQLLexer.ALTER_TABLE:
        return convertAlterTable(node);
      case SQLLexer.CREATE_SEQUENCE:
        return convertCreateSequence(node);
      case SQLLexer.DROP_SEQUENCE:
        return convertDropSequence(node);
      case SQLLexer.CREATE_INDEX:
        return convertCreateIndex(node);
      case SQLLexer.COMMENT_TABLE:
        return convertTableComment(node);
      case SQLLexer.COMMENT_COLUMN:
        return convertColumnComment(node);
    }
    if (node.isNil()) {
      List<Object> nodes = convertNodes(getChildNodes(node), dialect);
      return nodes.toArray();
    }
    throw new ParseException("Unknown token type", "'" + node.getText() + "'");
  }

  @SuppressWarnings("rawtypes")
  private static Expression convertExpressionNode(CommonTree node) {
    switch (node.getType()) {
      case SQLLexer.OR:
        return convertOr(node);
      case SQLLexer.AND:
        return convertAnd(node);
      case SQLLexer.XOR:
        return convertXor(node);
      case SQLLexer.EQ:
        return convertEq(node);
      case SQLLexer.BANGEQ:
        return convertBangEq(node);
      case SQLLexer.LTGT:
        return convertBangEq(node); // <>
      case SQLLexer.GT:
        return convertGt(node);
      case SQLLexer.GE:
        return convertGe(node);
      case SQLLexer.LT:
        return convertLt(node);
      case SQLLexer.LE:
        return convertLe(node);
      case SQLLexer.IS:
        return convertIs(node);
      case SQLLexer.NOT:
        return convertNot(node);
      case SQLLexer.NULL:
        return convertNull(node);
      case SQLLexer.LIKE:
        return convertLike(node);
      case SQLLexer.IN:
        return convertIn(node);
      case SQLLexer.BETWEEN:
        return convertBetween(node);
      case SQLLexer.PLUS:
        return convertPlus(node);
      case SQLLexer.SUB:
        return convertSub(node);
      case SQLLexer.STAR:
        return convertStar(node);
      case SQLLexer.SLASH:
        return convertSlash(node);
      case SQLLexer.PERCENT:
        return convertPercent(node);
      case SQLLexer.BARBAR:
        return convertBarBar(node);
      case SQLLexer.INVOCATION:
        return convertInvocation(node);
      case SQLLexer.QUOTED_NAME:
        return convertQuotedName(node);
      case SQLLexer.IDENTIFIER:
        return convertIdentifier(node);
      case SQLLexer.STRING:
        return convertStringToExpression(node);
      case SQLLexer.INT:
        return convertInt(node);
      default:
        throw new ParseException("Unknown token type (" + node.getType() + ")", "'" + node.getText() + "'");
    }
  }

  @SuppressWarnings("unchecked")
  private static Expression<Boolean> convertOr(CommonTree node) {
    ConditionalOrExpression result = new ConditionalOrExpression("OR");
    for (CommonTree childNode : getChildNodes(node)) {
      result.addTerm(convertExpressionNode(childNode));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Expression<Boolean> convertAnd(CommonTree node) {
    ConditionalAndExpression result = new ConditionalAndExpression("AND");
    for (CommonTree childNode : getChildNodes(node)) {
      result.addTerm(convertExpressionNode(childNode));
    }
    return result;
  }

  private static Expression<Boolean> convertXor(CommonTree node) {
    return new ExclusiveOrExpression(
        convertExpressionNode(childAt(0, node)),
        convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertEq(CommonTree node) {
    return new EqualsExpression("=",
        convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertBangEq(CommonTree node) {
    return new NotEqualsExpression(convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertGt(CommonTree node) {
    return new GreaterExpression(convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertGe(CommonTree node) {
    return new GreaterOrEqualsExpression(convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertLt(CommonTree node) {
    return new LessExpression(convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertLe(CommonTree node) {
    return new LessOrEqualsExpression(convertExpressionNode(childAt(0, node)), convertExpressionNode(childAt(1, node)));
  }

  private static Expression<Boolean> convertIs(CommonTree node) {
    if (node.getChildCount() > 1) {
      return new NotEqualsExpression("IS NOT", convertExpressionNode(childAt(0, node)), new NullExpression());
    } else {
      return new EqualsExpression("IS", convertExpressionNode(childAt(0, node)), new NullExpression());
    }
  }

  private static Expression<?> convertNot(CommonTree node) {
    return new LogicalComplementExpression("NOT ", convertExpressionNode(childAt(0, node)));
  }

  private static Expression<?> convertNull(CommonTree node) {
    return new NullExpression();
  }

  private static Expression<?> convertLike(CommonTree node) {
    Expression<?> valueEx = convertExpressionNode(childAt(0, node));
    CommonTree child1 = childAt(1, node);
    boolean not = (child1.getType() == SQLLexer.NOT);
    int collectionIndex = (not ? 2 : 1);
    Expression<?> refEx = convertExpressionNode(childAt(collectionIndex, node));
    Expression<?> result = new LikeExpression(valueEx, refEx);
    if (not) {
      result = new LogicalComplementExpression(result);
    }
    return result;
  }

  private static Expression<?> convertIn(CommonTree node) {
    Expression<?> valueEx = convertExpressionNode(childAt(0, node));
    CommonTree child1 = childAt(1, node);
    boolean not = (child1.getType() == SQLLexer.NOT);
    int collectionIndex = (not ? 2 : 1);
    Expression<? extends Collection<?>> collEx = convertValueList(childAt(collectionIndex, node));
    Expression<?> result = new ValueCollectionContainsExpression("IN", valueEx, collEx);
    if (not) {
      result = new LogicalComplementExpression(result);
    }
    return result;
  }

  private static Expression<? extends Collection<?>> convertValueList(CommonTree node) {
    List<Expression<?>> result = new ArrayList<>();
    for (CommonTree child : getChildNodes(node)) {
      result.add(convertExpressionNode(child));
    }
    return ExpressionUtil.constant(result);
  }

  private static Expression<?> convertBetween(CommonTree node) {
    return new BetweenExpression(convertExpressionNode(childAt(0, node)),
        convertExpressionNode(childAt(1, node)),
        convertExpressionNode(childAt(2, node)));
  }

  @SuppressWarnings("unchecked")
  private static Expression<?> convertPlus(CommonTree node) {
    SumExpression result = new SumExpression();
    for (CommonTree child : getChildNodes(node)) {
      result.addTerm(convertExpressionNode(child));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Expression<?> convertSub(CommonTree node) {
    if (node.getChildCount() == 1) {
      return new UnaryMinusExpression<>(convertExpressionNode(childAt(0, node)));
    } else {
      SubtractionExpression result = new SubtractionExpression();
      for (CommonTree child : getChildNodes(node)) {
        result.addTerm(convertExpressionNode(child));
      }
      return result;
    }
  }

  @SuppressWarnings("unchecked")
  private static Expression<?> convertStar(CommonTree node) {
    MultiplicationExpression result = new MultiplicationExpression();
    for (CommonTree child : getChildNodes(node)) {
      result.addTerm(convertExpressionNode(child));
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private static Expression<?> convertSlash(CommonTree node) {
    DivisionExpression result = new DivisionExpression();
    for (CommonTree child : getChildNodes(node)) {
      result.addTerm(convertExpressionNode(child));
    }
    return result;
  }

  private static Expression<?> convertPercent(CommonTree node) {
    return new ModuloExpression(
        convertExpressionNode(childAt(0, node)),
        convertExpressionNode(childAt(1, node)));
  }

  private static Expression<String> convertBarBar(CommonTree node) {
    return new ConcatExpression("||",
        convertExpressionNode(childAt(0, node)),
        convertExpressionNode(childAt(1, node)));
  }

  private static Expression<?> convertInvocation(CommonTree node) {
    String functionName = convertIdentifier(childAt(0, node)).evaluate(null);
    Expression<?>[] arguments = convertArguments(childAt(1, node));
    return new FunctionInvocation(functionName, arguments);
  }

  @SuppressWarnings("rawtypes")
  private static Expression<?>[] convertArguments(CommonTree node) {
    ArrayBuilder<Expression> result = new ArrayBuilder<>(Expression.class);
    for (CommonTree child : getChildNodes(node)) {
      result.add(convertExpressionNode(child));
    }
    return result.toArray();
  }

  private static Expression<String> convertIdentifier(CommonTree node) {
    return new ColumnExpression(node.getText(), false);
  }

  private static Expression<String> convertQuotedName(CommonTree node) {
    String quotedColName = node.getText().trim();
    String colName = quotedColName.substring(1, quotedColName.length() - 1);
    return new ColumnExpression(colName, true);
  }

  private static Expression<?> convertStringToExpression(CommonTree node) {
    return ExpressionUtil.constant(node.getText());
  }

  private static Expression<?> convertInt(CommonTree node) {
    return ExpressionUtil.constant(new BigInteger(node.getText()));
  }

  private static Object convertTableComment(CommonTree node) {
    // TODO Process 'comment on table'
    return null;
  }

  private static Object convertColumnComment(CommonTree node) {
    // TODO Process 'comment on column'
    return null;
  }

  private static Object convertAlterTable(CommonTree node) {
    // TODO Process 'alter table'
    return null;
  }

  private static Object convertDropTable(CommonTree node) {
    // TODO Process 'drop table'
    return null;
  }

  private static Object convertCreateSequence(CommonTree node) {
    // TODO Process 'create sequence'
    return null;
  }

  private static Object convertCreateIndex(CommonTree node) {
    // TODO Process 'create index'
    return null;
  }

  private static Object convertDropSequence(CommonTree node) {
    // TODO Process 'drop sequence'
    return null;
  }

  private static List<Object> convertNodes(List<CommonTree> nodes, DatabaseDialect dialect) {
    List<Object> result = new ArrayList<>();
    for (CommonTree node : nodes) {
      result.add(convertNode(node, dialect));
    }
    return result;
  }

  private static DBTable convertCreateTable(CommonTree node, DatabaseDialect dialect) {
    String tableName = convertString(childAt(0, node));
    DBTable table = new DBTable(tableName);
    convertTableDetails(childAt(1, node), table, dialect);
    // TODO parse ora_configs
    return table;
  }

  private static void convertTableDetails(CommonTree node, DBTable table, DatabaseDialect dialect) {
    for (CommonTree subNode : getChildNodes(node)) {
      convertTableDetail(subNode, table, dialect);
    }
  }

  private static void convertTableDetail(CommonTree node, DBTable table, DatabaseDialect dialect) {
    switch (node.getType()) {
      case SQLLexer.COLUMN_SPEC:
        convertColumnSpec(node, table);
        break;
      case SQLLexer.PRIMARY:
        convertInlinePK(node, table, dialect);
        break;
      default:
        throw new ParseException("Unknown table detail token type",
            String.valueOf(node.getText()),
            node.getLine(),
            node.getCharPositionInLine());
    }
  }

  private static void convertInlinePK(CommonTree node, DBTable table, DatabaseDialect dialect) {
    String constraintName = convertString(childAt(0, node));
    String[] pkColumnNames = convertNameList(childAt(1, node));
    DBPrimaryKeyConstraint pk = new DBPrimaryKeyConstraint(
        table, constraintName, dialect.isDeterministicPKName(constraintName), pkColumnNames);
    table.setPrimaryKey(pk);
  }

  private static String[] convertNameList(CommonTree node) {
    String[] result = new String[node.getChildCount()];
    for (int i = 0; i < result.length; i++) {
      result[i] = convertString(childAt(i, node));
    }
    return result;
  }

  private static void convertColumnSpec(CommonTree node, DBTable table) {
    String columnName = convertString(childAt(0, node));
    String columnTypeName;
    Integer size = null;
    Integer fractionDigits = null;
    int detailOffset = 2;
    columnTypeName = convertString(childAt(1, node));
    if (node.getChildCount() > 2 && childAt(2, node).getType() == SQLLexer.SIZE) {
      detailOffset++;
      CommonTree sizeNode = childAt(2, node);
      size = convertInteger(childAt(0, sizeNode));
      if (sizeNode.getChildCount() > 1) {
        CommonTree subNode2 = childAt(1, sizeNode);
        if (subNode2.getType() == SQLLexer.INT) {
          fractionDigits = convertInteger(subNode2);
        } else {
          // TODO support (n BYTE) / (n CHAR)
        }
      }
    }
    DBColumn column = new DBColumn(columnName, table, DBDataType.getInstance(columnTypeName), size, fractionDigits);
    table.addColumn(column);
    for (int i = detailOffset; i < node.getChildCount(); i++) {
      convertColumnDetail(childAt(i, node), column);
    }
  }

  private static Integer convertInteger(CommonTree node) {
    return Integer.parseInt(node.getText());
  }

  private static void convertColumnDetail(CommonTree node, DBColumn column) {
    switch (node.getType()) {
      case SQLLexer.NOT:
        column.setNullable(false);
        break;
      case SQLLexer.DEFAULT:
        column.setNullable(false);
        break;
      default:
        throw new ParseException("Unknown column detail token type",
            String.valueOf(node.getText()),
            node.getLine(),
            node.getCharPositionInLine());
    }
  }

  private static String convertString(CommonTree node) {
    return node.getText();
  }

  private static SQLParser parser(CharStream in) {
    SQLLexer lex = new SQLLexer(in);
    CommonTokenStream tokens = new CommonTokenStream(lex);
    return new SQLParser(tokens);
  }

  private static ParseException mapToParseException(RecognitionException cause, String text) {
    return new ParseException("Error parsing SQL", cause,
        text, cause.line, cause.charPositionInLine);
  }

  @SuppressWarnings("unchecked")
  private static List<CommonTree> getChildNodes(CommonTree node) {
    return node.getChildren();
  }

  private static CommonTree childAt(int index, CommonTree node) {
    return (CommonTree) node.getChild(index);
  }

  private static void checkForSyntaxErrors(String text, String type,
                                           SQLParser parser, ParserRuleReturnScope r) {
    if (parser.getNumberOfSyntaxErrors() > 0) {
      throw new SyntaxError("Illegal " + type, text, -1, -1);
    }
    CommonToken stop = (CommonToken) r.stop;
    if (text != null && stop.getStopIndex() < StringUtil.trimRight(text).length() - 1) {
      if (stop.getStopIndex() == 0) {
        throw new SyntaxError("Syntax error after " + stop.getText(), text);
      } else {
        throw new SyntaxError("Syntax error at the beginning ", text);
      }
    }
  }

}
