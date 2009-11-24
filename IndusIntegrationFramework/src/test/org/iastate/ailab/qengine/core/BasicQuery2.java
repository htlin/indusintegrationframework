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
 * 
 * @author RR
 * 
 */
public class BasicQuery2 {
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
       * File.separator + "config-example-4" + File.separator + "view.txt";
       */

      String baseDir = System.getProperty("user.dir") + File.separator
            + "config-example-4";

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

      //String query = "select COUNT(moviename) from MOVIE_DETAILS where descriptor > 'spoofs';";
      String query = "select COUNT(moviename) from MOVIE_DETAILS where descriptor < 'action';";
      System.out.println("Query: " + query);

      QueryResult result;
      try {
         result = engine.execute(query);
         Assert.assertNotNull(result);
         int count = result.getCount().intValue();
         if (count < 1) {
            Assert.fail("Count is less than 1");
         }
         System.out.println("Count is: " + count);
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
