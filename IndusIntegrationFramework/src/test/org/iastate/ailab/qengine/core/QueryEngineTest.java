package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import junit.framework.Assert;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author neeraj
 */
public class QueryEngineTest {

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
    * Currently They require fixing Consider renaming these tests and
    * adding more code to automatically verify correctness.
    */

   @Test
   public void testQuery03() {
      System.out.println("testQuery3");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' OR  firstname='neeraj';";
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
   public void testQuery04() {
      System.out.println("testQuery4");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' OR  firstname='neeraj' AND key2 > 45;";
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
   public void testQuery05() {
      System.out.println("testQuery5");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' AND  firstname='neeraj' AND social > 45;";
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
   public void testQuery09() {
      System.out.println("testQuery9");

      String query = "select * from EMPLOYEETABLE;";
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
   public void testQuery11() {
      System.out.println("testQuery11s");

      String query = "select firstname from EMPLOYEETABLE where (position='manager' OR firstname='Steve') OR timehere > 3;";
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
   public void testQuery12() {
      System.out.println("testQuery12");

      String query = "SELECT firstname, position  FROM EMPLOYEETABLE where position='manager' OR firstname='Steve' AND key2 > 45;";
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
   public void testQuery13() {
      System.out.println("testQuery13");

      String query = "select * from EMPLOYEETABLE where (position >'jun' AND timehere > 1) OR (firstname='Steve' AND key2 > 45);";
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
   public void testQuery14() {
      System.out.println("testQuery14");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where benefits > 4000 or position='manager';";
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
   public void testQuery16() {
      System.out.println("testQuery16");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where (select firstname, position from EMPLOYEETABLE where (position='manager' AND timehere > 1) OR (firstname='Steve' AND key > 45)) > 2;";
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
   public void testQuery17() {
      System.out.println("testQuery17");

      String query = "select firstname from EMPLOYEETABLE where firstname='Steve';";
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
   public void testQuery18() {
      System.out.println("testQuery18");

      String query = "select * from EMPLOYEETABLE where (position >'redshirt' AND timehere > 1) OR (firstname='Steve' AND key2 > 45);";
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
   public void testQuery19() {
      System.out.println("testQuery19");

      String query = "SELECT firstname  FROM EMPLOYEETABLE WHERE  IN ('Manager', 'Staff') OR status > 'grad';";
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
   public void testQuery21() {
      System.out.println("testQuery21");

      String query = "select key2 from EMPLOYEETABLE where firstname='raj' OR firstname='remy';";
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
