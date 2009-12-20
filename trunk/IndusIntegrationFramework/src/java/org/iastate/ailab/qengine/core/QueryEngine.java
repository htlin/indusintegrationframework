package org.iastate.ailab.qengine.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.datasource.DataNode.QueryType;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.exceptions.EngineException;
import org.iastate.ailab.qengine.core.util.QueryUtils;

import Zql.ZQuery;

public class QueryEngine {

   private static final Logger logger = Logger.getLogger(QueryEngine.class);

   private DataNode tree;

   /*
    * Statement object created here, so that it will not close until the
    * QueryENgine object goes out of scope or QueryEngine.Close() is called
    */
   private Statement stmt;

   // private String viewConfigFile;

   private View view;

   private final Init context = Init._this(); //serves as the context for the current QueryEngine

   //will ensure All the Stores Ontology, ontologyMap etc. are global and exist for the life cycle of the QueryEngine Object

   /*
    * public QueryEngine(String viewConfigFile) throws
    * FileNotFoundException, IOException { this.viewConfigFile =
    * viewConfigFile; init(); }
    */

   public QueryEngine(String base) throws ConfigurationException,
         FileNotFoundException, IOException {
      //TODO: Wrap as Configuration Exception
      File baseDirectory = new File(base);
      if (!baseDirectory.isDirectory()) {
         throw new ConfigurationException(
               "Configuration base  should point to a directory:" + base);
      }

      init(baseDirectory);

   }

   //public void setConfigFile(String viewConfigFile) {
   //  this.viewConfigFile = viewConfigFile;
   // }

   private void init(File baseDirectory) throws FileNotFoundException,
         IOException {
      try {

         //TODO: Wrap as Configuration Exception
         //indus.conf should be in base Directory
         File indusConfFile = new File(baseDirectory, "indus.conf");
         FileInputStream in = new FileInputStream(indusConfFile);
         Properties indusConf = new Properties();
         indusConf.load(in);

         //get the viewFileName from indus.conf, default is "view.txt"
         String viewFileName = indusConf.getProperty("view_filename",
               "view.txt");

         File viewConfigFile = new File(baseDirectory, viewFileName);

         //First Step Initialize the context
         context.init(indusConfFile, viewConfigFile);

         //It creates the representation of view including DTree
         view = context.getViewContext(); //workhorse. After this ready to execute
         tree = view.getDTree(); //get easy access to the built DTree

         String sessionId = (context.getViewConfigData().getUserViewName()
               + "_" + System.currentTimeMillis());
         //TODO: Associate this session Id with each virtual node. This will be used to create temp tables

      } catch (FileNotFoundException e) {
         logger.error("Could not finish init", e);
         throw e;
      } catch (IOException e) {
         logger.error("Could not finish init", e);
         throw e;
      }
   }

   public DataSourceDescriptor getViewDescriptor() {
      return Init._this().getUserViewDataSourceDescriptor();
   }

   public QueryResult execute(String query) throws Exception {
      ZQuery q = QueryUtils.getZQueryFromString(query);
      return this.execute(q);
   }

   public QueryResult execute(ZQuery query) throws Exception {
      Statement stmt = null;
      QueryResult qr = new QueryResult();

      if (tree.isAnswerableQuery(query)) {
         ResultSet rs = tree.execute(query, stmt);
         if (tree.getQueryType() == QueryType.COUNT_QUERY) {
            qr.setCount(BigInteger.valueOf(tree.getCount()));
         } else {
            //during processing we have moved in the resultSet while aggregating. Set it back to the start of result set
            try {
               rs.beforeFirst();
            } catch (SQLException e) {
               throw new EngineException("SQLException - " + e.getMessage(), e);
            }
            qr.setResultSet(rs);
         }
         // If it's a count query we have to do some ugly stuff in order to get a returnable
//       ResultSet object;
//       if(tree.getQueryType() == QueryType.COUNT_QUERY) {
//       DatabaseUtils.storeCountResultsLocally(tree); String countQuery =
//       "SELECT count FROM " + tree.getNodeName() + "_table"; rs =
//       DatabaseUtils.queryDataSource(tree, countQuery, stmt, false);
      } else {
         logger.debug(query + " is not answerable for this node");
         Vector<String> queryColumns = QueryUtils.getQueryColumns(query);
         Vector<String> nodeColumns = tree.getDataSourceDescriptor()
               .getAllColumnNames();

         String currQueryColumn = null;
         for (int i = 0; i < queryColumns.size(); i++) {
            currQueryColumn = queryColumns.get(i);
            if (!nodeColumns.contains(currQueryColumn)) {
               logger.debug("(userview) NOT found: " + currQueryColumn);
            } else {
               logger.debug("Found: " + currQueryColumn);
            }
         }
      }

      return qr;
   }

   public void close() {
      try {
         stmt.close();
      } catch (SQLException e) {
         logger.error("SQLException - Could not close the Statement: " + stmt,
               e);
      }
   }

   private void printOutput(QueryResult result) {
      if (tree.getQueryType() != QueryType.COUNT_QUERY) {
         ResultSet rs = result.getResultSet();
         if (rs != null) {
            display(rs, System.out);
         } else {
            logger.warn("The ResultSet is null!");
         }
      } else {
         // it is a count query
         System.out.println(" **Count Query Result ** \n");
         System.out.println(result.getCount());
      }

   }

   private static void display(ResultSet rs, PrintStream out) {
      try {
         ResultSetMetaData metaData = rs.getMetaData();
         int colCount = metaData.getColumnCount();

         String rowHeader = "";
         for (int i = 1; i <= colCount; i++) {
            rowHeader += metaData.getColumnName(i) + "\t";
         }
         out.println(rowHeader);

         String rows = "";
         if (rs.isFirst()) {
            out.println("Cursor at the beginning of the ResultSet");
         }
         /*boolean val = rs.first();

         if (!val) {
            logger
                  .debug("Could not move cursor to the beginning of the ResultSet!");
         } */
         while (rs.next()) {
            for (int i = 1; i <= colCount; i++) {
               rows += rs.getString(i) + "\t";
            }
            rows += "\n"; //Add new line
         }
         out.println(rows);
      } catch (Exception e) {
         out.println("Exception displaying this ResultSet in QueryEngine!");
      }
   }

   public static void main(String args[]) {
      //TODO  Test as a command line jar

      QueryEngine engine = null;
      if (args.length == 0) {
         System.out
               .println("check usage: An argument must be passed pointing to the baseDirectory");
      }
      String baseDir = args[0];
      try {
         engine = new QueryEngine(baseDir);
      } catch (FileNotFoundException e) {
         System.out.println("FileNotFoundException for baseDir: " + baseDir);
      } catch (IOException e) {
         System.out.println("IOException for the baseDir: " + baseDir);
      }

      BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
      String input = "";
      while (true) {

         System.out.print("Enter query to execute or q to quit. \n");
         System.out.println("indus>");

         try {
            input = in.readLine();
         } catch (IOException e) {
            System.out.println("Error Reading Input");
            e.printStackTrace();
         }

         if ("q".equalsIgnoreCase(input)) {

            break;
         } else {
            try {
               QueryResult result = engine.execute(input);
               engine.printOutput(result);
            } catch (Exception e) {
               System.out.println(e.getMessage());
               e.printStackTrace();
            }
         }
      }
   }
}
