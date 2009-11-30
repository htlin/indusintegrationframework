package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.reasoners.impl.MappedOntologyGraphs.MappedOntologyGraphsForDataSource;
import org.iastate.ailab.qengine.core.reasoners.interfaces.Axiom;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.model.OWLClass;

import Zql.ZConstant;
import Zql.ZExpression;

/**
 * @author neeraj
 * Modified by Roshan, Rohit 
 */
public class DefaultReasonerImpl implements Reasoner {

   private static final Logger logger = Logger
         .getLogger(DefaultReasonerImpl.class);

   OntologyMapStore mapStore = Init._this().getViewContext()
         .getOntologyMapStore();

   OntologyStore ontStore = Init._this().getViewContext().getOntologyStore();

   // Store the built MEOG(Mapping Extended Ontology Graph) here as 
   //Init Caches the Reasoners and hence those will be cached too
   MappedOntologyGraphs mappedGraphs = MappedOntologyGraphs._this();

   public Set<URI> getEquivalentClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource) {

      Set<Axiom> mappingAxioms = mapStore.getMappings(userViewOntologyURI,
            targetDataSource);

      //TODO: We can potentially optimize equivalent class by looking at mapping axioms
      // We have to return all the ToNodes where the fromNode corresponds to classID and AVHRole is equivalenceClass
      java.util.Iterator<Axiom> it = mappingAxioms.iterator();
      Axiom currAxiom = null;
      Set<URI> result = new HashSet<URI>();
      while (it.hasNext()) {
         currAxiom = it.next();
         if (currAxiom.getRole().equals(AVHRole.EQUIVALENT_CLASS)
               && currAxiom.getFromNode().equals(classID)) {
            result.add(currAxiom.getToNode());
         }
      }

      return result;
   }

   public Set<URI> getSubClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource) {
      Set<Axiom> userViewAxioms = ontStore.getOntology(userViewOntologyURI)
            .getAxioms();

      Set<Axiom> dataSourceAxioms = ontStore.getOntology(dataSourceOntologyURI)
            .getAxioms();
      int size = dataSourceAxioms.size();
      logger.trace("size=" + size);
      Set<Axiom> mappingAxioms = mapStore.getMappings(userViewOntologyURI,
            targetDataSource);

      //build the OntologyMappedGraphs. The build is smart enought to figure if it has not done this earlier
      mappedGraphs.build(userViewAxioms, dataSourceAxioms, mappingAxioms,
            targetDataSource, userViewOntologyURI, dataSourceOntologyURI);
      MappedOntologyGraphsForDataSource mappedgraphDataSource = mappedGraphs
            .getMappedOntologyGraphsForDataSource(targetDataSource);

      //construct the identifier which allows us to get the relevant graph for the mapping under consideration
      String mappingIdentifier = userViewOntologyURI + "->"
            + dataSourceOntologyURI;

      return mappedgraphDataSource.getMappedOntologyGraph(mappingIdentifier)
            .getSubClass(classID);
   }

   public Set<URI> getSuperClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource) {
      Set<Axiom> userViewAxioms = ontStore.getOntology(userViewOntologyURI)
            .getAxioms();

      Set<Axiom> dataSourceAxioms = ontStore.getOntology(dataSourceOntologyURI)
            .getAxioms();
      int size = dataSourceAxioms.size();
      logger.trace("size=" + size);
      Set<Axiom> mappingAxioms = mapStore.getMappings(userViewOntologyURI,
            targetDataSource);

      //build the OntologyMappedGraphs. The build is smart enought to figure if it has not done this earlier
      mappedGraphs.build(userViewAxioms, dataSourceAxioms, mappingAxioms,
            targetDataSource, userViewOntologyURI, dataSourceOntologyURI);

      MappedOntologyGraphsForDataSource mappedgraphDataSource = mappedGraphs
            .getMappedOntologyGraphsForDataSource(targetDataSource);

      //construct the identifier which allows us to get the relevant graph for the mapping under consideration
      String mappingIdentifier = userViewOntologyURI + "->"
            + dataSourceOntologyURI;

      return mappedgraphDataSource.getMappedOntologyGraph(mappingIdentifier)
            .getSuperClass(classID);
   }

   public ZExpression getAVHClass(URI userViewOntologyURI, URI classID,
         URI dataSourceOntologyURI, String targetDataSource, String AVHRole,
         ZExpression inp) {
     // Ontmap uri, String targetDataSource, String AVHRole, ZExpression inp
      Set<URI> result;
      if (AVHRole.equals(">")) {
         result = getSuperClass(userViewOntologyURI, classID,
               dataSourceOntologyURI, targetDataSource);
      } else if (AVHRole.equals("<")) {
         result = getSubClass(userViewOntologyURI, classID,
               dataSourceOntologyURI, targetDataSource);
      } else if (AVHRole.equals("=")) {
         result = getEquivalentClass(userViewOntologyURI, classID,
               dataSourceOntologyURI, targetDataSource);
      } else {
         // Will not Happen
         logger.warn("UNKNOWN AVH ROLE=" + AVHRole);
         return inp;
      }

      ZConstant value = null;
      String temp;
      if (result.size() > 0) {
         java.util.Iterator<URI> it = result.iterator();
         while (it.hasNext()) {
            temp = URI2DataContent(it.next());
            value = new ZConstant(temp, ZConstant.STRING);
            inp.addOperand(value); //It Already contains operator IN and the columnName
         }
      } else {
         //no Sub/Super/EquivalentClass exist (e.g. the case when there are no mappings)
         //temporary solution is fill it with a value which does not exist. In Future you can optimize it
         logger.trace("NOTHING FOR: " + classID + " " + AVHRole
               + "  in data source: " + targetDataSource);
         value = new ZConstant("XX_NOT_FOUND_XX", ZConstant.STRING);
         inp.addOperand(value);
      }
      //TODO: Handle the case when the result is null, temporary solution is to
      return inp;
   }

   /**
    * Converts a URI to a value as stored in the database
    * 
    * @param inp
    * @return
    */
   private static String URI2DataContent(URI inp) {
      // converts www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH/doct --> doct
      //converts www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH#doct --> doct
      //converts indus:doct --> doct
      String result = inp.toString();
      //Look for the last occurence of '#','/', ':' in that order and return the substring after it
      int index = result.lastIndexOf("#");
      if (index == -1) {
         index = result.lastIndexOf("/");
      }
      if (index == -1) {
         index = result.lastIndexOf(":");
      }
      return result.substring(index + 1); //the Passed param is URI so index will not be -1
   }

 
   /* Modified by Roshan, Rohit
   * Dummy implementation of pellet specific methods added to Reasoner interface.
   * PelletReasonerImpl class has actual implementation.
   */
   
   @Override
   public Set<URI> getEquivalentClass(OWLReasoner reasoner, OWLClass owlClassObject) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<URI> getSubClass(OWLReasoner reasoner, OWLClass owlClassObject) {
      // TODO Auto-generated method stub
      return null;
   }

   @Override
   public Set<URI> getSuperClass(OWLReasoner reasoner, OWLClass owlClassObject) {
      // TODO Auto-generated method stub
      return null;
   }
}
