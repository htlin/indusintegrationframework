package org.iastate.ailab.qengine.core.datasource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy;
import org.iastate.ailab.qengine.core.PathPlanner;
import org.iastate.ailab.qengine.core.aggregators.DataAggregator;

import Zql.ZQuery;

public interface DataNode {
   //declare enums
   public enum NodeLocation {
      LEAF_NODE, INNER_NODE, NOT_SUPPORTED
   }

   public enum QueryType {
      COUNT_QUERY, DATA_QUERY, NOT_SUPPORTED
   }

   //All the nodes in a level have same aggregation
   public enum LevelFragmentationType {
      HORIZONTAL, /* CurrLevel is Horizonatl Aggregation */
      VERTICAL, /* CurrLevel is Vertical Aggregation */
      NOT_SUPPORTED
   }

   /**
    * 
    * @author neeraj This describes once data is returned from children how
    * to combine/aggrgate them The optimizer should decide which way to
    * proceed
    */

   public enum DataAggregationType {
      /*
       * Take all IDs (assume ID is primary key Example Occurence: 1)When
       * data horizontally distributed
       */
      DATA_ADD,

      /*
       * Take common IDs (discard rest) Example Occurence: 1) When data is
       * vertical and you get Ids matching different conditions from
       * different child and the operator between conditions is AND
       */
      DATA_COMMON,

      /*
       * All children should return same Ids, joing them Example Occurence:
       * 1) When Data Vertical, no where clause and columns in select
       * clause occur in different children Remark: since both will return
       * same ids, DATA_COMMON will take care of it
       */
      DATA_UNION,

      /*
       * Only one child participates in Query.No Need to Aggregate
       */
      PASS_THROUGH,

      /*
       * When data is vertical and you get Ids matching different
       * conditions from different child and the operator between
       * conditions is OR You first get ids from each child that match
       * condition, join them and then execute a remote query to get select
       * columns corresponding to those ids If select columns split you may
       * have to join them (DATA_UNION) (Verify since you may not get all
       * ids corresponding to select clause)
       */
      EXECUTE_REMOTE_QUERY_FOR_OR_CLAUSE,

      EXECUTE_REMOTE_QUERY_FOR_AND_CLAUSE,

      /*
       * The response date needs to be joined and an query needs to be
       * executed on top of it. Example Occurence: 1) When data vertical,
       * the conditions columns are split across children in such a way
       * there is not an 'AND' and 'OR' operator between them. Select id
       * where (a > 3 & b>4) || (f=6) A,f from one data source and b from
       * other child.
       * 
       * Remark: All aggregation can fall back to this model You can always
       * get all the data and then do selection on it(but it is not the
       * most optimized)
       */
      EXECUTE_LOCAL_QUERY,

      /*
       * get all data from the join columns of 2 data sources then do a
       * data_common on them into a temp table then execute another query
       * doing column IN joincolumns
       */
      NOT_SUPPORTED
   }

   //setters
   public void setNodeType(NodeLocation nodeType);

   public void setQueryType(QueryType queryType);

   public void setLevelFragmentationType(
         LevelFragmentationType levelFragmentation);

   public void setDataAggregationType(DataAggregationType dataAggregationType);

   public void setDataAggregator(DataAggregator dataAggregator);

   public void setResponseQuery(ZQuery query);

   public void setCount(int count);

   public void setNodeName(String name);

   public void setDataSourceDriver(String driver);

   public void setDataSource(String dataSource);

   public void addChild(DataNode child);

   public void setDataNodeAggregationStrategy(
         DefaultDataNodeAggregationStrategy aggregationStrategy);

   public void setDataSourceDescriptor(DataSourceDescriptor desc);

   public void setProperty(String key, String value);

   //Don't require this
   //public void setMappedDataSourceDescriptor(DataSourceDescriptor mappedDesc);

   public void addTableMap(String userViewTable, String dataSourceTable);

   //TODO: In future can replace by schemaMap
   public void addToReverseColumnMap(String thisDataSourceColumnName,
         String userViewColumnName);

   public void setParticipating(boolean isParticipating);

   public String getUserViewTable(String dataSourceTable);

   public LevelFragmentationType getLevelFragmentationType();

   public DataAggregationType getDataAggregationType();

   public DataAggregator getDataAggregator();

   public ZQuery getNodeQuery();

   public QueryType getQueryType();

   public int getCount();

   public String getDataSource(); //This is the associated datasource with the node;

   //public String getDataSourceDriver();

   public String getProperty(String key);

   public PathPlanner getPlanner();

   public String getNodeName();

   public boolean isParticipatingInQuery();

   public boolean isLeafNode();

   /**
    * contains the description of the datasource All the columns in this
    * datasource are a subset of one in userView e.g userView: columnNames
    * {key,benefits.firstName} e.g dataSource Columns {key,benefits}
    * 
    * @return
    */
   public DataSourceDescriptor getDataSourceDescriptor();

   /**
    * For leaf nodes it contains the DataSourceDescriptor of the actual
    * data source e.g userView: columnNames {key,benefits.firstName} e.g
    * dataSource Columns {SSN,salary} where key-->SSN and benefits-->Salary
    * No correspondence to firstName
    * 
    * @return
    */
   // public DataSourceDescriptor getMappedDataSourceDescriptor();
   public ZQuery getResponseQuery();

   public Vector<DataNode> getChildren();

   /**
    * Applicable to leaf nodes. Given a column name in a leaf node should
    * return what is the corresponding column name in user view For inner
    * nodes returns the passed value TODO Handle for Multiple Tables
    * 
    * @param dataSourceColumnName
    * @return
    */
   public String getUserViewColumnName(String dataSourceColumnName);

   /**
    * for a columnName in user view returns the corresponding column in
    * datasource
    * 
    * @param userViewColumnName
    * @return
    */
   public String getDataSourceColumnName(String userViewColumnName);

   /**
    * for inner nodes same behavior as <code>getDataSourceColumnName</code>
    * For leaf node returns the mapped column corresponding to the column
    * in user view
    * 
    * @param userViewColumnName
    * @return
    */
   public String getMappedDataSourceColumnName(String userViewColumnName);

   /**
    * given a table is userview returns the corresponding table in the
    * datasource TODO In future have to do it per column since a table in
    * userview may be spread over two or more tables in datasource
    * 
    * @param userViewTable
    * @return
    */
   public String getDataSourceTable(String userViewTable);

   /**
    * for virtual nodes it is same as </code> getDataSourceTable <code> for
    * leaf nodes return the actual mapped table corresponding to userView
    * 
    * @param userViewTable
    * @return
    */
   public String getMappedDataSourceTable(String userViewTable);

   //returns strategy as how to join Nodes in the DataTree
   public DefaultDataNodeAggregationStrategy getNodeAggregationStrategy();

   /**
    * checks if this node can answer the particular query
    * 
    * @param query
    * @return
    */
   public boolean isAnswerableQuery(Object query);

   //the workhorse for our default Implementation the query will be Zquery
   public void execute(Object query) throws Exception;

   /*
    * we must pass the Statement object around so that it will be open when
    * the ResultSet is returned to the user
    */
   public ResultSet execute(Object query, Statement stmt) throws Exception;
}
