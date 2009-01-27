package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

public class DefaultOntologyImpl implements OntologyI {

   /* The URI used to identify the Ontology */
   private URI ontologyId;

   /* The axioms in the ontology */
   private Set<Axiom> axioms = new HashSet<Axiom>();

   /**
    * Represents the classes that form the ontology
    */
   private Set<URI> classes = new HashSet<URI>();

   /**
    * Store properties associated with the ontology. e.g. description
    */
   private Map<String, String> properties = new HashMap<String, String>();

   public URI getOntologyURI() {
      return ontologyId;
   }

   public Set<Axiom> getAxioms() {
      return axioms;
   }

   public void addAxiom(Axiom axiom) {
      if (!axioms.contains(axiom)) { //do we need the check as adding to HashSet
         axioms.add(axiom);
      }
   }

   public void addClass(URI classId) {
      if (!classes.contains(classId)) { //do we need the check as adding to HashSet
         classes.add(classId);
      }
   }

   public void setOntologyURI(URI id) {
      this.ontologyId = id;
   }

   public boolean containsClass(URI classId) {
      return classes.contains(classId);
   }

   public void setOntologyProperty(String key, String value) {
      properties.put(key, value);
   }

   public String getOntologyProperty(String key) {
      return properties.get(key);
   }

   public Set<URI> getAllConcepts() {
      return classes;
   }

   @Override
   public String toString() {
      StringWriter stringWriter = new StringWriter();
      display(stringWriter);
      return stringWriter.toString();
   }

   public void display(OutputStream out) {
      display(new PrintWriter(out));
   }

   public void display(Writer writer) {
      display(new PrintWriter(writer));
   }

   public void display(PrintWriter printWriter) {
      printWriter.println("ontologyID=" + getOntologyURI());

      //display classes/concepts
      printWriter.println("Concepts:");
      Iterator<URI> it = classes.iterator();
      while (it.hasNext()) {
         printWriter.println("\t" + it.next());
      }

      //display axioms
      Iterator<Axiom> axIt = axioms.iterator();
      printWriter.println("Axioms");
      while (axIt.hasNext()) {
         axIt.next().display(printWriter);
      }

      //display properties if exist
      if (properties.size() > 0) {
         printWriter.println("Properties:");
         Iterator<String> strIt = properties.keySet().iterator();
         String key = null;
         String value = null;
         while (strIt.hasNext()) {
            key = strIt.next();
            value = properties.get(key);
         }
      }
   }
}
