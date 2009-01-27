package org.iastate.ailab.qengine.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultPathPlannerImplentation.ChildNodesPlan;
import org.iastate.ailab.qengine.core.DefaultPathPlannerImplentation.ChildQuery;
import org.iastate.ailab.qengine.core.aggregators.DataAggregator;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataNode.DataAggregationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.QueryType;
import org.iastate.ailab.qengine.core.exceptions.RequestFlowException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.util.DatabaseUtils;
import org.iastate.ailab.qengine.core.util.NodeQueryUtils;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;

public class DefaultRequestFlowImplementation implements RequestFlow {

   private static final Logger logger = Logger
         .getLogger(DefaultRequestFlowImplementation.class);

   // static int i =0;
   int i = 0;

   public void execute(DataNode node, Object query) {

      ZQuery nodeQuery = (ZQuery) query;
      if (QueryUtils.isCountQuery(nodeQuery)) {
         node.setQueryType(QueryType.COUNT_QUERY);
      } else {
         node.setQueryType(QueryType.DATA_QUERY);
      }

      if (node.isLeafNode()) {
         QueryTransFormer transformer = Init._this().getNodeQueryTransFormer(
               node.getNodeName());

         nodeQuery = NodeQueryUtils.getMappedQuery(nodeQuery, node, false);

         logger.debug(node.getNodeName() + " To Be Re-Written-->" + nodeQuery);

         /*
          * call the transformer to rewrite the query, The transformer has
          * the reasoner ConversionFunctions also applied here
          */
         nodeQuery = (ZQuery) transformer.reWriteQuery(nodeQuery, node);

         logger.debug(node.getNodeName() + " Query rewritten-->" + nodeQuery);
         System.out.println("Query  be Executed: " + nodeQuery);

         // this is a leaf node so we only need to do PASS_THROUGH aggregation
         DataAggregator aggregator = SolutionCreator.getDataAggregatorImpl();
         aggregator.setAggregationType(DataAggregationType.PASS_THROUGH);
         aggregator.setDataNode(node);
         node.setDataAggregator(aggregator);

         if (node.getQueryType() == QueryType.DATA_QUERY) {
            // Query datasource. the results will be returned and automatically stored into a temp table by the DatabaseUtil

            try {
               DatabaseUtils.queryDataSource(node, nodeQuery, true);
            } catch (SQLException e) {
               throw new RequestFlowException("SQLException - "
                     + e.getMessage(), e);
            }
         } else if (node.getQueryType() == QueryType.COUNT_QUERY) {
            // no need to store results in a temp table so we need the ResultSet so we can store the count in the node
            Statement stmt = null;
            ResultSet rs;
            try {
               rs = DatabaseUtils.queryDataSource(node, nodeQuery, stmt, false);
            } catch (SQLException e) {
               throw new RequestFlowException("SQLException - "
                     + e.getMessage(), e);
            }

            // Assiming count query is of form "Select count(colName) from...."
            String colName = nodeQuery.getSelect().get(0).toString();
            try {
               while (rs.next()) {
                  int count = rs.getInt(colName);
                  node.setCount(count);

                  // node.setCount(rs.getInt("count")); //did not work for mysql, seemed okay for postgre
               }
            } catch (SQLException e) {
               throw new RequestFlowException("SQLException - "
                     + e.getMessage(), e);
            }
         }
      } else {
         PathPlanner planner = node.getPlanner();
         ChildNodesPlan plan = planner.getPathPlan(node, nodeQuery);
         // set aggregationType
         node.setDataAggregationType(plan.getDataAggregationType());

         if (plan.getResponseFlowQuery() != null) {
            node.setResponseQuery(plan.getResponseFlowQuery());
         }

         // create data aggregator here from a factory using the now known aggregationType
         DataAggregator aggregator = SolutionCreator.getDataAggregatorImpl();
         aggregator.setAggregationType(plan.getDataAggregationType());
         aggregator.setDataNode(node);
         node.setDataAggregator(aggregator);

         printPlanToLogger(plan);

         Vector<DataNode> children = node.getChildren();
         for (int i = 0; i < children.size(); i++) {
            DataNode child = children.get(i);
            ChildQuery cQuery = plan.getChildQueryForNode(child.getNodeName());
            if (cQuery.doesParticipateInQuery()) {
               child.execute(cQuery.getChildQuery());
            } else {
               child.setParticipating(false);
            }
         }
      }
   }

   public void printPlanToLogger(ChildNodesPlan plan) {

      String indent = getIndentation(i++);

      logger.trace(indent + "***" + plan.getNodeName() + "***");
      logger.trace(indent + "RETURN PATH AGGREGATION="
            + plan.getDataAggregationType());
      String responseFlowQuery = "N/A";
      if (plan.getResponseFlowQuery() != null) {
         responseFlowQuery = plan.getResponseFlowQuery().toString();
      }
      logger.trace(indent + "RESPONSE FLOW QUERY=" + responseFlowQuery);

      ChildQuery childQueries[] = plan.getChildQueries();
      logger.trace(indent + "CHILDREN=" + childQueries.length);
      indent += '\t';
      for (int i = 0; i < childQueries.length; i++) {
         ChildQuery currQuery = childQueries[i];
         logger.trace(indent + "CHILD_NAME=" + currQuery.getChildName());
         logger.trace(indent + "PARTICIPATES="
               + currQuery.doesParticipateInQuery());
         String childQuery = "N/A";
         if (currQuery.doesParticipateInQuery()) {
            childQuery = currQuery.childQuery.toString();
         }
         logger.trace(indent + "QUERY=" + childQuery);
      }
   }

   public void printPlan(ChildNodesPlan plan) {

      String indent = getIndentation(i++);

      System.out.println(indent + "***" + plan.getNodeName() + "***");
      System.out.println(indent + "RETURN PATH AGGREGATION="
            + plan.getDataAggregationType());
      String responseFlowQuery = "N/A";
      if (plan.getResponseFlowQuery() != null) {
         responseFlowQuery = plan.getResponseFlowQuery().toString();
      }
      System.out.println(indent + "RESPONSE FLOW QUERY=" + responseFlowQuery);

      ChildQuery childQueries[] = plan.getChildQueries();
      System.out.println(indent + "CHILDREN=" + childQueries.length);
      indent += '\t';
      for (int i = 0; i < childQueries.length; i++) {
         ChildQuery currQuery = childQueries[i];
         System.out.println(indent + "CHILD_NAME=" + currQuery.getChildName());
         System.out.println(indent + "PARTICIPATES="
               + currQuery.doesParticipateInQuery());
         String childQuery = "N/A";
         if (currQuery.doesParticipateInQuery()) {
            childQuery = currQuery.childQuery.toString();
         }
         System.out.println(indent + "QUERY=" + childQuery);
      }
   }

   private String getIndentation(int number) {
      String indentation = "\t";
      for (int index = 0; index < number; index++) {
         indentation += indentation;
      }
      return indentation;
   }
}
