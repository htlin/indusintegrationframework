package org.iastate.ailab.qengine.core.util;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import Zql.ParseException;
import Zql.ZQuery;
import Zql.ZqlParser;

public class QueryUtilsTest {

   private static int testNum = 0;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      System.out
            .println("--------------------Starting JUnit test QueryUtilsTest--------------------");
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      System.out
            .println("--------------------Finished JUnit test QueryUtilsTest--------------------");
   }

   @Before
   public void setUp() throws Exception {
      System.out.print("********************Starting test number " + ++testNum
            + "********************");
   }

   @After
   public void tearDown() throws Exception {
      System.out.println("********************Finished test number " + testNum
            + "********************");
   }

   @Test
   public void testGetWhereColumns() throws ParseException {
      System.out.println("testGetWhereColumns");

      // String input = "select firstname, position from EMPLOYEETABLE where (position='manager' AND timehere > 1) OR (firstname='Steve' AND key > 45);";
      // String input = "Select COUNT(*) FROM votes_train WHERE Class='democrat'   AND handicapped-infants='n';";
      String input = "select COUNT(*) from EMPLOYEETABLE where position2 > 'redshirt2' AND  neeraj='koul';";
      ZqlParser parser = new ZqlParser();
      ByteArrayInputStream inpStream = new ByteArrayInputStream(input
            .getBytes());
      parser.initParser(inpStream);
      ZQuery query = (ZQuery) parser.readStatements().get(0);

      Vector<String> whereColumns = QueryUtils.getWhereColumns(query);

      System.out.println("******** Where Columns ***********");
      for (int i = 0; i < whereColumns.size(); i++) {
         System.out.println(whereColumns.get(i));
      }
      System.out.println("**********************************");
   }

   @Test
   public void testParsingWhereColumnsCountQuery() throws ParseException {
      System.out.println("testGetWhereColumns");
      /*
       * ZQuery has error parsing columns with quote charcters arounds
       * columns. e.g Select COUNT() FROM votes_train WHERE
       * Class='democrat' AND handicapped-infants='n'; AND
       * `handicapped-infants`='n';"; Removing the back quote is causing
       * theno. of columns clculation wrong
       */

      String input = "Select COUNT(*) FROM votes_train WHERE Class='democrat'   AND handicapped-infants='n';";

      ZqlParser parser = new ZqlParser();
      ByteArrayInputStream inpStream = new ByteArrayInputStream(input
            .getBytes());
      parser.initParser(inpStream);
      ZQuery query = (ZQuery) parser.readStatements().get(0);

      Vector<String> whereColumns = QueryUtils.getWhereColumns(query);

      System.out.println("no of columns=" + whereColumns.size());
      System.out.println("******** Where Columns ***********");
      for (int i = 0; i < whereColumns.size(); i++) {
         System.out.println(whereColumns.get(i));
      }
      System.out.println("**********************************");
   }

}
