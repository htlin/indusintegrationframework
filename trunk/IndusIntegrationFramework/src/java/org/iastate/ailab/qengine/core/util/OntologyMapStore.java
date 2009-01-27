package org.iastate.ailab.qengine.core.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import org.iastate.ailab.qengine.core.exceptions.EngineException;
import org.iastate.ailab.qengine.core.reasoners.impl.AVHRole;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;

@Deprecated
public interface OntologyMapStore {
   //TODO: Deprecated, Remove
   /**
    * @param ontContent Location to the file that contains the ontology. If
    * <param>inline</param> is true then it contains ontology
    * @param inline if this true <param> <ontContent </param> itself
    * contains the ontology
    */
   public void parse(String ontContent, boolean inline);

   public void addNameSpace(String ontologyAbbreviation, String ontologyFullName);

   public String getNameSpace(String ontologyAbbreviation);

   public String getAbbreviation(String ontologyFullName);

   public HashMap<String, HashSet<Axiom>> getAxioms(String dataSource);

   public AVHRole getAVHRoleFromString(String r) throws EngineException;

   /**
    * prints all the axioms for the dataSource to the passed stream
    * 
    * @param out
    * @param dataSource
    * @param namespace the namespace for which to display axioms in the
    * dataSource if nameSpace is passed as null, display all axioms for all
    * namespaces in the dataSource
    */
   public void display(PrintStream out, String dataSource, String namespace);
}
