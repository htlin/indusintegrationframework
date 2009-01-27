package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.exceptions.OntologyParsingException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;

/**
 * @author neeraj
 */
public class DefaultOntologyParserImpl extends AbstractOntologyParser {

   private static final Logger logger = Logger
         .getLogger(DefaultOntologyParserImpl.class);

   private boolean debug = false; //set to true will display ontologies

   //Keep track of files you parsed so that you don't do it again
   private Set<String> filesParsed = new HashSet<String>();

   @Override
   protected String initSeparator() {
      return "/";
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.iastate.ailab.qengine.core.reasoners.impl.OntologyParser#parse(java.lang.String,
    * boolean, org.iastate.ailab.qengine.core.reasoners.impl.OntologyStore)
    */
   public void parse(String source, boolean inline, OntologyStore store) {

      BufferedReader reader;
      FileInputStream in = null;
      if (inline) {
         //inline = true => source itself contains the ontology
         reader = new BufferedReader(new StringReader(source));
      } else {
         //the source points to a file where the ontology exists

         if (filesParsed.contains(source)) {
            return; //already parsed it, no need to do it
            //TODO: Handle Timestamps so that you can do dynamic loading
         }
         try {
            in = new FileInputStream(source);
         } catch (FileNotFoundException e) {
            throw new OntologyParsingException(
                  "FileNotFoundException - Could not find the file: " + source);
         }
         reader = new BufferedReader(new InputStreamReader(in));
      }

      //keep track of prefixes and associated namespaces
      Map<String, String> prefixToNameSpace = new HashMap<String, String>();

      try {
         // The following is how the ontology is stored
         // ;typename=positionType
         // ;subTypeOf=AVH
         // ;ordername=ISA
         // xmlns:n1=www.ailab.iastate.edu/indus/uView/ont1 in the file
         // n1:positionType_AVH{
         // ..\positionType_AVH\grad
         // ..\positionType_AVH\grad\M.S
         // ..\positionType_AVH\grad\ph.D
         // ..\positionType_AVH\_undergraduate
         // ..\positionType_AVH\_undergraduate\fe
         // ..\positionType_AVH\_undergraduate\so
         // ..\positionType_AVH\_undergraduate\jun
         // ..\positionType_AVH\_undergraduate\se

         String strPatternXmlns = "^xmlns:([^\\=]+)\\=(\\S+)";
         String strPatternOntName = "^([^\\:]+)\\:([^\\:]+)\\{\\s*$";
         String strPatternAxiom = "^\\.\\.\\\\(\\S+)$";
         String strPatternEndAxioms = "^}";

         Pattern patternXmlns = Pattern.compile(strPatternXmlns);
         Pattern patternOntName = Pattern.compile(strPatternOntName);
         Pattern patternAxiom = Pattern.compile(strPatternAxiom);
         Pattern patternEndAxioms = Pattern.compile(strPatternEndAxioms);

         OntologyI currOntology = null;
         // ==================
         // === Parse ====
         // ==================
         while (true) {
            String line;
            try {
               line = reader.readLine();
            } catch (IOException e) {
               throw new OntologyParsingException("IOException - "
                     + e.getMessage(), e);
            }
            if (line == null)
               break;

            if (debug)
               System.out.println("\n currline-->>" + line);

            Matcher matcher = patternXmlns.matcher(line);
            while (matcher.find()) {
               String newOntoNamespace = matcher.group(2);
               String newOntoAbbrev = matcher.group(1);
               //store all the prefixes
               prefixToNameSpace.put(newOntoAbbrev, newOntoNamespace);
            }

            matcher = patternOntName.matcher(line);
            while (matcher.find()) {
               String currOntoAbbrev = matcher.group(1);
               String currOntoName = matcher.group(2);

               URI currOntoURI;
               if (prefixToNameSpace.containsKey(currOntoAbbrev)) {
                  //prefix seen before, use its expansion

                  String prefix = prefixToNameSpace.get(currOntoAbbrev);
                  try {
                     currOntoURI = URIUtils.createURI(currOntoName, prefix);
                  } catch (URISyntaxException e) {
                     throw new OntologyParsingException(
                           "URISyntaxException while trying to create a URI from the ontology with name "
                                 + currOntoName + " and namspace prefix "
                                 + prefix, e);
                  }
                  currOntology = SolutionCreator.getOntologyImpl();
                  //axioms = new HashSet<Axiom>();
               } else {
                  //the prefix not seen before
                  String trimmedLine = line.trim();
                  try {
                     currOntoURI = URIUtils.createURI(trimmedLine);
                  } catch (URISyntaxException e) {
                     throw new OntologyParsingException(
                           "URISyntaxException while trying to create a URI from the following String: "
                                 + trimmedLine, e);
                  }
               }
               currOntology.setOntologyURI(currOntoURI); //uniquely identify the ontology
            }

            matcher = patternAxiom.matcher(line);

            while (matcher.find()) {
               String currOntologyURIString = currOntology.getOntologyURI()
                     .toString();

               String[] ontoNodes = matcher.group(1).split("\\\\");

               URI parentNode;
               String parentNodeLocalValue;
               if (ontoNodes.length > 2) {
                  parentNodeLocalValue = ontoNodes[ontoNodes.length - 2];
                  String childNodeLocalValue = ontoNodes[ontoNodes.length - 1];

                  try {
                     parentNode = URIUtils.createURI(parentNodeLocalValue,
                           currOntologyURIString);
                  } catch (URISyntaxException e) {
                     throw new OntologyParsingException(
                           "URISyntaxException while trying to create a URI from the node "
                                 + parentNodeLocalValue
                                 + " and the ontology URI "
                                 + currOntologyURIString, e);
                  }
                  URI childNode;
                  try {
                     childNode = URIUtils.createURI(childNodeLocalValue,
                           currOntologyURIString);
                  } catch (URISyntaxException e) {
                     throw new OntologyParsingException(
                           "URISyntaxException while trying to create a URI from the node "
                                 + childNodeLocalValue
                                 + " and the ontology URI "
                                 + currOntologyURIString, e);
                  }

                  currOntology.addClass(parentNode);
                  currOntology.addClass(childNode);
                  AxiomImpl a = new AxiomImpl(parentNode, childNode,
                        AVHRole.SUPER_CLASS);
                  currOntology.addAxiom(a);
               } else if (ontoNodes.length > 1) {
                  //special case: node connected to root with no children
                  parentNodeLocalValue = ontoNodes[ontoNodes.length - 1];
                  try {
                     parentNode = URIUtils.createURI(parentNodeLocalValue,
                           currOntologyURIString);
                  } catch (URISyntaxException e) {
                     throw new OntologyParsingException(
                           "URISyntaxException while trying to create a URI from the node "
                                 + parentNodeLocalValue
                                 + " and the ontology URI "
                                 + currOntologyURIString, e);
                  }

                  //no Axiom, just add as class
                  currOntology.addClass(parentNode);
               }
            }

            matcher = patternEndAxioms.matcher(line);
            while (matcher.find()) {
               store.addOntology(currOntology.getOntologyURI(), currOntology);
               if (debug) {
                  currOntology.display(System.out); //Debugging, display current ontology
               }
            }
         }
      } finally {
         //Close Resources
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException e) {
               logger.error(
                     "IOException - Could not close the BufferedReader: "
                           + reader, e);
            }
         }

         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               logger
                     .error(
                           "IOException - Could not close the FileInputStream: "
                                 + in, e);
            }
         }
      }
   }
}
