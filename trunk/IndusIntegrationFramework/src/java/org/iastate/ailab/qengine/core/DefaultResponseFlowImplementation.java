package org.iastate.ailab.qengine.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataNode.DataAggregationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.QueryType;
import org.iastate.ailab.qengine.core.exceptions.RequestFlowException;
import org.iastate.ailab.qengine.core.exceptions.ResponseFlowException;
import org.iastate.ailab.qengine.core.util.DatabaseUtils;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;

public class DefaultResponseFlowImplementation implements ResponseFlow {

   private static final Logger logger = Logger
         .getLogger(DefaultResponseFlowImplementation.class);

   //private ZQuery responseFlowQuery;

   //private DataAggregationType dataAggregationType;

   //public void setResponseFlowQuery(ZQuery responseFlowQuery) {
   // this.responseFlowQuery = responseFlowQuery;
   //}

   public ResultSet execute(DataNode node, Statement stmt) {
      ResultSet rs = null;

      logger.debug("Response Flow Executing for node=" + node.getNodeName()
            + "-->");
      if ((node.getDataAggregationType() == DataAggregationType.EXECUTE_REMOTE_QUERY_FOR_OR_CLAUSE)
            || (node.getDataAggregationType() == DataAggregationType.EXECUTE_REMOTE_QUERY_FOR_AND_CLAUSE)) {

         // get query with placeholder replaced with actually values
         ZQuery resolvedQuery = replaceWithValues(node, node.getResponseQuery());
         //TODO: For above handle case when previous query returns no values
         //In End to do this execute may have to return a custome wrapper around ResultSet 

         // cleanup child node temp tables so that we can re-use them for another requestflow query
         for (DataNode childNode : node.getChildren()) {
            try {
               DatabaseUtils.removeTempTable(childNode);
            } catch (SQLException e) {
               throw new ResponseFlowException("SQLException - "
                     + e.getMessage(), e);
            }
         }

         // execute the new query on this node
         rs = node.execute(resolvedQuery, stmt); //when moving to multithread be careful about locks
      } else {

         //it should set the aggregator based on DataAggregationType and do the needful

         // aggregate here and then do cleanup afterward
         try {
            rs = node.getDataAggregator().aggregate(stmt);
         } catch (SQLException e) {
            throw new RequestFlowException("SQLException - " + e.getMessage(),
                  e);
         }

         // cleanup temp tables of the children nodes
         for (DataNode childNode : node.getChildren()) {
            if (childNode.getQueryType() == QueryType.DATA_QUERY) {
               // only do cleanup if it's a data query because there are no temp tables created for a count query
               try {
                  DatabaseUtils.removeTempTable(childNode);
               } catch (SQLException e) {
                  throw new RequestFlowException("SQLException - "
                        + e.getMessage(), e);
               }
            }
         }
      }

      return rs;
   }

   // public void setDataAggregationType(DataAggregationType dataAggregationType) {
   //   this.dataAggregationType = dataAggregationType;
   //}

   /**
    * Replace the where clause placeholder with the actual key values
    * 
    * @param node Current data node
    * @param query Select clause containing the where clause with
    * placeholder
    * @return query containing where clause with all key values
    */
   private ZQuery replaceWithValues(DataNode node, ZQuery query) {
      //replace the responseFlowQuery place holders with appropriate values from the results of earlier query
      //TODO Implementation

      String q = query.toString();

      Vector<String> keys = getKeyValues(node);
      //TODO: What if no keys are returned. In that case $keyValues$ need to be set to some non existent query values
      //Better still there is no need to submit the query

      String whereClause = query.getWhere().toString();

      // Add a where clause separated by an 'OR' for every key in the vector
      //TODO  Why not use IN clause
      if (keys.size() > 0) {
         q = q.replace("keyValues", keys.remove(0));
         for (String key : keys) {
            q += " OR " + whereClause.replace("keyValues", key);
         }
         q += ";";
      } else {
         //TODO: this is more  of workaround. We should throw an Exception so that
         //users of function no that this query will return an empty Result and hence no need to send the query at all
         //Also assuing -65535 is not a value of key. Use int value because will work even if joinCol is varChar

         q = q.replace("keyValues", "-65535");
      }

      query = QueryUtils.getZQueryFromString(q);
      return query;
   }

   // get the values we want to use in the where clause
   /**
    * Get a Vector<String> of all key values to be used in the where
    * clause
    * 
    * @param node Current data node
    * @return Vector of Strings containing all key values
    */
   private Vector<String> getKeyValues(DataNode node) {
      Vector<DataNode> children = node.getChildren();

      /*
       * Assumes binary tree String table1 = children.get(0).getNodeName() +
       * "_table"; String table2 = children.get(1).getNodeName() +
       * "_table";
       * 
       * String selectCol1 = getSelectColumns(children.get(0)); String
       * selectCol2 = getSelectColumns(children.get(1)); // query to select
       * all key values from local temp tables String query = "select " +
       * selectCol1 + " from " + table1; query += " union"; query += "
       * select " + selectCol2 + " from " + table2 + ";";
       */

      String query = "";
      boolean addConnecter = false;
      for (DataNode child : children) {
         String table = child.getNodeName() + "_table";
         String selectCols = getSelectColumns(child);

         if (addConnecter) {
            if (node.getDataAggregationType() == DataAggregationType.EXECUTE_REMOTE_QUERY_FOR_OR_CLAUSE)
               query += " union "; // remote query for or
            else
               query += " intersect "; // remote query for and
         }

         query += "select " + selectCols + " from " + table;
         addConnecter = true;
      }
      query += ";";

      ZQuery q = QueryUtils.getZQueryFromString(query);

      Vector<String> keys = new Vector<String>();
      Statement stmt = null;
      try {
         ResultSet rs = DatabaseUtils.queryDataSource(node, q, stmt, false);

         // add each key from the query to the vector
         while (rs.next()) {
            keys.add(rs.getString(1));
         }
      } catch (SQLException e) {
         logger.error("SQLException", e);
         throw new ResponseFlowException("SQLException - " + e.getMessage(), e);
      }

      return keys;
   }

   /**
    * Get the select columns for a query
    * 
    * @return select columns sql string
    */
   private String getSelectColumns(DataNode node) {
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
}
