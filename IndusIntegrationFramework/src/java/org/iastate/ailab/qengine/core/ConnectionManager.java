package org.iastate.ailab.qengine.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.IndusConfiguration.DbType;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;

public class ConnectionManager {
   /*
    * TODO: Do connection pooling for efficiency. May be even a separate
    * thread so that it is done in parallel while the view is being set
    */
   private static final Logger logger = Logger
         .getLogger(ConnectionManager.class);

   public static Connection getConnection(DataNode node) throws SQLException {

      IndusConfiguration indusConfig = Init._this().getIndusConfiguration();
      if (node.isLeafNode()) {
         String hostname = node.getProperty("host");
         String dbName = node.getProperty("datasource");

         // if datasource is not set, use the leaf node name itself
         if (dbName == null || dbName == "") {
            dbName = node.getNodeName();
         }

         DbType dbType = DbType.valueOf(node.getProperty("type"));
         String temp = indusConfig.getProperty("account_" + dbName,
               "indus;indus");

         String[] userNamePassword = temp.split(";");
         String userName = userNamePassword[0];
         String password = userNamePassword[1];

         return getDataSourceConnection(hostname, dbName, userName, password,
               dbType);
      } else {
         // This a virtual DataSource, so make connection to the database associated with indus
         return getIndusConnection();
      }
   }

   public static Connection getIndusConnection() throws SQLException {
      IndusConfiguration indusConfig = Init._this().getIndusConfiguration();

      String dbName = indusConfig.getIndusDbName();
      DbType dbType = indusConfig.getIndusDbType();

      String userNamePassword[] = indusConfig.getProperty("account_indus",
            "indus;indus").split(";");
      String userName = userNamePassword[0];
      String password = userNamePassword[1];

      String hostname = indusConfig.getProperty("hostname_indus", "localhost");

      return getDataSourceConnection(hostname, dbName, userName, password,
            dbType);
   }

   public void closeConnection(DataNode node, Connection connection) {
      try {
         // When implementing pooling, add it to the pool
         connection.close();
      } catch (SQLException e) {
         logger.error("SQLException while tyring to close the Connection: "
               + connection, e);
      }
   }

   private static Connection getDataSourceConnection(String host,
         String database, String userName, String password, DbType dbType)
         throws SQLException {

      // TODO; Give error message as to which database you where connecting if connection fails
      String url;
      switch (dbType) {
      case mysql:
         SolutionCreator.instantiate("com.mysql.jdbc.Driver");
         url = "jdbc:mysql://" + host + "/" + database;
         break;
      case postgre:
         SolutionCreator.instantiate("org.postgresql.Driver");
         url = "jdbc:postgresql://" + host + "/" + database;
         break;
      // TODO Add cases for other datasources (e.g oracle)
      default:
         throw new IllegalArgumentException(
               "Unsupported relational database type: " + dbType);
      }
      return DriverManager.getConnection(url, userName, password);
   }
}
