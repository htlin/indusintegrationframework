package org.iastate.ailab.qengine.core.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.ConnectionManager;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.datasource.ColumnDescriptor;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DataSourceDescriptor;
import org.iastate.ailab.qengine.core.datasource.DefaultColumnDescriptorImpl;
import org.iastate.ailab.qengine.core.datasource.DefaultDataSourceDescriptorImplementation;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.exceptions.EngineException;
import org.iastate.ailab.qengine.core.exceptions.PropertiesException;
import org.iastate.ailab.qengine.core.reasoners.impl.URIUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class DataSourceDescriptorXMLLoader extends DefaultHandler implements
      DataSourceDescriptorLoader {
   private static final Logger logger = Logger
         .getLogger(DataSourceDescriptorXMLLoader.class);

   private SAXParser parser;

   private DataSourceDescriptor currDescriptor;//Stack<DataSourceDescriptor> descriptorStack;

   private final Set<ColumnDescriptor> tableColumns = new HashSet<ColumnDescriptor>();

   private String currTableName;

   private ColumnDescriptor currColumn;

   private DataNode node;

   private String XMLFile;

   private String baseDirectoryAsString = null;

   public DataSourceDescriptorXMLLoader() {
//		descriptorStack = new Stack<DataSourceDescriptor>();
//		tableColumns = new Vector<String>();
//		currTableName = "";
//		XMLFile = "";
      SAXParserFactory saxFactory = SAXParserFactory.newInstance();

      try {
         parser = saxFactory.newSAXParser();
      } catch (ParserConfigurationException e) {
         throw new ConfigurationException("ParserConfigurationException - "
               + e.getMessage(), e);
      } catch (SAXException e) {
         throw new ConfigurationException("SAXException - " + e.getMessage(), e);
      }
   }

   public DataSourceDescriptorXMLLoader(String XMLFile) {
      this(); // do regular construction first then set the XMLFile
      this.XMLFile = XMLFile;

      try {
         java.io.File XML = new java.io.File(XMLFile);
         baseDirectoryAsString = XML.getParentFile().getCanonicalPath();
      } catch (Exception ignore) {
         this.logger
               .debug(
                     "Ignoring exception to find relative paths in DataDourceDescriptorXML Loader ",
                     ignore);
      }
   }

   public void addDataSourceDescriptors(DataNode node) {
      this.node = node;
      try {
         parser.parse(XMLFile, this);
      } catch (SAXException e) {
         throw new ConfigurationException("SAXException - " + e.getMessage(), e);
      } catch (IOException e) {
         throw new ConfigurationException("IOException - " + e.getMessage(), e);
      }
   }

   public void setXMLFile(String XMLFile) {
      this.XMLFile = XMLFile;
      //handle relative paths
      try {
         java.io.File XML = new java.io.File(XMLFile);
         baseDirectoryAsString = XML.getParentFile().getCanonicalPath();
      } catch (Exception ignore) {
         this.logger
               .debug(
                     "Ignoring exception to find relative paths in DataDourceDescriptorXML Loader ",
                     ignore);
      }
   }

   @Override
   public void startElement(String uri, String locName, String qName,
         Attributes attributes) {
      if (qName.equals("descriptor")) {
         //this.createReasoner();
         this.createDescriptor(attributes);
      } else if (qName.equals("datasource")) {
         if (attributes.getValue("name") != null) {
            currDescriptor.setDSName(attributes.getValue("name"));
         }
      } else if (qName.equals("table")) {
         if (attributes.getValue("name") != null) {
            currTableName = attributes.getValue("name");
            tableColumns.clear(); // clear out the old table columns
         } else {
            // throw an exception probably
            //TODO do not catch own Exception
            try {
               throw (new EngineException("Null attributes for table creation"));
            } catch (EngineException e) {
               e.printStackTrace();
            }
         }
      } else if (qName.equals("column")) {
         this.addColumn(attributes);
      } else if (qName.equals("operation")) {
         try {
            this.storeColumnMetaData(attributes);
         } catch (RuntimeException e) {
            String message = "RuntimeException while handling 'operation' attribute in DataDescriptor";
            logger.warn(message, e);
            throw new ConfigurationException(message, e);
         }
      }
   }

   @Override
   public void endElement(String uri, String locName, String qName) {
      if (qName.equals("table")) {
         this.addTable();
      } else if (qName.equals("descriptor")) {
         //currDescriptor.setReasoner(currReasoner);
         node.setDataSourceDescriptor(currDescriptor);
      }
   }

   private void createDescriptor(Attributes attr) {
      currDescriptor = new DefaultDataSourceDescriptorImplementation();
      currDescriptor.setVirtual(false);
   }

//	private void createReasoner()
//	{
//		currReasoner = new INDUSReasoner();
//		currReasoner.setDataSouceNode(node);
//	}

   private void addColumn(Attributes attr) {

      //save reference to the column name being processed
      currColumn = new DefaultColumnDescriptorImpl();

      String name = attr.getValue("name");
      currColumn.setColumnName(name);

      //store all attributes as properties
      String key = "";
      for (int i = 0; i < attr.getLength(); i++) {
         key = attr.getQName(i);
         String temp = attr.getValue(i);
         currColumn.setProperty(key, attr.getValue(i));
      }
      tableColumns.add(currColumn);
   }

   private void storeColumnMetaData(Attributes attr) {

      String type = attr.getValue("type");

      boolean isAVH = type.equals("AVH");
      currColumn.setAVH(isAVH);
      if (isAVH) {
         String ontParam = attr.getValue("ontology");

         URI classID;
         try {
            classID = URIUtils.createURI(ontParam);
         } catch (URISyntaxException e) {
            String resource = node.getNodeName() + "." + currTableName + "."
                  + currColumn.getColumnName();
            String message = "The ontology associated with the column "
                  + resource + ", " + ontParam + ", is not a URI";
            logger.warn(message, e);
            throw new ConfigurationException(message, e);
         }

         currColumn.setColumnOntologyURI(classID);

         /*
          * Try to identify if the ontology is stored in the database,
          * specified by ontId
          */
         final String ontId = attr.getValue("ontId");
         if (ontId != null) {

            /* Read ontology from Indus database */
            final String ontology = readOntology(ontId);
            if (ontology != null && !ontology.trim().equals("")) {
               try {
                  Init._this().getViewContext().getOntologyStore().parse(
                        ontology, true);
               } catch (FileNotFoundException e) {
                  throw new PropertiesException("Cound not find the file "
                        + ontology, e);
               } catch (IOException e) {
                  throw new PropertiesException(
                        "IOException - Could not load the properties from the InputStream based on the file "
                              + ontology, e);
               }
            } else {
               String nullOrEmpty = ontology == null ? "Null" : "Empty";
               throw new ConfigurationException(nullOrEmpty
                     + " ontology encounterd in database for id = " + ontId);
            }
         } else {
            /*
             * Ontology must be present in some file specified by ontLoc
             */
            String ontLoc = attr.getValue("ontLoc");

            if (ontLoc != null) {
               if (baseDirectoryAsString != null) {
                  ontLoc = baseDirectoryAsString
                        + System.getProperty("file.separator") + ontLoc;
               }

               try {
                  Init._this().getViewContext().getOntologyStore().parse(
                        ontLoc, false);
               } catch (FileNotFoundException e) {
                  throw new PropertiesException("Cound not find the file "
                        + ontLoc, e);
               } catch (IOException e) {
                  throw new PropertiesException(
                        "Could not load the properties from the InputStream based on the file "
                              + ontLoc, e);
               }
            } else {
               throw new ConfigurationException(
                     "Neither ontId nor ontLoc provided.");
            }
         }
      }

      //store all the attributes. Have Unique names or will overwrite column attributes of same name
      for (int i = 0; i < attr.getLength(); i++) {
         String key = attr.getQName(i);
         String value = attr.getValue(i);
         currColumn.setProperty(key, value);
      }
   }

   /**
    * Reads the ontology.
    * 
    * @param ontId the ontology id
    * 
    * @return the string containing the ontology
    */
   private String readOntology(String ontId) {
      String ontology = null;

      Connection connection = null;
      try {
         final String query = "SELECT ontology FROM ontologyref WHERE id = '"
               + ontId + "'";

         connection = ConnectionManager.getIndusConnection();
         final Statement statement = connection.createStatement();

         final ResultSet resultSet = statement.executeQuery(query);
         if (resultSet != null && resultSet.next()) {
            final Blob ontologyBlog = resultSet.getBlob("ontology");
            ontology = new String(ontologyBlog.getBytes(1, (int) ontologyBlog
                  .length()));
            logger.trace("---> ontology: " + ontology);
         }
      } catch (SQLException e) {
         throw new ConfigurationException("SQLException - " + e.getMessage(), e);
      } finally {
         try {
            connection.close();
         } catch (SQLException e) {
            logger.debug("SQLException closing the Connection: " + connection,
                  e);
         }
      }

      return ontology;
   }

   private void addTable() {
      currDescriptor.addTable(currTableName, tableColumns);
   }

//	private void addChildAggregationStrategy(String joinCol) {
//      DefaultDataNodeAggregationStrategy strategy = new DefaultDataNodeAggregationStrategy();
//      strategy
//            .setChildLevelFragmentationType(DataNode.LevelFragmentationType.VERTICAL);
//      JoinColumn jc = strategy.new JoinColumn("EMPLOYEETABLE", "key");
//      strategy.setChildJoinColumn("DS2", jc);
//      jc = strategy.new JoinColumn("EMPLOYEETABLE", "key");
//      strategy.setChildJoinColumn("DS3", jc);
//      return strategy;
//
//      // need to rethink this quite a bit for vertical and for horizontal strategies
//      DefaultDataNodeAggregationStrategy strategy = new DefaultDataNodeAggregationStrategy();
//      strategy.setChildLevelFragmentationType(childFragType);
//      JoinColumn jc = strategy.new JoinColumn(currTableName, joinCol);
//
//      // add the join column to each child
//      for (DataNode child : node.getChildren()) {
//         strategy.setChildJoinColumn(child.getNodeName(), jc);
//      }
//
//      // add the strategy to the node
//      node.setDataNodeAggregationStrategy(strategy);
//   }
}
