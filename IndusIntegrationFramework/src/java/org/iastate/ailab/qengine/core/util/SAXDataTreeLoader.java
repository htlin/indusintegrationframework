package org.iastate.ailab.qengine.core.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy;
import org.iastate.ailab.qengine.core.Init;
import org.iastate.ailab.qengine.core.DefaultDataNodeAggregationStrategy.JoinColumn;
import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.datasource.DefaultDataNodeImplementation;
import org.iastate.ailab.qengine.core.datasource.DataNode.LevelFragmentationType;
import org.iastate.ailab.qengine.core.datasource.DataNode.NodeLocation;
import org.iastate.ailab.qengine.core.exceptions.ConfigurationException;
import org.iastate.ailab.qengine.core.exceptions.EngineException;
import org.iastate.ailab.qengine.core.factories.solution.SolutionCreator;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SAXDataTreeLoader extends DefaultHandler implements DataTreeLoader {

   private static final Logger logger = Logger
         .getLogger(SAXDataTreeLoader.class);

   private SAXParser parser;

   private final Stack<DataNode> nodeStack;

   private DataNode rootNode;

   private String joinTable;

   private String joinCol;

   private String joinColType;

   private final HashMap<String, String> properties = new HashMap<String, String>();

   String baseDirectoryAsString; //to handle relative paths

   public SAXDataTreeLoader() {

      nodeStack = new Stack<DataNode>();

      SAXParserFactory saxFactory = SAXParserFactory.newInstance();

      try {
         parser = saxFactory.newSAXParser();
      } catch (ParserConfigurationException e) {
         throw new ConfigurationException("ParserConfigurationException - "
               + e.getMessage(), e);
      } catch (SAXException e) {
         throw new ConfigurationException("SAXException - " + e.getMessage(), e);
      }
      //Set Default value
      baseDirectoryAsString = System.getProperty("user.dir");
   }

   public void setProperty(String name, String value) {
      properties.put(name, value);
   }

   public String getProperty(String name) {
      return properties.get(name);
   }

   public DataNode getDataTree() {
      String XMLFile = Init._this().getViewConfigData().getDTreefile();
      java.io.File XML = new java.io.File(XMLFile);
      try {
         baseDirectoryAsString = XML.getParentFile().getCanonicalPath();
      } catch (IOException e1) {
         //Should not happen. Continue using default value
         e1.printStackTrace();
      }
      try {
         parser.parse(XML, this);
      } catch (SAXException e) {
         throw new ConfigurationException("SAXException - " + e.getMessage(), e);
      } catch (IOException e) {
         throw new ConfigurationException("IOException - " + e.getMessage(), e);
      } //create root node
      addSchemaMapToDTree(); //associate the schemaMap with the DTree
      return rootNode;
   }

   // Callback method for SAXParser
   @Override
   public void startElement(String uri, String locName, String qName,
         Attributes attributes) {
      // qName is the element name
      if (qName.equals("node")) {
         this.createNode(attributes);
         // create node and push it onto the nodeStack
      } else if (qName.equals("children")) {
         // store joinCol and joinTable so we can do the strategy when we find the end children element
         joinCol = attributes.getValue("joincol");
         joinTable = attributes.getValue("joinTable");
         joinColType = attributes.getValue("type");
         // this.addChildAggregationStrategy(attributes);
      } else if (qName.equals("dbInfo")) {
         this.addDBInfo(attributes);
      } else if (qName.equals("dataSourceDescriptor")) {
         this.addDataDescriptor(attributes);
      }
   }

   @Override
   public void endElement(String uri, String locName, String qName) {
      if (qName.equals("node")) {
         // if it has more than 1 node then this one must be a child because there can only be one root node
         if (nodeStack.size() > 1) {
            DataNode child = nodeStack.pop();
            nodeStack.peek().addChild(child);
         } else {
            // we are finished with the current node so pop it from the stack
            DataNode userViewNode = nodeStack.pop();
            String rootOfDTree = userViewNode.getNodeName();
            String userViewName = Init._this().getViewConfigData()
                  .getUserViewName().trim();
            //TODO: May have to trim the userViewName 

            try {
               if (!rootOfDTree.equals(userViewName)) {
                  throw new EngineException(
                        "The root of the DTree does not match the name of the user view: "
                              + rootOfDTree + " != " + userViewName);
               }
            } catch (EngineException e) {
               throw e;
            } catch (RuntimeException e) {
               String message = "Error 1025";
               logger.warn(message, e);
               throw new EngineException(message, e);
            }
         }
      } else if (qName.equals("children")) {
         // if we have finished adding children nodes, then the current node must not be a LEAF_NODE
         nodeStack.peek().setNodeType(NodeLocation.INNER_NODE);
         this.addChildAggregationStrategy();
      }
   }

   private void createNode(Attributes attr) {

      DataNode node = new DefaultDataNodeImplementation();
      node.setNodeName(attr.getValue("name"));

      if (attr.getValue("fragmentationType").equals("horizontal")) {
         node.setLevelFragmentationType(LevelFragmentationType.HORIZONTAL);
      } else if (attr.getValue("fragmentationType").equals("vertical")) {
         node.setLevelFragmentationType(LevelFragmentationType.VERTICAL);
      }

      // assume it is a leaf node until we find a child at which point
      // we will change it to an INNER_NODE
      node.setNodeType(NodeLocation.LEAF_NODE);

      if (nodeStack.empty()) {
         // if it's empty then this is the root node
         rootNode = node;
      }
      // add this node to the stack so we know what the current node is
      nodeStack.push(node);
   }

   private void addDBInfo(Attributes attr) {
      DataNode currNode = nodeStack.peek();

      currNode.setDataSourceDriver(attr.getValue("DRIVER")); //TODO: Refactor away as we  have properties now
      currNode.setDataSource(attr.getValue("DATASOURCE"));

      //store all the attributes. Have Unique  names or will overwrite column attributes of same name
      String key = "";
      String value = "";
      for (int i = 0; i < attr.getLength(); i++) {
         key = attr.getQName(i);
         value = attr.getValue(i);
         currNode.setProperty(key, value);
      }
   }

   private void addDataDescriptor(Attributes attr) {

      DataSourceDescriptorXMLLoader loader = (DataSourceDescriptorXMLLoader) SolutionCreator
            .getDatasourceDescriptorLoader();

      String file = attr.getValue("file");
      //Handling relative paths      
      if (this.baseDirectoryAsString != null) {
         file = baseDirectoryAsString + System.getProperty("file.separator")
               + file;
      }
      DataNode currNode = nodeStack.peek();

      loader.setXMLFile(file);
      loader.addDataSourceDescriptors(currNode);
   }

   private void addChildAggregationStrategy() {
      DataNode currNode = nodeStack.peek();

      // TODO need to get the user view table in a different way
      if (joinCol != null && joinTable != null) {
         // children are fragmented vertically
         DefaultDataNodeAggregationStrategy strategy = new DefaultDataNodeAggregationStrategy();
         strategy
               .setChildLevelFragmentationType(DataNode.LevelFragmentationType.VERTICAL);

         // set the join col for each child
         for (DataNode child : currNode.getChildren()) {
            JoinColumn jc = strategy.new JoinColumn(joinTable, joinCol,
                  joinColType);
            strategy.setChildJoinColumn(child.getNodeName(), jc);
         }

         currNode.setDataNodeAggregationStrategy(strategy);

         // reset them to null so we can do horizontal if needed
         joinCol = null;
         joinTable = null;
      } else {
         // children are fragmented horizontally

         DefaultDataNodeAggregationStrategy strategy = new DefaultDataNodeAggregationStrategy();
         strategy
               .setChildLevelFragmentationType(DataNode.LevelFragmentationType.HORIZONTAL);

         currNode.setDataNodeAggregationStrategy(strategy);
      }
   }

   private void addSchemaMapToDTree() {
      try {
         SchemaMapLoader schemaLoader = SolutionCreator.getSchemaMapLoader();
         //TODO: See how you can use SchemaMapStore/SchemaMapParser to do this
         //we end up having two parsers that parse same information
         schemaLoader.addSchemaMap(rootNode);
      } catch (RuntimeException e) {
         logger.warn("Error 1030", e);
         throw new EngineException(e);
      }
   }
}
