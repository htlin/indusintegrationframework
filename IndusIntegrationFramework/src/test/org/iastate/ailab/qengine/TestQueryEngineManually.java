package org.iastate.ailab.qengine;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import org.apache.log4j.BasicConfigurator;
import org.iastate.ailab.qengine.core.QueryEngine;

import Zql.ParseException;
import Zql.ZQuery;
import Zql.ZqlParser;

public class TestQueryEngineManually {

   static {
      BasicConfigurator.configure();
   }

   private static QueryEngine engine;

   public static void main(String[] args) throws IOException {

      Scanner stdin = new Scanner(System.in);

      createEngine();

      while (true) {
         System.out.println("Enter Query (type 'quit' to exit):");
         String curLine = stdin.nextLine();

         if (curLine.equals("quit")) {
            System.out.println("Exiting");
            break;
         }

         try {
            run(curLine);
            System.out.println("**********DONE***************");
         } catch (ParseException e) {
            System.out.println("The query is not correct SQL synatx!");
            System.out.println(e.getMessage());
         }
         System.out.println();
      }
   }

   private static void createEngine() throws IOException {
      String configFile = System.getProperty("user.dir")
            + System.getProperty("file.separator") + "config-example-1"
            + System.getProperty("file.separator") + "view.txt";
      try {
         engine = new QueryEngine(configFile);
      } catch (FileNotFoundException e) {
         System.out.println("FileNotFoundException for the user view File: "
               + configFile);
         throw e;
      } catch (IOException e) {
         System.out
               .println("IOException for the user view File: " + configFile);
         throw e;
      }
   }

   private static void run(String input) throws ParseException {

      ZqlParser parser = new ZqlParser();
      ByteArrayInputStream inpStream = new ByteArrayInputStream(input
            .getBytes());
      parser.initParser(inpStream);

      ZQuery query = (ZQuery) parser.readStatements().get(0);
      
      System.out.println("Parsed query: " + query);

      engine.execute(query);
   }
}
