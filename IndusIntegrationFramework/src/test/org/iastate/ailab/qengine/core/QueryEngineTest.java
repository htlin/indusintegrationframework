package org.iastate.ailab.qengine.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

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
      //Logger.getRootLogger().setLevel(Level.DEBUG);
      Logger.getRootLogger().setLevel(Level.TRACE);
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

   @Test
   public void testCount() {
      System.out.println("testCount");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where position > 'redshirt';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      int count = result.getCount().intValue();
      if (count < 1) {
         Assert.fail("Count is less than 1");
      }

      System.out.println("Count is: " + count);
   }

   @Test
   public void testCountWildCard() {
      System.out.println("testCount Wild Card");

      String query = "select COUNT(*) from EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      int count = result.getCount().intValue();
      if (count < 1) {
         Assert.fail("Count is less than 1");
      }

      System.out.println("Count is: " + count);
   }

   @Test
   public void testCountWildCardSinglePath() {
      System.out.println("testCount Wild Card");

      String query = "select COUNT(*) from EMPLOYEETABLE where position > 'redshirt';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      int count = result.getCount().intValue();
      if (count < 1) {
         Assert.fail("Count is less than 1");
      }

      System.out.println("Count is: " + count);
   }

   @Test
   public void testSuperClass() throws SQLException {
      System.out.println("testSuperClass");

      String query = "select firstname from EMPLOYEETABLE where position > 'redshirt';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 1) {
         Assert.fail("Column Count is not 1");
      }

      display(rs, query);
   }

   @Test
   public void testSubClass() throws SQLException {
      System.out.println("testSubClass");

      //tests subclass, currently data only comes from one of the data sources
      String query = "select firstname, position from EMPLOYEETABLE where position <'undergraduate';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 2) {
         Assert.fail("Column Count is not 2");
      }

      display(rs, query);
   }

   @Test
   public void testIsClass() throws SQLException {
      System.out.println("testIsClass");

      //IS-A is implemented by = and < operators as below
      String query = "select firstname, position from EMPLOYEETABLE where position='undergraduate' OR position < 'undergraduate';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 2) {
         Assert.fail("Column Count is not 2");
      }

      display(rs, query);
   }

   @Test
   public void testFixMe() throws SQLException {
      System.out.println("testFixMe");

      String query = "select key2,firstname, position from EMPLOYEETABLE;";
      System.out.println("Query: " + query);
      //In DataAggregation we are always removing join Column even if it is part of the query. We keep track of\
      //if we added the joinColumn for handling vertical fragmentation and then only remove it

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 3) {
         Assert.fail("Column Count is not 3");
      }

      display(rs, query);
   }

   @Test
   public void testToDO() {
      System.out.println("testToDO");

      //#Currently freely hanging nodes have some trouble. Handle Cases where there is no subclass for a node
      // NULL POINTER EXCEPTION in Reasoner IF YOU SPELL THE DataContentValue Wrong
      //" select firstname, position from EMPLOYEETABLE where position <'underGGraduate';";
      Assert.fail("Test not implemented!");
   }

   @Test
   public void testJoinColumn() throws SQLException {
      System.out.println("testJoinColumn");

      //String query ="select key2,firstname, position from EMPLOYEETABLE;";
      String query = "select key2 from EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 1) {
         Assert.fail("Column Count is not 1");
      }

      display(rs, query);
   }

   /**
    * The two columns being selected are vertically fragmented
    * 
    * @throws SQLException
    */
   @Test
   public void testNoWhereClause() throws SQLException {
      System.out.println("testNoWhereClause");

      String query = "select firstname, position from EMPLOYEETABLE;";
      // String query = "select firstname, position from EMPLOYEETABLE where position <'undergraduate';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      int count = rs.getMetaData().getColumnCount();
      if (count != 2) {
         Assert.fail("Column Count is not 2");
      }

      display(rs, query);
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
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery02() {
      System.out.println("testQuery2");

      String query = "SELECT firstname,benefits FROM EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery03() {
      System.out.println("testQuery3");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' OR  firstname='neeraj';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery04() {
      System.out.println("testQuery4");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' OR  firstname='neeraj' AND key2 > 45;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery05() {
      System.out.println("testQuery5");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' AND  firstname='neeraj' AND social > 45;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery06() {
      System.out.println("testQuery6");

      String query = "SELECT firstname  FROM EMPLOYEETABLE where position='grad' AND  firstname='neeraj' AND key2 > 45;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery07() {
      System.out.println("testQuery7");

      String query = "Select position   FROM EMPLOYEETABLE where  benefits > 4000;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery08() {
      System.out.println("testQuery8");

      String query = "select firstname, position, benefits from EMPLOYEETABLE where benefits > 4000;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery09() {
      System.out.println("testQuery9");

      String query = "select * from EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery11() {
      System.out.println("testQuery11s");

      String query = "select firstname from EMPLOYEETABLE where (position='manager' OR firstname='Steve') OR timehere > 3;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery12() {
      System.out.println("testQuery12");

      String query = "SELECT firstname, position  FROM EMPLOYEETABLE where position='manager' OR firstname='Steve' AND key2 > 45;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery13() {
      System.out.println("testQuery13");

      String query = "select * from EMPLOYEETABLE where (position >'jun' AND timehere > 1) OR (firstname='Steve' AND key2 > 45);";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery14() {
      System.out.println("testQuery14");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where benefits > 4000 or position='manager';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery15() {
      System.out.println("testQuery15");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where firstname='John' OR firstname='Neeraj' OR firstname='scout';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      int count = result.getCount().intValue();
      //TODO should this count be verified?

      System.out.println("Count is: " + count);
   }

   @Test
   public void testQuery16() {
      System.out.println("testQuery16");

      String query = "select COUNT(firstname) from EMPLOYEETABLE where (select firstname, position from EMPLOYEETABLE where (position='manager' AND timehere > 1) OR (firstname='Steve' AND key > 45)) > 2;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery17() {
      System.out.println("testQuery17");

      String query = "select firstname from EMPLOYEETABLE where firstname='Steve';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery18() {
      System.out.println("testQuery18");

      String query = "select * from EMPLOYEETABLE where (position >'redshirt' AND timehere > 1) OR (firstname='Steve' AND key2 > 45);";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery19() {
      System.out.println("testQuery19");

      String query = "SELECT firstname  FROM EMPLOYEETABLE WHERE  IN ('Manager', 'Staff') OR status > 'grad';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery21() {
      System.out.println("testQuery21");

      String query = "select key2 from EMPLOYEETABLE where firstname='raj' OR firstname='remy';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery22() {
      System.out.println("testQuery22");

      String query = "select position from EMPLOYEETABLE where firstname='raj' OR firstname='remy';";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
   }

   @Test
   public void testQuery24() {
      System.out.println("testQuery24");

      String query = "select firstname, position from EMPLOYEETABLE;";
      System.out.println("Query: " + query);

      QueryResult result = engine.execute(query);
      Assert.assertNotNull(result);

      ResultSet rs = result.getResultSet();
      Assert.assertNotNull(rs); //should get some result

      display(rs, query);
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
