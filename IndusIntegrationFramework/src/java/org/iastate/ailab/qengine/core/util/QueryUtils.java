package org.iastate.ailab.qengine.core.util;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy.JoinColumn;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;

import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZqlParser;

public class QueryUtils {

   private static final Logger logger = Logger.getLogger(QueryUtils.class);

   private QueryUtils() {
      //cannot instantiate
   }

   public static boolean participatesInQuery(DataSourceDescriptor desc,
         ZQuery query) {
      //if any of columns in Zquery are present in DataSourceDescriptor return true

      Vector<String> queryColumns = getQueryColumns(query);
      Vector<String> dataSourceColumns = desc.getAllColumnNames();
      return IsIntersection(queryColumns, dataSourceColumns);
   }

   public static Vector<String> getQueryColumns(ZQuery query) {
      Vector<String> result = new Vector<String>();
      result.addAll(getSelectColumns(query));

      Vector<String> whereColumns = getWhereColumns(query);
      String currWhereColumn = null;

      //Add only those where columns which have not been added before
      for (int i = 0; i < whereColumns.size(); i++) {
         currWhereColumn = whereColumns.get(i);
         if (!result.contains(currWhereColumn)) {
            result.add(currWhereColumn);
         }
      }
      result.trimToSize();
      return result;
   }

   public static Vector<String> getSelectColumns(ZQuery query) {
      Vector<String> result = new Vector<String>();
      Vector<ZSelectItem> select = query.getSelect();
      for (int i = 0; i < select.size(); i++) {
         ZSelectItem sel = select.get(i);
         String col = sel.getColumn();
         // if it's an expression than get the column from within the expression
         if (sel.isExpression()) {
            if (sel.getExpression() instanceof ZExpression) {
               ZExpression zExp = (ZExpression) sel.getExpression();
               col = zExp.getOperand(0).toString();
            }
         }
         // only add it once
         if (!result.contains(col)) {
            result.add(col);
         }
      }
      return result;
   }

   public static Vector<String> getWhereColumns(ZQuery query) {
      Vector<String> result = new Vector<String>();
      ZExpression where = (ZExpression) query.getWhere();
      result = addZExpressionColumns(where, result);
      return result;
   }

   private static Vector<String> addZQueryColumns(ZQuery query,
         Vector<String> result) {
      result.addAll(getQueryColumns(query));
      return result;
   }

   private static Vector<String> addZExpressionColumns(ZExpression expression,
         Vector<String> result) {

      //if passed expression is null, nothing to add to the passed columns   
      if (expression == null)
         return result;

      Vector<ZExp> operands = expression.getOperands();
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         Vector<String> temp = addOperandColumns(currOperand, result);
         result = temp;
         // result.addAll(temp);

//			   if(currOperand instanceof ZConstant) {
//				   ZConstant curr = (ZConstant)currOperand;
//				   if(curr.getType() ==ZConstant.COLUMNNAME) {
//					   result.add(curr.getValue());
//				   }
//				   
//			   } else if (currOperand instanceof ZExpression) {
//				   Vector<String> temp =  addZExpressionColumns((ZExpression)currOperand,result);
//				   result.addAll(temp);
//			   } else if (currOperand instanceof ZQuery) {
//				   Vector<String> temp =  addZQueryColumns((ZQuery)currOperand,result);
//				   result.addAll(temp);
//			   }
      }

      return result;
   }

   private static Vector<String> addOperandColumns(ZExp currOperand,
         Vector<String> result) {
      if (currOperand instanceof ZConstant) {
         ZConstant curr = (ZConstant) currOperand;
         if (curr.getType() == ZConstant.COLUMNNAME) {

            if (!result.contains(curr.getValue())) {
               result.add(curr.getValue()); //Add if not only previously included
            }
         }
      } else if (currOperand instanceof ZExpression) {
         Vector<String> temp = addZExpressionColumns((ZExpression) currOperand,
               result);
         result = temp;
         //result.addAll(temp);
      } else if (currOperand instanceof ZQuery) {
         Vector<String> temp = addZQueryColumns((ZQuery) currOperand, result);
         result = temp;
         // result.addAll(temp); //verifyy that you need .addAll
      }
      return result;
   }

   public static Vector<String> getOperandColumns(ZExp operand) {
      Vector<String> res = new Vector<String>();
      return addOperandColumns(operand, res);
   }

   private static Vector<String> addZExpressionOperators(
         ZExpression expression, Vector<String> result) {
      String currOperator = expression.getOperator();
      result.add(currOperator);
      Vector<ZExp> operands = expression.getOperands();
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);
         if (currOperand instanceof ZConstant) {
            continue;
         } else if (currOperand instanceof ZExpression) {
            Vector<String> temp = addZExpressionOperators(
                  (ZExpression) currOperand, result);
            result = temp;
            //result.addAll(temp);
         } else if (currOperand instanceof ZQuery) {
            ZQuery query = (ZQuery) currOperand;
            //e.g select a where b in (select b where d>5);
            Vector<String> temp = addZExpressionOperators((ZExpression) query
                  .getWhere(), result);
            result.addAll(temp);
         }
      }

      return result;
   }

   /**
    * Return the operators in where clause of the query
    * 
    * @param query
    * @return
    */
   public static Vector<String> getQueryOperators(ZQuery query) {
      Vector<String> res = new Vector<String>();
      ZExpression where = (ZExpression) query.getWhere();
      return addZExpressionOperators(where, res);
   }

   public static String getFirstOperator(ZQuery query) {

      ZExpression where = (ZExpression) query.getWhere();
      if (where == null) {
         return null;
      } else {
         return where.getOperator();
      }
   }

   /**
    * returns true if the first operator in the query is the passed
    * operator
    * 
    * @param query
    * @return
    */
   public static boolean isThisFirstOperator(ZQuery query, String operator) {
      boolean result = false;
      String val = getFirstOperator(query);
      if (val != null && val.equalsIgnoreCase(operator)) {
         result = true;
      }
      return result;
   }

   /**
    * Finds where all the operands in a where clause are ANDED This
    * actually makes sure there is at least one AND clause and no OR clause
    * For IN clauses the internal clauses are taken into consideration Used
    * in Optimization
    * 
    * @param query
    * @return
    */
   public static boolean areAllOperandsAND(ZQuery query) {
      boolean result = false;
      Vector<String> res = QueryUtils.getQueryOperators(query);
      //check if there is atleast one AND and no OR
      if (res != null && (res.contains("AND") || res.contains("AND"))
            && !(res.contains("OR") || res.contains("OR"))) {
         result = true;
      }
      return result;
   }

   private static boolean IsIntersection(Vector<String> src, Vector<String> dest) {
      boolean intersect = false;
      boolean found = false;
      String srcName = null;
      String destName = null;
      for (int i = 0; i < src.size(); i++) {
         srcName = src.get(i);
         for (int j = 0; j < dest.size(); j++) {
            destName = dest.get(j);
            if (srcName.equals(destName)) {
               found = true;
               intersect = true;
               break;
            }
         }

         if (found)
            break; //from outer loop
      }

      return intersect;
   }

   /**
    * Checks if the Select clause of the query contains the joinColumn
    * Returns true if the select clause contains a wild card
    * 
    * @param query
    * @param joinColumn
    * @return
    */
   public static boolean containsJoinColumn(ZQuery query, JoinColumn joinColumn) {
      boolean contains = false;
      ZSelectItem sItem = new ZSelectItem(joinColumn.columnName);
      Vector<ZSelectItem> select = query.getSelect();
      if (isPresent(select, sItem)) {
         contains = true;
      }
      return contains;
   }

   /**
    * Adds a column to select part of the query if it is not already there.
    * 
    * @param query
    * @param addColumn
    */
   public static ZQuery addJoinColumn(ZQuery query, JoinColumn joinColumn) {

      ZQuery res = new ZQuery();
      ZSelectItem sItem = new ZSelectItem(joinColumn.columnName);
      ZFromItem fItem = new ZFromItem(joinColumn.tableName);
      Vector<ZSelectItem> select = query.getSelect();

//		if (!select.contains(sItem)) {
//         select.add(sItem);
//      }
      if (!isPresent(select, sItem)) {
         select.add(sItem);
      }
      Vector<ZFromItem> from = query.getFrom();

//		if (!from.contains(fItem)) {
//         from.add(fItem);
//      }

      if (!isPresent(from, fItem)) {
         from.add(fItem);
      }
      res.addSelect(select);
      res.addFrom(from);
      res.addWhere(query.getWhere());

      return res;
   }

   private static boolean isPresent(Vector<ZSelectItem> selectItems,
         ZSelectItem thisSelect) {
      boolean res = false;
      ZSelectItem currSelect;

      for (int i = 0; i < selectItems.size(); i++) {
         currSelect = selectItems.get(i);
         if (currSelect.getColumn().equals("*")
               || thisSelect.getColumn().equals(currSelect.getColumn())) {
            res = true;
            break;
            //also handles cases when select items is *. It by default means that everything is present
         }
      }
      return res;
   }

   private static boolean isPresent(Vector<ZFromItem> fromItems,
         ZFromItem thisFrom) {
      boolean res = false;
      ZFromItem currFrom;

      for (int i = 0; i < fromItems.size(); i++) {
         currFrom = fromItems.get(i);
         if (thisFrom.getTable().equalsIgnoreCase(currFrom.getTable())) {
            res = true;
            break;
         }
      }
      return res;
   }

   /**
    * Given columns and corresponding tables, generate a query to get the
    * selected columns
    * 
    * @param selectColumns
    * @param tables
    * @return
    */
   public static ZQuery generateSelectQuery(String[] selectColumns,
         String[] tables) {
      //TODO Throw an exception if passed parameters are null

      ZQuery query = new ZQuery();

      Vector<ZSelectItem> select = new Vector<ZSelectItem>();
      Vector<ZFromItem> from = new Vector<ZFromItem>();
      ZSelectItem sItem;
      ZFromItem fItem;

      for (int i = 0; i < selectColumns.length; i++) {
         sItem = new ZSelectItem(selectColumns[i]);
         select.add(sItem);
      }

      for (int i = 0; i < tables.length; i++) {
         fItem = new ZFromItem(tables[i]);
         from.add(fItem);
      }

      query.addSelect(select);
      query.addFrom(from);
      return query;
   }

   /**
    * A util function that creates the select part of the countQuery for
    * the given columName
    * 
    * @param columnName
    * @return ZSelectItem representing select COUNT(columnName)
    */
   public static ZSelectItem createCountQuerySelectPart(String columnName) {

      ZExpression sCountItem = null;

      ZSelectItem sItem = new ZSelectItem(columnName);
      sItem.setAggregate("COUNT");
      sCountItem = new ZExpression("COUNT");

      //ZExpression corresponding to selectItem
      ZConstant val = new ZConstant(columnName, ZConstant.COLUMNNAME);
      sCountItem.addOperand(val);

      sItem.setExpression(sCountItem);
      return sItem;
   }

   /**
    * It creates a new query with select clause corresponding to joinColumn
    * and where clause remaining unchanged
    * 
    * @param query
    * @param newSelectColumns
    * @param desc
    * @return
    */
   public static ZQuery replaceSelectColumnsByJoinColumn(ZQuery query,
         JoinColumn joinColumn, DataSourceDescriptor desc) {

      return replaceSelectColumnsByJoinColumn(query, joinColumn, desc, false);
   }

   /**
    * It creates a new count query with select clause corresponding to
    * joinColumn and where clause remaining unchanged
    * 
    * @param query
    * @param joinColumn
    * @param desc
    * @return
    */
   public static ZQuery replaceCountSelectColumnsByJoinColumn(ZQuery query,
         JoinColumn joinColumn, DataSourceDescriptor desc) {

      return replaceSelectColumnsByJoinColumn(query, joinColumn, desc, true);
   }

   /**
    * It creates a new query with select clause corresponding to joinColumn
    * and where clause remaining unchanged
    * 
    * @param query
    * @param newSelectColumns
    * @param desc
    * @param countQuery If true the select column is to be count query
    * corresponding to join Column
    * @return
    */
   private static ZQuery replaceSelectColumnsByJoinColumn(ZQuery query,
         JoinColumn joinColumn, DataSourceDescriptor desc, boolean countQuery) {
      ZQuery res = new ZQuery();
      ZSelectItem sItem = null;
      if (!countQuery) {
         sItem = new ZSelectItem(joinColumn.columnName);
      } else {
         sItem = createCountQuerySelectPart(joinColumn.columnName);
      }
      ZFromItem fItem = new ZFromItem(joinColumn.tableName);

      //Vector<ZSelectItem> select = new Vector<ZSelectItem>();
      Vector<ZSelectItem> select = new Vector<ZSelectItem>();
      select.add(sItem);
      Vector<ZFromItem> queryFrom = query.getFrom();

      if (!isPresent(queryFrom, fItem)) {
         queryFrom.add(fItem);
      }

      /*
       * TODO Some of the tables in From may correspond to select columns
       * in the passed query and may not be required. But There is no harm
       * in keeping them. The database should optimize knowing they are not
       * part of query. However, it will be good to clean it up.
       */

      res.addSelect(select); //select corresponds to join Column
      res.addFrom(queryFrom); //may have added the table corresponding to joinColumn
      res.addWhere(query.getWhere()); //keep where clause changed
      return res;
   }

   /**
    * The template is of following The select clause corresponds to select
    * columns in the passed query The where clause corresponds to where
    * joinColumn in ('$placeHolderPreviousQueryResponse$'); Once this
    * template is replace it is the responsibility of the calling program
    * to replace the $placeHolderPreviousQueryResponse$ by appropriate
    * value
    * 
    * @param query
    * @param joinColumn
    * @return
    */
   public static ZQuery simpleResponseQueryTemplate(ZQuery query,
         JoinColumn joinColumn) {
      ZQuery res = new ZQuery();
      Vector<ZFromItem> queryFrom = query.getFrom();

      ZFromItem fItem = new ZFromItem(joinColumn.tableName);
      if (!isPresent(queryFrom, fItem)) {
         queryFrom.add(fItem);
      }

      String quoteChar = "'";
      if (joinColumn.columnType.equalsIgnoreCase("int")) {
         quoteChar = ""; //no need to quote for integer and
      }

      //fancy way to get where clause for template
      //hard code the appropriate query and get where clause from it;
      String test = "SELECT " + joinColumn.tableName + "."
            + joinColumn.columnName + " FROM " + joinColumn.tableName
            + " WHERE " + joinColumn.tableName + "." + joinColumn.columnName
            + "=" + quoteChar + "keyValues" + quoteChar + ";";

      logger.trace("templateQuery=" + test);

      ZQuery testQuery = null;
      try {
         ZqlParser parser = new ZqlParser();
         ByteArrayInputStream inpStream = new ByteArrayInputStream(test
               .getBytes());
         parser.initParser(inpStream);
         testQuery = (ZQuery) parser.readStatements().get(0);
      } catch (ParseException e) {
         //should not happen since we hand coded the query
         logger.error(
               "ParseException while generating simpleResponseQueryTemplate (parsing query): "
                     + testQuery, e);
      }

      res.addSelect(query.getSelect());
      res.addFrom(queryFrom);
      res.addWhere(testQuery.getWhere());

      return res;
   }

   public static ZQuery getZQueryFromString(String query) {
      if (!query.endsWith(";"))
         query += ";";

      ZqlParser parser = new ZqlParser();
      ByteArrayInputStream in = new ByteArrayInputStream(query.getBytes());
      parser.initParser(in);
      ZQuery q = null;
      try {
         Vector<ZQuery> statements = parser.readStatements();
         q = statements.get(0);
      } catch (ParseException e) {
         logger.warn(
               "ParseException converting string query to Zquery. Check Syntax: "
                     + query, e);
         //TODO should an exception be thrown here instead of continuing?
      }

      return q;
   }

   /**
    * Provides an instance of query which is a clone of the passed query
    * 
    * @param query
    * @return
    */
   public static ZQuery getZQueryClone(ZQuery query) {
      ZQuery out = new ZQuery();
      out.addSelect(query.getSelect());
      out.addFrom(query.getFrom());
      out.addWhere(query.getWhere());
      out.addGroupBy(query.getGroupBy());
      out.addOrderBy(query.getOrderBy());
      return out;
   }

   public static boolean isCountQuery(ZQuery query) {
      boolean retVal = false;

      Vector<?> zSelectItems = query.getSelect();

      ZSelectItem currSelect;
      ZExpression currSelectExp; //for aggregate queries the select may be a ZExpression based on how it is constructed
      for (int i = 0; i < zSelectItems.size(); i++) {
         //currSelect = zSelectItems.get(i);
         if (zSelectItems.get(i) instanceof ZExpression) {
            currSelectExp = (ZExpression) zSelectItems.get(i);
            //ZExpression zExp = (ZExpression) currSelect.getExpression();
            // check if it's a count expression
            if (currSelectExp.getOperator().equalsIgnoreCase("Count")) {
               retVal = true;
               break;
            }
         } else if (zSelectItems.get(i) instanceof ZSelectItem) {
            currSelect = (ZSelectItem) zSelectItems.get(i);
            String aggr = currSelect.getAggregate();
            if (aggr != null && aggr.equalsIgnoreCase("Count")) {
               retVal = true;
               break;
            }
         }
      }

      return retVal;
   }

   /**
    * Checks if the passed string is in one of { >,< = }. These can be
    * overloaded to represent AVH role in INDUS SQL
    * 
    * @param inp
    * @return
    */
   public static boolean isPossibleAVHRole(String inp) {
      boolean result = false;
      if (inp.equals("<") || inp.equals(">") || inp.equals("=")) {
         result = true;
      }
      return result;
   }
}
