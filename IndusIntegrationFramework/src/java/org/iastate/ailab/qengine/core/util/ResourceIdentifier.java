package org.iastate.ailab.qengine.core.util;

public interface ResourceIdentifier {

   // A resource should be identified as A.B.C.D
   public void setRoot(String root);

   public void addPathlet(String path);

   public void setResource(String resource);

   public String getResourceIdentifier();
}
