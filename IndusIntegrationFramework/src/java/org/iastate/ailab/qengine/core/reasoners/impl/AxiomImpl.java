package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

/**
 * This Class Represents an implementation of DataContent
 * 
 * @author neeraj
 */

public class AxiomImpl implements Axiom {

   private static final Logger logger = Logger.getLogger(AxiomImpl.class);

   //from --RelationShip--> to
   // GradStudent SubClassOf Student 
   //from correspondsTo GradStudent, to correspondsTo Student, 
   // relationShip correspondsTo  SubClassOf

   private URI from;

   private URI to;

   private AVHRole relationShip; //use Interface

   public AxiomImpl(URI f, URI t, AVHRole r) {
      this.from = f;
      this.to = t;
      this.relationShip = r;
   }

   public URI getFromNode() {
      return from;
   }

   public URI getToNode() {
      return to;
   }

   public AVHRole getRole() {
      return relationShip;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == null) {
         return false;
      }
      if (!(obj instanceof Axiom)) {
         return false;
      }
      Axiom other = (Axiom) obj;
      return other.getFromNode().equals(from) && other.getToNode().equals(to)
            && other.getRole().equals(relationShip)
            || other.getFromNode().equals(to) && other.getToNode().equals(from)
            && other.getRole().equals(relationShip);
   }

   @Override
   public String toString() {
      StringWriter stringWriter = new StringWriter();
      display(stringWriter);
      return stringWriter.toString();
   }

   /**
    * displays the axiom in user readable format to the passed Stream
    */
   public void display(OutputStream out) {
      display(new PrintWriter(out));
   }

   public void display(Writer writer) {
      display(new PrintWriter(writer));
   }

   public void display(PrintWriter printWriter) {
      try {
         printWriter.println(from + "-> " + relationShip + "-> " + to);
      } catch (Exception e) {
         logger.error("Exception displaying an Axiom", e);
      }
   }
}
