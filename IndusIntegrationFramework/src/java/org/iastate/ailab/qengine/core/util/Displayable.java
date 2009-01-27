package org.iastate.ailab.qengine.core.util;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public interface Displayable {

   public void display(OutputStream out);

   public void display(Writer writer);

   public void display(PrintWriter printWriter);
}
