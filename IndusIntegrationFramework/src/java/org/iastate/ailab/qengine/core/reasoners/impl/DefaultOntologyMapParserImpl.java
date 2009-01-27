package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.exceptions.OntologyMapParsingException;

/**
 * @author neeraj
 */
public class DefaultOntologyMapParserImpl implements OntologyMapParser {

   private static final Logger logger = Logger
         .getLogger(DefaultOntologyMapParserImpl.class);

   //Keep track of files you parsed so that you don't do it again
   private Set<String> filesParsed = new HashSet<String>();

   /*
    * stores the user defined mapping as properties
    * DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_junior=EQUAL@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_jun
    */
   Properties maps = new Properties();

   public void parse(String source, boolean inline, OntologyMapStore store)
         throws FileNotFoundException, IOException {

      FileInputStream in = null;
      //TODO reader is not actually used
      BufferedReader reader = null;

      try {
         if (inline == false) {
            //the source points to a file where the ontology exists

            if (filesParsed.contains(source)) {
               return; //already parsed it, no need to do it
               //TODO: Handle Timestamps so that you can do dynamic loading
            }
            in = new FileInputStream(source);
         } else {
            //inline = true => source itself contains the ontology
            reader = new BufferedReader(new StringReader(source));
            in = new FileInputStream(source);
            //reader = new BufferedReader(new InputStreamReader(in));
         }
         maps.load(in);
         Iterator<Object> it = maps.keySet().iterator();
         while (it.hasNext()) {
            String key = (String) it.next();
            String value = (String) maps.get(key);
            ParseAndStore(key, value, store);
         }
      } finally {
         try {
            if (reader != null) {
               reader.close();
            }
         } catch (IOException e) {
            logger
                  .error("IOException closing the BufferedReader: " + reader, e);
         }
         try {
            if (in != null) {
               in.close();
            }
         } catch (IOException e) {
            logger.error("IOException closing the FileInputStream: " + in, e);
         }
      }
   }

   /**
    * Parses and store a single line (identified as key=value) in the Onto
    * Map
    * 
    * @param key
    * @param value
    * @param store
    */
   private void ParseAndStore(String key, String value, OntologyMapStore store) {
      //key --> DS1@www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH@_junior
      //value --> EQUAL@www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH@_jun

      String[] vals = key.split("@");
      String dataSourceName = vals[0];
      String dataSourceOntologyString = vals[1];

      //URI dataSourceOntologyURI = URIUtils.createURI(dataSourceOntologyString );
      String classId = vals[2];
      URI dataSourceClassURI = null;
      if (classId.startsWith("_")) {
         classId = classId.substring(1); //drop the '_'
         //since started with '_' prefix the data source ontology to it
         try {
            dataSourceClassURI = URIUtils.createURI(classId,
                  dataSourceOntologyString);
         } catch (URISyntaxException e) {
            throw new OntologyMapParsingException(
                  "URISyntaxException while trying to create a URI from the class ID "
                        + classId + " and the data source ontology "
                        + dataSourceOntologyString, e);
         }
      } else {
         try {
            dataSourceClassURI = URIUtils.createURI(classId);
         } catch (URISyntaxException e) {
            throw new OntologyMapParsingException(
                  "URISyntaxException while trying to create a URI from the class ID "
                        + classId, e);
         }
      }

      //handle the value
      vals = value.split("@");

      //The mappings in the config file are stored data source --> user View.
      //internally we store it as an axiom from userView --> data source. Hence get the inverse role
      //because A > B => B < A
      AVHRole relationShip = getInverseAVHRole(vals[0]);

      URI userViewOntologyURI;
      try {
         userViewOntologyURI = URIUtils.createURI(vals[1]);
      } catch (URISyntaxException e) {
         throw new OntologyMapParsingException(
               "URISyntaxException while trying to create a URI from the String "
                     + vals[1], e);
      }
      String userViewClassId = vals[2];
      URI userViewClassURI;
      if (userViewClassId.startsWith("_")) {
         userViewClassId = userViewClassId.substring(1); //drop the '_'
         //since started with '_' prefix the data source ontology to it
         try {
            userViewClassURI = URIUtils.createURI(userViewClassId, vals[1]);
         } catch (URISyntaxException e) {
            throw new OntologyMapParsingException(
                  "URISyntaxException while trying to create a URI from the class ID "
                        + classId + " and the data source ontology " + vals[1],
                  e);
         }
      } else {
         try {
            userViewClassURI = URIUtils.createURI(classId);
         } catch (URISyntaxException e) {
            throw new OntologyMapParsingException(
                  "URISyntaxException while trying to create a URI from the String "
                        + vals[1], e);
         }
      }
      //construct an Axiom which maps a concept in user view to the data source view
      AxiomImpl mapping = new AxiomImpl(userViewClassURI, dataSourceClassURI,
            relationShip);
      store.addMapping(userViewOntologyURI, mapping, dataSourceName);
   }

   /**
    * If the relation is SUPER we return SubClass and if it is SUB we
    * return SuperClass For EQUAL we return EquivalnetClass This is use to
    * because if A SuperrClass B => B SubClass A
    * 
    * @param relationShip
    * @return
    */
   private AVHRole getInverseAVHRole(String relationShip)
         throws IllegalArgumentException {
      if (relationShip.equals("EQUAL")) {
         return AVHRole.EQUIVALENT_CLASS;
      } else if (relationShip.equals("SUPER")) {
         return AVHRole.SUB_CLASS;
      } else if (relationShip.equals("SUB")) {
         return AVHRole.SUPER_CLASS;
      } else {
         String message = "Encountered unknown AVHRole while parsing mappings: "
               + relationShip;
         logger.warn(message);
         throw new IllegalArgumentException(message);
      }
   }
}
