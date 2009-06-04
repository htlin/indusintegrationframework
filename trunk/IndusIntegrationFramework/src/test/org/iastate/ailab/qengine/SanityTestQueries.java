package org.iastate.ailab.qengine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.QueryEngine;
import org.iastate.ailab.qengine.core.QueryResult;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 * @author neeraj
 * 
 */
public class SanityTestQueries {
   /**
    * After changes these tests should always be run in addition to
    * SanityTestQueries
    */
   static {
      BasicConfigurator.configure();
      Logger.getRootLogger().setLevel(Level.DEBUG);
      //Logger.getRootLogger().setLevel(Level.TRACE);
   }

   private static int testNum = 0;

   private QueryEngine engine = null;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      System.out
            .println("--------------------Starting JUnit test QueryEngineTest--------------------");
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      System.out
            .println("--------------------Finished JUnit test QueryEngineTest--------------------");
   }

   @Before
   public void setUp() throws IOException {
      System.out.print("********************Starting test number " + ++testNum
            + "********************");
      /*
       * String configFile = System.getProperty("user.dir") +
       * File.separator + "config-example-1" + File.separator + "view.txt";
       */

      String baseDir = System.getProperty("user.dir") + File.separator
            + "config-example-2";

      try {
         engine = new QueryEngine(baseDir);
      } catch (FileNotFoundException e) {
         System.out.println("FileNotFoundException for baseDir: " + baseDir);
         throw e;
      } catch (IOException e) {
         System.out.println("IOException for the baseDir: " + baseDir);
         throw e;
      }
   }

   @After
   public void tearDown() {
      System.out.println("********************Finished test number " + testNum
            + "********************");
   }

   /*
    * TODO - The tests below have been extracted from the class Main.
    * Consider renaming these tests and adding more code to automatically
    * verify correctness.
    */

   @Test
   public void testQuery01() {
      System.out.println("testQuery1");

      //String query = "SELECT position  FROM EMPLOYEETABLE  WHERE benefits > 1400;";

      String query = "SELECT position  FROM EMPLOYEETABLE  WHERE timehere > 0;";
      query = "select firstname from EMPLOYEETABLE where position > 'redshirt';";

      System.out.println("Query: " + query);
      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result

         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery02() {
      System.out.println("testQuery2");

      String query = "SELECT firstname,benefits FROM EMPLOYEETABLE;";
      System.out.println("Query: " + query);
      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result

         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery06() {
      System.out.println("testQuery6");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' AND  firstname='neeraj' AND key2 > 45;";
      System.out.println("Query: " + query);
      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result

         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery07() {
      System.out.println("testQuery7");

      String query = "Select position   FROM EMPLOYEETABLE where  benefits > 4000;";
      System.out.println("Query: " + query);
      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result
         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery08() {
      System.out.println("testQuery8");

      String query = "select firstname, position, benefits from EMPLOYEETABLE where benefits > 4000;";
      System.out.println("Query: " + query);
      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result
         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }

   }

   @Test
   public void testQuery15() {
      System.out.println("testQuery15");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where firstname='John' OR firstname='Neeraj' OR firstname='scout';";
      System.out.println("Query: " + query);

      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         int count = result.getCount().intValue();
         //TODO should this count be verified?

         System.out.println("Count is: " + count);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery22() {
      System.out.println("testQuery22");

      String query = "select position from EMPLOYEETABLE where firstname='raj' OR firstname='remy';";
      System.out.println("Query: " + query);

      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result

         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   @Test
   public void testQuery24() {
      System.out.println("testQuery24");

      String query = "select firstname, position from EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      try {
         QueryResult result = engine.execute(query);
         Assert.assertNotNull(result);

         ResultSet rs = result.getResultSet();
         Assert.assertNotNull(rs); //should get some result

         display(rs, query);
      } catch (Exception e) {
         Assert.fail(e.getMessage());
         e.printStackTrace();
      }
   }

   private static void display(ResultSet rs, String query) {
      try {
         ResultSetMetaData metaData = rs.getMetaData();
         int colCount = metaData.getColumnCount();
         String rowHeader = "";
         for (int i = 1; i <= colCount; i++) {
            rowHeader += metaData.getColumnName(i) + "\t\t";
         }
         System.out.println("Displaying Results for query: " + query);
         System.out
               .println("======================TABLE HEADER==========================================");
         System.out.println(rowHeader);
         System.out
               .println("=============================================================================");
         String rows = "";
         rs.beforeFirst();
         while (rs.next()) {
            for (int i = 1; i <= colCount; i++) {
               rows += rs.getString(i) + "\t\t";
            }
            rows += "\n"; //Add new line
         }

         System.out.println(rows);
         System.out
               .println("=============================================================================");

      } catch (Exception e) {
         System.out.println("Exception displaying result set!");
         throw new RuntimeException(e.getMessage(), e);
      }
   }
}
