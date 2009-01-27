package org.iastate.ailab.qengine.core.reasoners.impl;

import java.io.File;
import java.net.URI;
import java.util.Scanner;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class OwlOntologyParserImplTest {

   private static int testNum = 0;

   private OntologyStore ontologyStore;

   private OntologyParser owlOntologyParserImpl;

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      System.out.println("--------------------Starting JUnit test OwlOntologyParserImplTest--------------------");
   }

   @AfterClass
   public static void tearDownAfterClass() throws Exception {
      System.out.println("--------------------Finished JUnit test OwlOntologyParserImplTest--------------------");
   }

   @Before
   public void setUp() throws Exception {
      System.out.print("********************Starting test number "
            + ++testNum + "********************");

      ontologyStore = new DefaultOntologyStoreImpl();
      owlOntologyParserImpl = new OwlOntologyParserImpl();
   }

   @After
   public void tearDown() throws Exception {
      System.out.println("********************Finished test number " + testNum
            + "********************");
   }

   @Test
   public void testParseNotInline() throws Exception {
      System.out.println("testParseNotInline");

      String source = "config-example-2/ont0.owl";
      boolean inline = false;

      owlOntologyParserImpl.parse(source, inline, ontologyStore);

      String ontologyURIString = "http://www.ailab.iastate.edu/indus/uView/ont0/positionType_AVH";
      URI ontologyURI = new URI(ontologyURIString);

      OntologyI ontology = ontologyStore.getOntology(ontologyURI);
      Assert.assertNotNull(ontology);

      System.out.println("Ontology in the file '" + source + "' is:");
      System.out.println();
      System.out.println(ontology);
   }

   @Test
   public void testParseInline() throws Exception {
      System.out.println("testParseNotInline");

      String sourceFile = "config-example-2/ont1.owl";
      Scanner fileIn = new Scanner(new File(sourceFile));

      String source = "";
      while (fileIn.hasNext()) {
         source += fileIn.nextLine() + "\n";
      }

      boolean inline = true;

      owlOntologyParserImpl.parse(source, inline, ontologyStore);

      String ontologyURIString = "http://www.ailab.iastate.edu/indus/ds/ont1/statusType_AVH";
      URI ontologyURI = new URI(ontologyURIString);

      OntologyI ontology = ontologyStore.getOntology(ontologyURI);
      Assert.assertNotNull(ontology);

      System.out.println("Ontology in the file '" + sourceFile + "' is:");
      System.out.println();
      System.out.println(ontology);
   }

   @Test
   public void testParseWithUnrelatedClasses() throws Exception {
      System.out.println("testParseNotInline");

      String source = "config-example-2/ont2.owl";
      boolean inline = false;

      owlOntologyParserImpl.parse(source, inline, ontologyStore);

      String ontologyURIString = "http://www.ailab.iastate.edu/indus/ds/ont2/typeType_AVH";
      URI ontologyURI = new URI(ontologyURIString);

      OntologyI ontology = ontologyStore.getOntology(ontologyURI);
      Assert.assertNotNull(ontology);

      System.out.println("Ontology in the file '" + source + "' is:");
      System.out.println();
      System.out.println(ontology);
   }
}
