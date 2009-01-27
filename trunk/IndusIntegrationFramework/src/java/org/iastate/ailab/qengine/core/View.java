package org.iastate.ailab.qengine.core;

import org.iastate.ailab.qengine.core.datasource.DataNode;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyMapStore;
import org.iastate.ailab.qengine.core.reasoners.impl.OntologyStore;
import org.iastate.ailab.qengine.core.reasoners.impl.SchemaMapStore;

public interface View {
   //UserViewName=config/view1
   //DTreeFile=config/tree.xml
   //SchemaMap=config/schemamap.txt
   //OntologyMap=config/ontmap.txt

   /**
    * Function to initialize the view
    */
   public void init(ViewConfigData conf);

   /**
    * Returns the structure which provides easy access to get the values
    * with which view was configured/initialized (e.g. UserViewName)
    * 
    * @return
    */
   public ViewConfigData getViewConfigData();

   /**
    * The build DTree corresponding to userView
    * 
    * @return
    */
   public DataNode getDTree();

   /**
    * A common store where all the axioms/concepts corresponding to various
    * ontologies are stored
    * 
    * @return
    */
   public OntologyStore getOntologyStore();

   /**
    * A common store where all the mappings between ontologies (user and
    * data source view) are stored
    * 
    * @return
    */
   public OntologyMapStore getOntologyMapStore();

   /**
    * A common place to store all the schema Mappings between UserView and
    * DataSources
    * 
    * @return
    */
   public SchemaMapStore getSchemaMapStore();
}
