package org.iastate.ailab.qengine.core.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.ConnectionManager;
import org.iastate.ailab.qengine.core.datasource.DataNode;

import Zql.ZQuery;

public class DatabaseUtils {
   private static final Logger logger = Logger.getLogger(DatabaseUtils.class);

   private static HashMap<String, String> dataSourceMap = new HashMap<String, String>();

   private static int tableNum = 1; // append to table names;

   // TODO: make use of the tableNum

   /**
    * Queries the nodes datasource and stores the results locally
    * 
    * @param node the DataNode to be queried
    * @param query the query to be used
    * @return a ResultSet that is closed (probably could be changed to
    * return void)
    * @throws SQLException
    */
   public static ResultSet queryDataSource(DataNode node, ZQuery query,
         boolean saveLocally) throws SQLException {

      Statement stmt = getConnection(node).createStatement();
      ResultSet rs = stmt.executeQuery(query.toString()); //.toLowerCase());

      if (saveLocally) {
         storeResultsLocally(rs, node);
      }

      return rs;
   }

   // A Statement object is passed so that the statement will stay open even after the method returns
   /**
    * Queries the nodes datasource and returns the results
    * 
    * @param node the DataNode to be queried
    * @param query the query to be used
    * @param stmt a Statement object so that the ResultSet isn't closed
    * upon exiting the method
    * @return a ResultSet containing the results of the query
    * @throws SQLException
    */
   public static ResultSet queryDataSource(DataNode node, ZQuery query,
         Statement stmt, boolean saveLocally) throws SQLException {

      stmt = getConnection(node).createStatement();
      ResultSet rs = stmt.executeQuery(query.toString()); //.toLowerCase());

      if (saveLocally) {
         storeResultsLocally(rs, node);
      }

      return rs;
   }

   // A Statement object is passed so that the statement will stay open even after the method returns
   /**
    * Queries the nodes datasource and returns the results
    * 
    * @param node the DataNode to be queried
    * @param query the query to be used
    * @param stmt a Statement object so that the ResultSet isn't closed
    * upon exiting the method
    * @return a ResultSet containing the results of the query
    * @throws SQLException
    */
   public static ResultSet queryDataSource(DataNode node, String query,
         Statement stmt, boolean saveLocally) throws SQLException {

      stmt = getConnection(node).createStatement();
      ResultSet rs = stmt.executeQuery(query); //.toLowerCase());

      if (saveLocally) {
         storeResultsLocally(rs, node);
      }

      return rs;
   }

   /**
    * Queries the nodes datasource and stores the results locally
    * 
    * @param node the DataNode to be queried
    * @param query the query to be used
    * @return a ResultSet that is closed (probably could be changed to
    * return void)
    * @throws SQLException
    */
   public static ResultSet queryDataSource(DataNode node, String query,
         boolean saveLocally) throws SQLException {

      Statement stmt = getConnection(node).createStatement();
      ResultSet rs = stmt.executeQuery(query); //.toLowerCase());

      if (saveLocally) {
         storeResultsLocally(rs, node);
      }

      return rs;
   }

   // map a dataSource name to an actual connectionString for the dataSource
   public static void addDataSource(String dataSourceName,
         String connectionString) {
      dataSourceMap.put(dataSourceName, connectionString);
   }

   /**
    * Get a Connection to the provided dataSource
    * 
    * @param node the data source to connect to
    * @return an open Connection object
    * @throws SQLException
    */
   private static Connection getConnection(DataNode node) throws SQLException {
      return ConnectionManager.getConnection(node);
   }

   /**
    * Gives connection with the system database associated with indus
    * 
    * @throws SQLException
    */
   private static Connection getIndusConnection() throws SQLException {
      return ConnectionManager.getIndusConnection();
   }

   /**
    * Store the given ResultSet to a local table named after the given node
    * name
    * 
    * @param rs the ResultSet to be stored locally
    * @param node the current DataNode
    * @throws SQLException
    */
   public static void storeResultsLocally(ResultSet rs, DataNode node)
         throws SQLException {

      String tableName = node.getNodeName() + "_table";

      /*
       * TODO: What if multiple sessions. Do you need session id? The
       * session with Query Engine. But init is static check if table
       * exists before creating it
       */
      removeTempTable(node);

      //create temp table
      //Statement stmt = getConnection(queryEngine).createStatement();
      Statement stmt = getIndusConnection().createStatement();
      String query = getCreateTableQuery(rs, node, tableName);
      stmt.executeUpdate(query); //.toLowerCase());

      // insert the resultset into the new temp table
      ResultSetMetaData metaData = rs.getMetaData();
      int colCount = metaData.getColumnCount();

      while (rs.next()) {
         String insertQuery = "INSERT INTO " + tableName + " VALUES (";
         for (int i = 1; i <= colCount; i++) {
            if (i > 1) {
               insertQuery += ", ";
            }

            int type = metaData.getColumnType(i);
            switch (type) {
            // these will all become ints
            case Types.BIGINT:
            case Types.INTEGER:
            case Types.SMALLINT:
            case Types.TINYINT:
            case Types.DECIMAL:
            case Types.DOUBLE:
            case Types.FLOAT:
            case Types.NUMERIC:
               // it's number so we don't need ''
               insertQuery += rs.getString(i);
               break;
            default:
               logger.trace("Assuming that the java.sql.Types value " + type
                     + " needs to be surrounded with single quotes");
               insertQuery += "'" + rs.getString(i) + "'";
               break;
            // TODO add support for dates
            }
         }

         insertQuery += ")";
         logger.debug("insert query=" + insertQuery);

         // TODO make better method for dealing with case issues
         // probably need a class to fix queries made to postgre databases
         // stmt.executeUpdate(insertQuery.toLowerCase());
         stmt.executeUpdate(insertQuery);
      }
   }

   /**
    * Since we are returning a ResultSet object to the user we need to
    * somehow get a ResultSet even for a Count query. In order to do this,
    * this method can be called from the root node to store the count into
    * the local DB. Then a select query can be executed to get the count
    * into a ResultSet that can be returned
    * 
    * @param count count stored in DataNode
    * @param node current DataNode
    * @throws SQLException
    */
   public static void storeCountResultsLocally(DataNode node)
         throws SQLException {

      String table = node.getNodeName() + "_table";
      String query = "CREATE TABLE " + table + " (count int8)";
      // create table to store count results in

      Statement stmt = getIndusConnection().createStatement();
      //Statement stmt = getConnection("jdbc:postgresql:queryEngine").createStatement();

      // create temp table
      // TODO check if table exists before creating it

      stmt.executeUpdate(query.toLowerCase());

      // Insert count into table
      String insertQuery = "INSERT INTO " + table + " VALUES("
            + node.getCount() + ")";

      stmt.executeUpdate(insertQuery.toLowerCase());
   }

   /**
    * Get a query to create a table for a given ResultSet
    * 
    * @param rs the ResultSet that we need to store in a new table
    * @param node the current DataNode
    * @param tableName the name for the table that is to be created
    * @return a sql query to create a table to store the given ResultSet in
    * @throws SQLException
    */
   private static String getCreateTableQuery(ResultSet rs, DataNode node,
         String tableName) throws SQLException {

      //TODO: Handle the cases The exact form of query will be different for mysql/postgre
      //Check from Indus Configuration what kind of database is associated with indus

      //mysql CREATE TABLE 'pet' ('name' VARCHAR(20), 'owner' VARCHAR(20), 'death' DATE);
      tableName = "`" + tableName + "`";

      // name table after the node name
      String query = "CREATE TABLE " + tableName + " (";

      ResultSetMetaData metaData = rs.getMetaData();

      for (int i = 1; i <= metaData.getColumnCount(); i++) {
         String columnName = metaData.getColumnName(i);
         String columnType = metaData.getColumnTypeName(i);

         columnName = node.getUserViewColumnName(columnName); //map to userView
         columnName = "`" + columnName + "`";
         if (!query.contains(columnName + " " + columnType)) {
            if (i > 1) {
               query += ", ";
            }

            query += columnName + " " + columnType;
            if (columnType.equalsIgnoreCase("varchar")) {
               String defaultVarcharSize = "255";
               logger.trace("Using " + defaultVarcharSize
                     + " as the size of all varchars");
               query += "(" + defaultVarcharSize + ")";
            }
         }
      }

      query += ")";
      logger.trace("create query=" + query);

      return query;
   }

   /**
    * Delete temp table that goes with the provided node
    * 
    * @param node the data node to delete the temp table for
    * @throws SQLException
    */
   public static void removeTempTable(DataNode node) throws SQLException {
      //[IF EXISTS] checks if table exists before trying to drop it

      String dropQuery = "DROP TABLE IF EXISTS " + node.getNodeName()
            + "_table";

      //Statement stmt = getConnection("jdbc:postgresql:queryEngine").createStatement();
      Statement stmt = getIndusConnection().createStatement();
      stmt.executeUpdate(dropQuery);
   }

   public static int getCurrentTableNum() {
      return tableNum;
   }

   public static void iterateTableNum() {
      tableNum++;
   }
}
