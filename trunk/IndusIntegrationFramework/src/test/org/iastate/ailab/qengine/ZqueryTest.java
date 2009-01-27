package org.iastate.ailab.qengine;

import java.util.Vector;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import Zql.ParseException;
import Zql.ZConstant;
import Zql.ZExp;
import Zql.ZExpression;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;
import Zql.ZStatement;
import Zql.ZqlParser;

public class ZqueryTest {

   public static void printSelect(ZSelectItem select, String indent) {
   }

   public static void printFrom(ZFromItem from, String indent) {
   }

   public static void printZExpression(ZExpression expression, String indent) {

      indent += "\t";
      String operator = expression.getOperator();
      System.out.println(indent + operator);
      Vector<ZExp> operands = expression.getOperands();
      for (int i = 0; i < operands.size(); i++) {
         ZExp currOperand = operands.get(i);

         if (currOperand instanceof ZConstant) {
            System.out.println(indent + ((ZConstant) currOperand).getValue());

         } else if (currOperand instanceof ZExpression) {
            printZExpression((ZExpression) currOperand, indent);

         } else if (currOperand instanceof ZQuery) {
            printZQuery((ZQuery) currOperand, indent);
         }
      }
   }

   public static void printZQuery(ZQuery query, String indent) {
      Vector<ZSelectItem> select = query.getSelect();

      for (int i = 0; i < select.size(); i++) {
         ZSelectItem selectItem = select.get(i);
         System.out.println("Printing Select-->" + i);
         printSelect(selectItem, indent);
      }

      Vector<ZFromItem> from = query.getFrom();
      for (int i = 0; i < from.size(); i++) {
         ZFromItem fromItem = from.get(i);
         System.out.println("Printing FROM-->" + i);
         printFrom(fromItem, indent);
      }
      ZExpression expression = (ZExpression) query.getWhere();
      printZExpression(expression, indent);
   }

   public static void printZStatements(Vector<ZStatement> Zstatements) {
      for (int i = 0; i < Zstatements.size(); i++) {
         printZStatement(Zstatements.get(i));
      }
   }

   public static void printZStatement(ZStatement stmt) {
      String indent = "\t";
      String name = stmt.getClass().getCanonicalName();
      System.out.println("ZStatement name=" + name);
      if (stmt instanceof ZQuery) {
         //TO BEGIN WITH EVERYTHING IS A ZQUERY
         printZQuery((ZQuery) stmt, indent);
      } else if (stmt instanceof ZExpression) {
         //TODO is it correct to do nothing for this case?
         //if so, add a comment explaining why
      } else if (stmt instanceof ZConstant) {
         ZConstant cons = ((ZConstant) stmt);
         System.out.println("TYPE= " + cons.getType() + " VALUE="
               + cons.getValue());
      } else {
         System.out.println("DON'T KNOW");
      }
   }

   public static void main(String[] args) throws ParseException, IOException {

      String query1 = "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE "
            + " WHERE SALARY > 40000 AND POSITION < SOME_OTHER_COLUMN AND NAME='Neeraj';";

      String query2 = "Select name,SSN from nameTable,statTable where "
            + " status ='grad' AND year > 2005;";

      String query3 = "Select name,SSN from nameTable,statTable where "
            + "status IN (Select status from gradTable where status > masters) and position='RA';";

      ZqlParser parser = new ZqlParser();
      ByteArrayInputStream inpStream = new ByteArrayInputStream(query1
            .getBytes());
      parser.initParser(inpStream);
      printZStatements(parser.readStatements());

      inpStream.close();
      inpStream = new ByteArrayInputStream(query2.getBytes());
      parser.initParser(inpStream);
      printZStatements(parser.readStatements());

      inpStream.close();
      inpStream = new ByteArrayInputStream(query3.getBytes());
      parser.initParser(inpStream);
      printZStatements(parser.readStatements());
   }
}
