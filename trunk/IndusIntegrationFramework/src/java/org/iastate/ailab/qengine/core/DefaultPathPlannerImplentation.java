package org.iastate.ailab.qengine.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy.JoinColumn;
import org.iastate.ailab.qengine.core.TransFormerUtility.QueryTranslationResult;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataNode.DataAggregationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.LevelFragmentationType;
import org.iastate.ailab.qengine.core.exceptions.SolutionCreaterException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.util.NodeQueryUtils;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;

public class DefaultPathPlannerImplentation implements PathPlanner {

   private static final Logger logger = Logger
         .getLogger(DefaultPathPlannerImplentation.class);

   // utility class
   public class ChildQuery {
      ZQuery childQuery; // will be null/not defined when does not participate in Query

      // will happen for aggregationType PASS_THROUGH
      /**
       * whether the current childnode helps to answer query. data sources
       * need not participate in all queries
       */
      boolean particpatesInQuery = true; // by default everyone participates

      String childName;

      public ZQuery getChildQuery() {
         return childQuery;
      }

      public boolean doesParticipateInQuery() {
         return particpatesInQuery;
      }

      public String getChildName() {
         return childName;
      }
   }

   // utility class for a plan
   public class ChildNodesPlan {
      String nodeName; // the name of node for whose children this plan exists

      DataAggregationType aggregationType;

      ZQuery responseFlowQuery;

      Map<String, ChildQuery> childQueries = new HashMap<String, ChildQuery>();

      public DataAggregationType getDataAggregationType() {
         return aggregationType;
      }

      public ZQuery getResponseFlowQuery() {
         return responseFlowQuery;
      }

      public ChildQuery[] getChildQueries() {
         int size = childQueries.size();

         ChildQuery[] res = new ChildQuery[size];
         int index = 0;
         Iterator<ChildQuery> it = childQueries.values().iterator();
         while (it.hasNext()) {
            res[index++] = it.next();
         }
         return res;
         //return (ChildQuery[]) childQueries.values().toArray();
      }

      public ChildQuery getChildQueryForNode(String childName) {
         return childQueries.get(childName);
      }

      /**
       * Returns the name of the node for whose children the plan is being
       * made
       * 
       * @return
       */
      public String getNodeName() {
         return nodeName;
      }
   }

   View currUserView;

   /**
    * Responsibility of the creator of this node to set aggregation
    * Strategy using a method provided for this purpose It is only required
    * for virtual(non-leaf) nodes.
    */
   DefaultDataNodeAggregationStrategy aggregationStrategy = null;

   QueryTransFormer qTransFormer = null; // set in constructor via call to init method

   /*
    * If the Query is such that in can be answered in a single path this
    * will contain the index of the child that answers the query. Here for
    * ease of programming.
    */
   Integer singlePathQueryIndex = null;

   public void setView(View view) {
      this.currUserView = view;
   }

   public DefaultPathPlannerImplentation() {
      init();
   }

   public void init() throws SolutionCreaterException {
      qTransFormer = SolutionCreator.getQueryTransformerImpl();
   }

   public ChildNodesPlan getPathPlan(DataNode currNode, ZQuery nodeQuery) {

      ChildNodesPlan plan = new ChildNodesPlan();
      plan.nodeName = currNode.getNodeName();

      Vector<DataNode> children = currNode.getChildren();
      ChildQuery cQuery = null;
      DataNode childNode = null;
      DefaultDataNodeAggregationStrategy nodeStrategy = currNode
            .getNodeAggregationStrategy();
      SplitType splitType = getSplitType(currNode, nodeQuery);

      for (int i = 0; i < children.size(); i++) {
         childNode = children.get(i);
         String childNodeName = childNode.getNodeName();
         cQuery = new ChildQuery();
         cQuery.childName = childNodeName;
         cQuery.particpatesInQuery = false;

         if (currNode.getDataSourceDescriptor().isSomeColumnPresent(
               QueryUtils.getQueryColumns(nodeQuery))) {
            QueryTranslationResult res = null;
            JoinColumn joinColumn = nodeStrategy
                  .getChildJoinColumn(childNodeName);
            switch (splitType) {
            case NO_SPLIT_SINGLE_PATH: {
               plan.aggregationType = DataAggregationType.PASS_THROUGH;
               if (i == singlePathQueryIndex.intValue()) {
                  res = qTransFormer.simpleTranslateQuery(nodeQuery, childNode);
                  if (res.participatesInTranslation) {
                     cQuery.particpatesInQuery = true;
                     cQuery.childQuery = res.query;
                  } else {
                     // SOMETHING BAD HAPPENED. This node should participate
                     logger.warn("ERROR: No Split Single Path Error: "
                           + childNodeName
                           + " should have participated in query " + nodeQuery);
                     //TODO should exception be thrown here?
                  }
               } else {
                  // this node does not participate in Query
                  logger.debug(childNodeName
                        + " does not participate in Query " + childNodeName);
               }
               break;
            }
            case NO_SPLIT: {
               if (childNode.getLevelFragmentationType() == LevelFragmentationType.HORIZONTAL) {
                  plan.aggregationType = DataAggregationType.DATA_ADD;
                  // generate a subset query (with the columns present in this DS along with)
                  res = qTransFormer.simpleTranslateQuery(nodeQuery, childNode);
                  if (res.participatesInTranslation) {
                     cQuery.particpatesInQuery = true;
                     cQuery.childQuery = res.query;
                  }
               }
               break;
            }
            case SPLIT_SELECT_NO_WHERE_NO: {
               if (QueryUtils.isCountQuery(nodeQuery)) {
                  /*
                   * We only need to get counts from the child which
                   * contain the where clause. We assume the vertically
                   * fragmented children have same ids;
                   */
                  plan.aggregationType = DataAggregationType.PASS_THROUGH;

                  Vector<String> whereColumns = QueryUtils
                        .getWhereColumns(nodeQuery);

                  if (NodeQueryUtils.isCurrentNodeContainTheseColumns(
                        childNode, whereColumns)) {

                     ZQuery countQueryWithJoinColumn = QueryUtils
                           .replaceCountSelectColumnsByJoinColumn(nodeQuery,
                                 joinColumn, childNode
                                       .getDataSourceDescriptor());
                     res = qTransFormer.simpleTranslateQuery(
                           countQueryWithJoinColumn, childNode);
                     cQuery.particpatesInQuery = true;
                     cQuery.childQuery = res.query;
                  } else {
                     /*
                      * This is the child which contains the select clause
                      * for count query but we don't need it. 'select
                      * count(a) where b > c' is equal to 'select
                      * (joinColumn) where b > c' so no need to get
                      * select(a).
                      */
                     cQuery.particpatesInQuery = false;
                  }
                  break;
               } else {
                  // it is a data query, fall down and do same as SPLIT_SELECT_YES_WHERE_NO
               }
            }
            case SPLIT_SELECT_YES_WHERE_NO: {
               /*
                * This can be done in two ways,
                * DataAggregationType.DATA_COMMON or first get ids
                * corresponding to condition and then get select clause. We
                * pick one. Once we move to optimization we can go ahead
                * and do that.
                */

               /*
                * In the DS containing the select clause, you get the
                * select columns and join columns from other data source
                * you get select columns (if any and corresponding joins).
                * You take rows corresponding to common join columns (ids).
                */
               plan.aggregationType = DataAggregationType.DATA_COMMON;

               ZQuery queryWithJoinColumn = nodeQuery; // in the beginning put it same as the passed query
               if (!QueryUtils.containsJoinColumn(nodeQuery, joinColumn)) {
                  queryWithJoinColumn = QueryUtils.addJoinColumn(nodeQuery,
                        joinColumn);
                  currNode.setProperty("system_added_join", "true");
               }

               // generate a subset query (with column names present in this query) + join Column;
               res = qTransFormer.simpleTranslateQuery(queryWithJoinColumn,
                     childNode);

               if (res.participatesInTranslation) {
                  cQuery.particpatesInQuery = true;
                  cQuery.childQuery = res.query;
               }
               break;
               /*
                * Currently count queries cannot have a select split (i.e.
                * of form select count(name), key from table with name and
                * key in different tables so they will never come here
                */
            }
            case SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET:
            case SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET: {
               /*
                * TODO: handle Count queries 'select name (where b>1 OR
                * c>2) AND (d >5) b,c in one DS and name,d in other DS'.
                * You have to get ids first that match condition from
                * individual data sources, take the union of ids and then
                * get the select columns corresponding to the common ids
                */
               plan.aggregationType = DataAggregationType.EXECUTE_REMOTE_QUERY_FOR_AND_CLAUSE;

               ZQuery joinColumnQuery = QueryUtils
                     .replaceSelectColumnsByJoinColumn(nodeQuery, joinColumn,
                           childNode.getDataSourceDescriptor());
               res = qTransFormer.simpleTranslateQuery(joinColumnQuery,
                     childNode);
               if (res.participatesInTranslation) {
                  cQuery.particpatesInQuery = true; // it should participate since joinColumn
                  cQuery.childQuery = res.query;
               }
               plan.responseFlowQuery = QueryUtils.simpleResponseQueryTemplate(
                     nodeQuery, joinColumn);
               break;
            }
            case SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET: // same logic for these two cases
            case SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET: {
               /*
                * TODO: Handle Count Queries 'select name (where b>1 OR
                * c>2) OR (d >5) b,c in one DS and name,d in other DS'. You
                * have to get ids first that match condition from
                * individual data sources, take the union of ids and then
                * get the select columns corresponding to the ids (from
                * both)
                */
               plan.aggregationType = DataAggregationType.EXECUTE_REMOTE_QUERY_FOR_OR_CLAUSE;
               ZQuery joinColumnQuery = QueryUtils
                     .replaceSelectColumnsByJoinColumn(nodeQuery, joinColumn,
                           childNode.getDataSourceDescriptor());
               res = qTransFormer.simpleTranslateQuery(joinColumnQuery,
                     childNode);
               if (res.participatesInTranslation) {
                  cQuery.particpatesInQuery = true; // it should participate since joinColumn
                  cQuery.childQuery = res.query;
               }

               plan.responseFlowQuery = QueryUtils.simpleResponseQueryTemplate(
                     nodeQuery, joinColumn);
               break;
            }
            case SPLIT_SELECT_NO_WHERE_YES_ALL_CONDITIONS_ANDED:
            case SPLIT_SELECT_YES_WHERE_YES_ALL_CONDITIONS_ANDED: {
               // select name where (b>1 OR c > 2) and (d >5> b,c in one DS and name,d in other DS
               plan.aggregationType = DataAggregationType.DATA_COMMON; // verify

               ZQuery queryWithJoinColumn = QueryUtils.addJoinColumn(nodeQuery,
                     joinColumn);
               res = qTransFormer.simpleTranslateQuery(queryWithJoinColumn,
                     childNode);
               if (res.participatesInTranslation) {
                  cQuery.particpatesInQuery = true;
                  cQuery.childQuery = res.query;
               }
               break;
            }
            case YES: {
               /*
                * Generate a query with all column names (NO conditions)
                * present in this datasource + join Column. The response
                * query is current query executed on the returned
                * table(Probably no need to add the join column condition).
                */
               plan.aggregationType = DataAggregationType.EXECUTE_LOCAL_QUERY;
               ZQuery queryWithJoinColumn = QueryUtils.addJoinColumn(nodeQuery,
                     joinColumn);
               res = qTransFormer.translate2GetAllColumnsQuery(
                     queryWithJoinColumn, childNode);
               if (res.participatesInTranslation) {
                  cQuery.particpatesInQuery = true;
                  cQuery.childQuery = res.query;
               }
               break;
            }
            default:
               // should throw an exception or in worst case assume it to be Yes
            }
         } else {
            /*
             * When No NO_SPLIT_SINGLE_PATH and one of child does not
             * participate. May happen in future when three children, 2
             * participate and third doesn't. Current version supports only
             * one version.
             */
            logger.debug(currNode.getNodeName()
                  + " does not participate for query " + nodeQuery);
            cQuery.particpatesInQuery = false;
         }

         plan.childQueries.put(childNode.getNodeName(), cQuery);
      } // complete for all children

      return plan;
   }

   private SplitType getSplitType(DataNode currNode, ZQuery nodeQuery) {
      SplitType result = SplitType.NOT_SUPPORTED;

      DefaultDataNodeAggregationStrategy strategy = currNode
            .getNodeAggregationStrategy();

      // for horizontal data or a leaf node(actual data source)there is no split
      if (currNode.isLeafNode()
            || strategy.getChildLevelFragmentationType() == LevelFragmentationType.HORIZONTAL) {
         return result = SplitType.NO_SPLIT;
      }

      // Handle vertical data
      if (strategy.getChildLevelFragmentationType() == LevelFragmentationType.VERTICAL) {
         /*
          * Check if the query can be answered from on path only. e.g
          * 'select SSN where name='ishaan' and salary < 200'. SSN, name
          * and salary in same Data Source. e.g select SSN where name
          * ='ishaan'.
          */
         int index = NodeQueryUtils.isSinglePathQuery(currNode, nodeQuery);
         if (index != -1) {
            singlePathQueryIndex = new Integer(index);
            return result = SplitType.NO_SPLIT_SINGLE_PATH;
         }

         // By Now A single Node can't answer the query, implies some sort of split
         Vector<String> whereColumns = QueryUtils.getWhereColumns(nodeQuery);
         Vector<String> selectColumns = QueryUtils.getSelectColumns(nodeQuery);

         boolean selectColumnsInSinglePath = false;
         boolean whereColumnsInSinglePath = false;

         if (NodeQueryUtils.isSingleChildrenContainTheseColumns(currNode,
               selectColumns)) {
            selectColumnsInSinglePath = true;
         }

         if (NodeQueryUtils.isSingleChildrenContainTheseColumns(currNode,
               whereColumns)) {
            whereColumnsInSinglePath = true;
         }

         if (whereColumnsInSinglePath) {
            if (selectColumnsInSinglePath) {
               // e.g select a,b where c>3 and d > 4
               // a,b occur in datasource1 and c, d in datasource2
               result = SplitType.SPLIT_SELECT_NO_WHERE_NO;
            } else {
               // e.g select a where b='nk' and c>3 and d > 4
               // a,b occur in datasource1 and c, d in datasource2
               result = SplitType.SPLIT_SELECT_YES_WHERE_NO;
            }
         } else {
            /*
             * Where columns not in singlePath. Also implies there are more
             * than one where conditions (since split requires at least
             * two).
             */
            if (QueryUtils.areAllOperandsAND(nodeQuery)) {
               if (selectColumnsInSinglePath) {
                  result = SplitType.SPLIT_SELECT_NO_WHERE_YES_ALL_CONDITIONS_ANDED;
               } else {
                  result = SplitType.SPLIT_SELECT_YES_WHERE_YES_ALL_CONDITIONS_ANDED;
               }
            } else if (NodeQueryUtils.isSubSetQueryForOperandOR(currNode,
                  nodeQuery)) {
               // e.g select a where (b > 3) OR ( c=1 and d >5)
               // select a,d where (b>3) OR (c =1 or d >5)
               // a,b in one data source c,d in others
               if (selectColumnsInSinglePath) {
                  result = SplitType.SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET;
               } else {
                  result = SplitType.SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ORED_BUT_SUBSET;
               }
            } else if (NodeQueryUtils.isSubSetQueryForOperandAND(currNode,
                  nodeQuery)) {
               // e.g select a where (b>3) AND (c=1 or d =5) a,b in
               // datasource1 c,d in datasource2
               if (selectColumnsInSinglePath) {
                  result = SplitType.SPLIT_SELECT_NO_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET;
               } else {
                  result = SplitType.SPLIT_SELECT_YES_WHERE_YES_CONDITIONS_ANDED_BUT_SUBSET;
               }
            } else {
               // Operands are split such that we can send query to nodes
               // select a where (b=1 and c=3) OR (d=5 or b =5)
               // select a where (b=1 and c=3) AND (d=5 or b =5)
               // a,b in datasource1 abd c,d in datasource2
               result = SplitType.YES;
            }
         }
      }

      return result;
   }
}
