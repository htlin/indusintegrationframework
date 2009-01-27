package org.iastate.ailab.qengine.core.reasoners.impl;

import java.net.URI;
import java.util.Set;

import org.iastate.ailab.qengine.core.exceptions.OntologyParsingException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.io.StringInputSource;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

public class OwlOntologyParserImpl extends AbstractOntologyParser {

   @Override
   protected String initSeparator() {
      return "#";
   }

   @Override
   public void parse(String source, boolean inline, OntologyStore store) {
      OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

      OWLOntology ontology;
      if (inline) {
         StringInputSource sis = new StringInputSource(source);
         try {
            ontology = manager.loadOntology(sis);
         } catch (OWLOntologyCreationException e) {
            throw new OntologyParsingException(
                  "OWLOntologyCreationException while trying to load the inline ontology: "
                        + source, e);
         }
      } else {
         //URI physicalURI = URI.create("file:" + source);
         /**
          * URI has problems reading from windows style path. Changing it
          * to unix style paths
          * 
          *TODO Test on Unix
          */
         source = source.replace("\\", "/");
         URI physicalURI = URI.create("file:/" + source);
         try {
            ontology = manager.loadOntologyFromPhysicalURI(physicalURI);
         } catch (OWLOntologyCreationException e) {
            throw new OntologyParsingException(
                  "OntologyParsingException while trying to load the ontology from the physicalURI: "
                        + physicalURI, e);
         }
      }

      OntologyI currOntology = SolutionCreator.getOntologyImpl();

      //URI
      currOntology.setOntologyURI(ontology.getURI());

      //Classes and axioms
      for (OWLClass c : ontology.getReferencedClasses()) {
         //classes
         currOntology.addClass(c.getURI());

         //axioms
         Set<OWLDescription> equivalentClasses = c
               .getEquivalentClasses(ontology);
         for (OWLDescription desc : equivalentClasses) {
            currOntology.addAxiom(new AxiomImpl(desc.asOWLClass().getURI(), c
                  .getURI(), AVHRole.EQUIVALENT_CLASS));
         }

         Set<OWLDescription> superClasses = c.getSuperClasses(ontology);
         for (OWLDescription desc : superClasses) {
            currOntology.addAxiom(new AxiomImpl(desc.asOWLClass().getURI(), c
                  .getURI(), AVHRole.SUPER_CLASS));
         }
      }

      store.addOntology(currOntology.getOntologyURI(), currOntology);
   }
}
