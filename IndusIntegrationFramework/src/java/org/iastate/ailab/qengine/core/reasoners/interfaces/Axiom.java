package org.iastate.ailab.qengine.core.reasoners.interfaces;

import java.net.URI;

import org.iastate.ailab.qengine.core.reasoners.impl.AVHRole;
import org.iastate.ailab.qengine.core.util.Displayable;

public interface Axiom extends Displayable {

   //public DataContent getFromNode();

   //public DataContent getToNode()l

   public URI getFromNode();

   public URI getToNode();

   public AVHRole getRole();
}
