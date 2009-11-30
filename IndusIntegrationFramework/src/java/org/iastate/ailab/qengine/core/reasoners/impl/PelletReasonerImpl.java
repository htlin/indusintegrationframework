package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.IndusConfiguration;
import org.iastate.ailab.qengine.core.Init;

import Zql.ZConstant;
import Zql.ZExpression;


//new
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.inference.OWLReasoner;
import org.semanticweb.owl.inference.OWLReasonerAdapter;
import org.semanticweb.owl.inference.OWLReasonerException;
import org.semanticweb.owl.inference.OWLReasonerFactory;
import org.semanticweb.owl.model.*;
import org.semanticweb.reasonerfactory.pellet.PelletReasonerFactory;

// new ends
/**
 * @author Roshan, Rohit
 * This class has all the methods to call Pellet Reasoner API to
 * find super, sub, equivalent classes.
 */
public class PelletReasonerImpl extends DefaultReasonerImpl {

   private static final Logger logger = Logger
         .getLogger(DefaultReasonerImpl.class);
   
   
 /* OntologyMapStore is not required incase of Pellet Reasoner as the reasoner
  * can directly raed the ont map files in owl format
  */
   
  /* OntologyMapStore mapStore = Init._this().getViewContext()
         .getOntologyMapStore();

   OntologyStore ontStore = Init._this().getViewContext().getOntologyStore(); */


   @Override
   public Set<URI> getEquivalentClass(OWLReasoner reasoner, OWLClass owlClassObject) {
      IndusConfiguration indusConfig = Init._this().getIndusConfiguration();
      String equivalentFlag = indusConfig.getEquivalentFlag();
      System.out.println("equivalentFlag: " + equivalentFlag);
      Set<URI> result = new HashSet<URI>();
      Set<OWLClass> equiClsSets;
      try {
         equiClsSets = reasoner.getEquivalentClasses(owlClassObject);
      for(OWLClass ols : equiClsSets)
      {                 
            //System.out.println(ols.getURI());
            result.add(ols.getURI());
      }
      
      // If equivalent_flag is set to TRUE, do not take the descendant classes 
      if ((equivalentFlag != null) && (!equivalentFlag.equalsIgnoreCase("TRUE"))) {
      Set<Set<OWLClass>> subClsSets;
      subClsSets = reasoner.getDescendantClasses(owlClassObject);
      for(OWLClass ols : OWLReasonerAdapter.flattenSetOfSets(subClsSets))
      {                 
               //System.out.println(ols.getURI());
               result.add(ols.getURI());
      }
      } 
      } catch (OWLReasonerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return result;
   }

   @Override
   public Set<URI> getSubClass(OWLReasoner reasoner, OWLClass owlClassObject){
      // TODO Auto-generated method stub
      Set<URI> result = new HashSet<URI>();
      Set<Set<OWLClass>> subClsSets;
      try {
         subClsSets = reasoner.getDescendantClasses(owlClassObject);
                        
         System.out.println("\n Now we are printing the URI's");               
         
         for(OWLClass ols : OWLReasonerAdapter.flattenSetOfSets(subClsSets))
      {                 
               System.out.println(ols.getURI());
               result.add(ols.getURI());
      }
         System.out.println("\n");

      } catch (OWLReasonerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      // In this case, we don't particularly care about the equivalences, so we will flatten this
      // set of sets and print the result
         
         
      return result;
   }

   @Override
   public Set<URI> getSuperClass(OWLReasoner reasoner, OWLClass owlClassObject) {
      Set<URI> result = new HashSet<URI>();
      // TODO Auto-generated method stub
      Set<Set<OWLClass>> superClsSets;      
      
      try {
         superClsSets = reasoner.getAncestorClasses(owlClassObject);
         System.out.println("superClsSets size: "+superClsSets.size());
            
      for(OWLClass ols : OWLReasonerAdapter.flattenSetOfSets(superClsSets))
   {                 
            System.out.println("URIs: "+ols.getURI());
            result.add(ols.getURI());
   }
      } catch (OWLReasonerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      System.out.println("result size: "+result.size());
      return result;
   }
   
  
   public ZExpression getAVHClass(URI ontMapURI, URI classID, String AVHRole,

   ZExpression inp) {
      
      Set<URI> result = null;
      
      try {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
      OWLOntology ont = manager.loadOntologyFromPhysicalURI(ontMapURI);
      System.out.println("Loaded " + ont.getURI());
      OWLReasonerFactory reasonerFactory = new PelletReasonerFactory();
      OWLReasoner reasoner = reasonerFactory.createReasoner(manager);
      Set<OWLOntology> importsClosure = manager.getImportsClosure(ont);
      System.out.println("importsClosure "+ importsClosure.size());
      reasoner.loadOntologies(importsClosure);
      reasoner.classify();
      
      boolean consistent = reasoner.isConsistent(ont);
      System.out.println("Consistent: " + consistent);
      System.out.println("\n");
      
       /*We can easily get a list of inconsistent classes.  (A class is inconsistent if it
       can't possibly have any instances).  Note that the getInconsistentClasses method
       is really just a convenience method for obtaining the classes that are equivalent
       to owl:Nothing. */
      
      Set<OWLClass> inconsistentClasses = reasoner.getInconsistentClasses();
      if (!inconsistentClasses.isEmpty()) {
          System.out.println("The following classes are inconsistent: ");
          for(OWLClass cls : inconsistentClasses) {
              System.out.println("    " + cls);
          }
      }
      else {
          System.out.println("There are no inconsistent classes");
      }
      System.out.println("\n");

      OWLClass owlClassObject = manager.getOWLDataFactory().getOWLClass(URI.create(classID.toString()));

      if (AVHRole.equals(">")) {
         result = getSuperClass(reasoner, owlClassObject);
      } else if (AVHRole.equals("<")) {
         result = getSubClass(reasoner, owlClassObject);
      } else if (AVHRole.equals("=")) {
         result = getEquivalentClass(reasoner, owlClassObject);
      } else {
         // Will not Happen
         logger.warn("UNKNOWN AVH ROLE=" + AVHRole);
         return inp;
      }
      }catch(UnsupportedOperationException exception) {
         System.out.println("Unsupported reasoner operation.");
      }
      catch(OWLReasonerException ex) {
          System.out.println("Reasoner error: " + ex.getMessage());
      }
      catch (OWLOntologyCreationException e) {
          System.out.println("Could not load the pizza ontology: " + e.getMessage());
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
        // logger.trace("NOTHING FOR: " + classID + " " + AVHRole
         //      + "  in data source: " + targetDataSource);
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
}
