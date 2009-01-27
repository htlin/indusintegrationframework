package org.iastate.ailab.qengine;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestConnection {

   public static void main(String[] args) throws Exception {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      String url = "jdbc:mysql://" + "mccarthy.cs.iastate.edu" + "/" + "indus";
      System.out.println("Connection attempt with url " + url);

      Connection c = DriverManager.getConnection(url, "indus", "indus");
      System.out.println("Connection: " + c);
   }
}
