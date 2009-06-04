package org.iastate.ailab.qengine.core.datasource;

import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy;
import org.iastate.ailab.qengine.core.DefaultPathPlannerImplentation;
import org.iastate.ailab.qengine.core.PathPlanner;
import org.iastate.ailab.qengine.core.RequestFlow;
import org.iastate.ailab.qengine.core.ResponseFlow;
import org.iastate.ailab.qengine.core.aggregators.DataAggregator;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;

public class DefaultDataNodeImplementation implements DataNode {

   private static final Logger logger = Logger
         .getLogger(DefaultDataNodeImplementation.class);

   private static int NODE_NUMBER = 0;

   private int nodeId; // system generated

   /**
    * The name of the node. Should Uniquely identify it
    */
   private String nodeName;

   // Properties of CurrNode and initialize to default values
   private NodeLocation nodeLocation = NodeLocation.NOT_SUPPORTED;

   private QueryType queryType = QueryType.NOT_SUPPORTED; // run time

   private LevelFragmentationType levelFragmentationType = LevelFragmentationType.NOT_SUPPORTED;

   private DataAggregationType dataAggregationType = DataAggregationType.NOT_SUPPORTED; // (run time)

   public String debug;

   ZQuery nodeQuery; // query passed from the parent (run time)

   ZQuery responseQuery; // query to be executed on the response flow path

   DataSourceDescriptor currDSDescriptor;

   DataSourceDescriptor mappedDSDescriptor; // for non leaf nodes it should be null

   DataAggregator nodeDataAggregator;

   DefaultDataNodeAggregationStrategy aggregationStrategy;

   Vector<DataNode> children; // children.

   PathPlanner planner;

   DataAggregator dataAggregator; // initialize or based on query

   RequestFlow requestFlow; // initialize;

   ResponseFlow responseFlow; // initialize

   private int count; // stores count from count queries

   private String dataSourceDriver;

   private String dataSource;

   /**
    * Whether the node participates in query, by default true Based on
    * Query the Planner/Optimizer may change it to false
    */

   boolean isParticipating = true;

   /**
    * used to store the column Name in the userview as mapped to data
    * source key columnName in userView value mappedColumnName in
    * datasource
    */
   Map<String, String> columnMap = new HashMap<String, String>();

   /**
    * used to store the column Name in the data source as mapped to user
    * view key-->currentColumnName value--> the column Name in userview
    * mapped to this
    * 
    */
   Map<String, String> reverseColumnMap = new HashMap<String, String>();

   /**
    * A map that stores mapping from the table in User view to the table in
    * remote datasource
    */
   private final Map<String, String> tableMap = new HashMap<String, String>();

   private final Map<String, String> reverseTableMap = new HashMap<String, String>();

   private final Properties dataNodeProperties = new Properties();

   public DefaultDataNodeImplementation() {
      children = new Vector<DataNode>();
      nodeId = NODE_NUMBER++;
      Init();
   }

   void Init() {

      requestFlow = SolutionCreator.getRequestFlow();
      responseFlow = SolutionCreator.getResponseFlow();

      // TODO Move Planner to a factory implementation
      planner = new DefaultPathPlannerImplentation();
   }

   public DefaultDataNodeImplementation(String name) {
      this.nodeName = name;
      nodeId = NODE_NUMBER++;
      children = new Vector<DataNode>();
   }

   public void setNodeType(NodeLocation nodeType) {
      this.nodeLocation = nodeType;
   }

   public void setQueryType(QueryType queryType) {
      this.queryType = queryType;
   }

   public void setLevelFragmentationType(
         LevelFragmentationType levelFragmentationType) {
      this.levelFragmentationType = levelFragmentationType;
   }

   public void setDataAggregationType(DataAggregationType dataAggregationType) {
      this.dataAggregationType = dataAggregationType;
   }

   public DataAggregationType getDataAggregationType() {
      return dataAggregationType;
   }

   public DataAggregator getDataAggregator() {
      return dataAggregator;
   }

   public void setDataAggregator(DataAggregator dataAggregator) {
      this.dataAggregator = dataAggregator;
   }

   public void setNodeQuery(ZQuery query) {
      this.nodeQuery = query;
   }

   public void setResponseQuery(ZQuery query) {
      this.responseQuery = query;
   }

   public ZQuery getResponseQuery() {
      return responseQuery;
   }

   public ZQuery getNodeQuery() {
      return nodeQuery;
   }

   public void addChild(DataNode child) {
      children.add(child);
   }

   public Vector<DataNode> getChildren() {
      return children;
   }

   public String getNodeName() {
      return nodeName;
   }

   public void setNodeName(String nodeName) {
      this.nodeName = nodeName;
   }

   public boolean isParticipatingInQuery() {
      return isParticipating;
   }

   public void setParticipating(boolean isParticipating) {
      this.isParticipating = isParticipating;
   }

   public boolean isLeafNode() {
      return (nodeLocation == NodeLocation.LEAF_NODE);
   }

   public DataSourceDescriptor getDataSourceDescriptor() {
      logger.trace(this.nodeName + ".getDataSourceDescriptor() -> isnull? "
            + (currDSDescriptor == null));
      return currDSDescriptor;
   }

   public void setDataSourceDescriptor(DataSourceDescriptor desc) {
      this.currDSDescriptor = desc;
   }

//   public DataSourceDescriptor getMappedDataSourceDescriptor() {
//      return mappedDSDescriptor; }

   public void setMappedDataSourceDescriptor(DataSourceDescriptor mappedDesc) {
      this.mappedDSDescriptor = mappedDesc;
   }

   public LevelFragmentationType getLevelFragmentationType() {
      return levelFragmentationType;
   }

   public PathPlanner getPlanner() {
      return planner;
   }

   public String getProperty(String key) {
      return (String) dataNodeProperties.get(key);
   }

   public void setProperty(String key, String value) {
      dataNodeProperties.put(key, value);
   }

   public boolean isAnswerableQuery(Object query) {
      ZQuery zQuery = (ZQuery) query;
      boolean res = false;
      Vector<String> queryColumns = QueryUtils.getQueryColumns(zQuery);
      if (query.toString().contains("*")) {
         // if star is a query column remove it because it really means "all columns"
         queryColumns.remove("*");
      }
      if (currDSDescriptor.areAllColumnsPresent(queryColumns)) {
         res = true;
      }

      return res;
   }

   // This is where the whole thing occurs
   public ResultSet execute(Object query, Statement stmt) throws Exception {
      // TODO: Add logic to see if the query can be answered by this node
      ZQuery currQuery = (ZQuery) query;
      this.setNodeQuery(currQuery);
      requestFlow.execute(this, currQuery);
      ResultSet rs = responseFlow.execute(this, stmt);

      return rs;
   }

   // we should never use this one if we want results back as a ResultSet
   public void execute(Object query) throws Exception {
      Statement stmt = null;
      this.execute(query, stmt);
   }

   // TODO This map thing may take too much memory.
   // First round of implementation is Okay
   // Stub it out by making call to a View Reader
   public String getUserViewColumnName(String dataSourceColumnName) {
      if (!isLeafNode())
         return dataSourceColumnName;

//      System.out.println("Reverse Size() " + reverseColumnMap.size());
//      Collection v = reverseColumnMap.keySet();
//      Iterator it = v.iterator();
//      while (it.hasNext()) {
//         System.out.println("Value '" + dataSourceColumnName + "' =?: '"
//               + it.next() + "'");
//      }

      return reverseColumnMap.get(dataSourceColumnName);
   }

   // utility function, Stores so that information can be retrieved using getUserViewColumnName
   public void addToReverseColumnMap(String thisDataSourceColumnName,
         String userViewColumnName) {
      reverseColumnMap.put(thisDataSourceColumnName, userViewColumnName);
      columnMap.put(userViewColumnName, thisDataSourceColumnName);
   }

   public String getDataSourceColumnName(String userViewColumnName) {
      if (!isLeafNode())
         return userViewColumnName;

      return columnMap.get(userViewColumnName);
   }

   public String getMappedDataSourceColumnName(String userViewColumnName) {
      // TODO Make sure the column exists for inner nodes
      if (!isLeafNode())
         return userViewColumnName;

      return columnMap.get(userViewColumnName);
   }

   // utility function, Stores so that information can be retrieved using getDataSourceColumnName
   public void addToColumnMap(String userViewColumnName,
         String thisDataSourceColumnName) {
      columnMap.put(userViewColumnName, thisDataSourceColumnName);
      reverseColumnMap.put(thisDataSourceColumnName, userViewColumnName);
   }

   public void addTableMap(String userViewTable, String dataSourceTable) {
      tableMap.put(userViewTable, dataSourceTable);
      reverseTableMap.put(dataSourceTable, userViewTable);
   }

   public String getUserViewTable(String dataSourceTable) {
      return reverseTableMap.get(dataSourceTable);
   }

   public String getMappedDataSourceTable(String userViewTable) {
      // TODO: In future a userView table for leaf node may be spread over tables
      return tableMap.get(userViewTable);
   }

   public String getDataSourceTable(String userViewTable) {
      return tableMap.get(userViewTable);
   }

   // returns strategy as how to join Nodes in the DataTree
   public DefaultDataNodeAggregationStrategy getNodeAggregationStrategy() {
      return aggregationStrategy;
   }

   public void setDataNodeAggregationStrategy(
         DefaultDataNodeAggregationStrategy aggregationStrategy) {
      this.aggregationStrategy = aggregationStrategy;
   }

   public QueryType getQueryType() {
      return this.queryType;
   }

   public void setCount(int count) {
      this.count = count;
   }

   public int getCount() {
      return this.count;
   }

   public void setDataSourceDriver(String driver) {
      this.dataSourceDriver = driver;
   }

   public String getDataSourceDriver() {
      return this.dataSourceDriver;
   }

   public void setDataSource(String dataSource) {
      this.dataSource = dataSource;
   }

   public String getDataSource() {
      return this.dataSource;
   }

   // TODO probably should store database connection info in leaf nodes
}
