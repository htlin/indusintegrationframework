package org.iastate.ailab.qengine.core.aggregators;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy.JoinColumn;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataNode.DataAggregationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.LevelFragmentationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.QueryType;
import org.iastate.ailab.qengine.core.exceptions.ResponseFlowException;
import org.iastate.ailab.qengine.core.util.DatabaseUtils;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;
import Zql.ZSelectItem;

public class DefaultDataAggregatorImplementation implements DataAggregator {

   private static final Logger logger = Logger
         .getLogger(DefaultDataAggregatorImplementation.class);

   private DataAggregationType type;

   DataNode node;

   public ResultSet aggregate() throws SQLException {
      return this.aggregate(null);
   }

   public ResultSet aggregate(Statement stmt) throws SQLException {
      ResultSet rs = null;

      switch (type) {
      case DATA_ADD:
      case DATA_UNION: // these two should be the same
         if (node.getQueryType() == QueryType.DATA_QUERY) {
            ZQuery query = getUnionQuery();
            try {
               rs = DatabaseUtils.queryDataSource(node, query, stmt, true);
            } catch (SQLException e) {
               throw new ResponseFlowException("SQLException - "
                     + e.getMessage(), e);
            }
         } else if (node.getQueryType() == QueryType.COUNT_QUERY) {
            /*
             * if it's a count query then add up the count of the child
             * nodes and set this node(parent) to be the total of it's
             * children's count
             */
            int count = 0;
            for (DataNode child : node.getChildren()) {
               count += child.getCount();
            }
            node.setCount(count);
         }
         break;
      case DATA_COMMON: // vertical
         // we use a string query here because ZQuery apparently does not like join queries			
         String q = getCommonQuery();
         rs = DatabaseUtils.queryDataSource(node, q, stmt, true);
         break;
      case PASS_THROUGH:
         // nothing needs to be done for this case
         if (node.getQueryType() == QueryType.COUNT_QUERY) {
            for (DataNode child : node.getChildren()) {
               /*
                * vertical fragmentation for a count query with
                * pass_through will only require one child so set this
                * nodes count to the count of whichever child node has a
                * count
                */
               if (child.getCount() > 0) {
                  node.setCount(child.getCount());
               }
            }
         } else if (!node.isLeafNode()) {
            ZQuery query = getPassThroughQuery();

            rs = DatabaseUtils.queryDataSource(node, query, stmt, true);
         } else {
            // it is leaf node with DataQuery
            //TODO: IMP -->Reverse Inverse Translation of Data Content
         }
         break;
      case EXECUTE_LOCAL_QUERY:
         // we use a string query here because ZQuery apparently does not like join queries			
         q = getLocalQuery();
         if (node.getQueryType() == QueryType.DATA_QUERY) {
            rs = DatabaseUtils.queryDataSource(node, q, stmt, true);
         } else if (node.getQueryType() == QueryType.COUNT_QUERY) {
            rs = DatabaseUtils.queryDataSource(node, q, stmt, false);
         }
         break;
      case EXECUTE_REMOTE_QUERY_FOR_OR_CLAUSE:
      case EXECUTE_REMOTE_QUERY_FOR_AND_CLAUSE:
         // We will never get to these cases because they are taken care of
         // in the responseflow
         break;
      }

      return rs;
   }

   private ZQuery getPassThroughQuery() {
      String table = "";
      for (DataNode child : node.getChildren()) {
         // only one child should be participating so get that table and break the loop
         if (child.isParticipatingInQuery()) {
            table = child.getNodeName() + "_table";
         }
      }

      String selectCols = getSelectColumns();

      String query = "select " + selectCols + " from " + table + ";";

      return QueryUtils.getZQueryFromString(query);
   }

   /**
    * Get a query for DATA_ADD or DATA_UNION
    * 
    * @return a union query
    */
   private ZQuery getUnionQuery() {
      Vector<DataNode> children = node.getChildren();

      //TODO: verify this when the two paths have different number of columns. Is the syntax of common query correct?
      // assumes binary tree
//		String table1 = children.get(0).getNodeName() + "_table";
//      String table2 = children.get(1).getNodeName() + "_table";
//      String selectCols = getSelectColumns();
//
//      // use union all because we don't know for sure what the user is wanting
//      // if they don't want union all they can remove duplicates themselves
//      String query = "select " + selectCols + " from " + table1;
//      query += " union all ";
//      query += "select " + selectCols + " from " + table2 + ";";

      // allows for non-binary trees
      Vector<String> tables = new Vector<String>();
      for (DataNode child : children) {
         tables.add(child.getNodeName() + "_table");
      }

      String selectCols = getSelectColumns();

      String query = "";
      boolean addUnion = false;
      for (String table : tables) {
         if (addUnion) {
            query += " union all ";
         }
         query += "select " + selectCols + " from " + table;

         addUnion = true;
      }
      query += ";";

      logger.trace("Aggregation:UnionQuery=" + query);

      return QueryUtils.getZQueryFromString(query);
   }

   // ZQuery apparently doesn't understand join queries so we keep this query as a string
   /**
    * Get a query for DATA_COMMON
    * 
    * @return a join query
    */
   private String getCommonQuery() {
      Vector<DataNode> children = node.getChildren();
      JoinColumn[] joinCols = getJoinColumns();

      // assumes binary tree
//		String table1 = children.get(0).getNodeName() + "_table";
//      String table2 = children.get(1).getNodeName() + "_table";
//      String selectCols = getSelectColumns(joinCols);
//
//      String query = "select " + selectCols + " from " + table1;
//      query += " inner join " + table2;
//      query += " on " + table1 + "." + joinCols[0].columnName + "=";
//      query += table2 + "." + joinCols[1].columnName + ";";

      Vector<String> tables = new Vector<String>();
      for (DataNode child : children) {
         tables.add(child.getNodeName() + "_table");
      }

      String selectCols = getSelectColumns(joinCols, true);

      String query = "";
      boolean addJoin = false;
      for (int i = 0; i < tables.size(); i++) {
         if (!addJoin) {
            query += "select " + selectCols + " from " + tables.get(i);
         } else {
            query += " inner join " + tables.get(i) + " on " + tables.get(0)
                  + "." + joinCols[0].columnName;
            query += "=" + tables.get(i) + "." + joinCols[i].columnName;
         }

         addJoin = true;
      }
      query += ";";

      logger.trace("Aggregation:commonQuery=" + query);

      return query;
   }

   // ZQuery apparently doesn't understand join queries so we keep this query as a string
   /**
    * Get a query for EXECUTE_LOCAL_QUERY
    * 
    * @return a join query with a where clause from the local query
    */
   private String getLocalQuery() {
      Vector<DataNode> children = node.getChildren();
      JoinColumn[] joinCols = getJoinColumns();

      // assumes binary tree
//		String table1 = children.get(0).getNodeName() + "_table";
//      String table2 = children.get(1).getNodeName() + "_table";
//      String selectCols = getSelectColumns(joinCols);
//
//      String query = "select " + selectCols + " from " + table1;
//      query += " inner join " + table2;
//      query += " on " + table1 + "." + joinCols[0].columnName + "=";
//      query += table2 + "." + joinCols[1].columnName;
//      query += " where " + getWhereClause() + ";";

      Vector<String> tables = new Vector<String>();
      for (DataNode child : children) {
         tables.add(child.getNodeName() + "_table");
      }

      //IF select columns is * , based on how the join query, the join column will show up twice
      //Hence ask the columns to be expanded
      String selectCols = getSelectColumns(joinCols, true);
      //TODO: IF select columns is * , based on how the join query, the join column will show up twice
      //Then you can't insert it in the temp table since by definition it has the join columns only once
      //We need to construct a join query in which the join column shows up only once

      String query = "";
      boolean addJoin = false;
      for (int i = 0; i < tables.size(); i++) {
         if (!addJoin) {
            query += "select " + selectCols + " from " + tables.get(i);
         } else {
            query += " inner join " + tables.get(i) + " on " + tables.get(0)
                  + "." + joinCols[0].columnName;
            query += "=" + tables.get(i) + "." + joinCols[i].columnName;
         }

         addJoin = true;
      }
      query += " where " + getWhereClause(joinCols) + ";";

      logger.trace("Aggregation:localQuery=" + query);

      return query;
   }

   /**
    * Get the where clause for the local query
    * 
    * @return where clause sql string
    */
   private String getWhereClause(JoinColumn[] joinCols) {
      ZQuery nodeQuery = node.getNodeQuery(); //Shouldn't this be the rewriiten query=>save the reqritten query
      String where = nodeQuery.getWhere().toString();
      Vector<?> from = nodeQuery.getFrom();
      Vector<DataNode> children = node.getChildren();
      Vector<String> join = new Vector<String>();

      // get a Vector<String> of the join column names
      for (JoinColumn col : joinCols) {
         join.add(col.columnName);
      }

      for (int i = 0; i < from.size(); i++) {
         String table = children.get(i).getNodeName() + "_table";
         where = where.replaceAll(from.get(i).toString(), table);
      }

      //Vector<String> whereCol = QueryUtils.getWhereColumns(nodeQuery);

      where = this.qualifyColumn(where, joinCols[0].columnName);
      // Nasty workaround for ambiguous column names in where clause if join column is present in the where clause
      // TODO find a better way to remove ambiguousness from this

//		for (String col : join) {
//         if (where.contains(col)) {
//            where = where.replaceAll(col, children.get(0).getNodeName()
//                  + "_table" + "." + col);
//            break;
//         }
//      }

      return where;
   }

   /**
    * Get the select columns for a query
    * 
    * @param joinCol Columns used for a join clause to be removed from
    * select columns
    * @param expand Expand the columnNames if the it is wildcard
    * @return select columns sql string
    */
   private String getSelectColumns(JoinColumn[] joinCol, boolean expand) {
      //	get the select columns from the nodeQuery and remove the join columns
      //TODO: What if the query itself contained the JoinColumn. We have to keep track if the join Column was added specifically in the query
      Vector selectCols = node.getNodeQuery().getSelect();
      String selectStr = "";
      Vector<String> join = new Vector<String>();

      //get the first select item to check if it is wildcard. No sense in having wildcard and other columns(though valid)
      ZSelectItem first = (ZSelectItem) selectCols.get(0);

      if (first.isWildcard() && expand) {
         selectCols = node.getDataSourceDescriptor().getAllColumnNames();
      }

      if (joinCol != null) {
         // get a Vector<String> of the join column names
         for (JoinColumn col : joinCol) {
            if (!join.contains(col.columnName)) {
               join.add(col.columnName);
            }
         }

      }
      boolean addComma = false;
      for (int i = 0; i < selectCols.size(); i++) {
         // if the select column is not in the join column vector
         // then add it to our select column string
         String selCol = selectCols.get(i).toString();
         if (!join.contains(selCol)) {
            // to make sure we don't add a comma before the first column
            if (addComma) {
               selectStr += ", ";
            }

            selectStr += selCol;
            addComma = true;
         }
      }
      if (joinCol != null) {
         selectStr = this.qualifyColumn(selectStr, joinCol[0].columnName);
      }

      return selectStr;
   }

   // won't work for *
   private String qualifyColumn(String clause, String joinCol) {
      String table = node.getChildren().get(0).getNodeName() + "_table";
      String qualName = table + "." + joinCol;
      if (clause.contains(" " + joinCol + " ")) {
         int start = clause.indexOf(" " + joinCol + " ");
         // we want the end in the main string so we need to add the start index
         int end = start + clause.substring(start).indexOf(" ");
         String subStr = clause.substring(start, end);
         subStr.replace(joinCol, qualName);
         clause.replace(" " + joinCol + " ", subStr);
      }
      // shouldn't happen
//		else if (clause.contains(" " + joinCol + Character.MATH_SYMBOL)) {
//         int start = clause.indexOf(" " + joinCol + Character.MATH_SYMBOL);
//         // we want the end in the main string so we need to add the start index
//         int end = start
//               + clause.substring(start).indexOf(Character.MATH_SYMBOL);
//         String subStr = clause.substring(start, end);
//         subStr.replace(joinCol, qualName);
//         clause.replace(" " + joinCol + Character.MATH_SYMBOL, subStr);
//      }
      else if (clause.contains("(" + joinCol + " ")) {
         int start = clause.indexOf("(" + joinCol + " ");
         // we want the end in the main string so we need to add the start index
         int end = start + clause.substring(start).indexOf(" ");
         String subStr = clause.substring(start, end);
         subStr = subStr.replace(joinCol, qualName);
         clause = clause.replace("(" + joinCol + " ", subStr);
      } else if (clause.contains("(" + joinCol + ")")) {
         int start = clause.indexOf("(" + joinCol + ")");
         // we want the end in the main string so we need to add the start index
         int end = start + clause.substring(start).indexOf(")");
         String subStr = clause.substring(start, end);
         subStr.replace(joinCol, qualName);
         clause.replace("(" + joinCol + ")", subStr);
      }

      return clause;
   }

   /**
    * Get the select columns for a query
    * 
    * @return select columns sql string
    */
   private String getSelectColumnsTest() {
      //reuse the getSlectColumns by passing joinColumns as null
      //Expand the wildcard
      JoinColumn[] temp = null;
      return getSelectColumns(temp, true);
   }

   /**
    * Get the select columns for a query
    * 
    * @return select columns sql string
    */
   private String getSelectColumns() {
      Vector<?> selectCols = node.getNodeQuery().getSelect();
      String selectStr = "";

      for (int i = 0; i < selectCols.size(); i++) {
         String selCol = selectCols.get(i).toString();
         if (i > 0) {
            selectStr += ", ";
         }

         selectStr += selCol;
      }

      return selectStr;
   }

   /**
    * Get the join columns for a sql join
    * 
    * @return Array of join column names
    */
   private JoinColumn[] getJoinColumns() {
      //TODO: Verify there should be just one join Column. May be an overkill

      JoinColumn[] joinCols = null;

      DefaultDataNodeAggregationStrategy strategy = node
            .getNodeAggregationStrategy();
      Vector<DataNode> children = node.getChildren();
      if (strategy != null
            && strategy.getChildLevelFragmentationType() == LevelFragmentationType.VERTICAL) {
         int size = children.size();
         joinCols = new JoinColumn[size];
         for (int i = 0; i < size; i++) {
            joinCols[i] = strategy.getChildJoinColumn(children.get(i)
                  .getNodeName());
         }
      }

      return joinCols;
   }

   public void setAggregationType(DataAggregationType type) {
      this.type = type;
   }

   public DataAggregationType getAggregationType() {
      return type;
   }

   public void setDataNode(DataNode node) {
      this.node = node;
   }

   public DataNode getDataNode() {
      return node;
   }
}
