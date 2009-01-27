package org.iastate.ailab.qengine.core;

import java.util.Vector;

import Zql.ZExp;
import Zql.ZFromItem;
import Zql.ZQuery;
import Zql.ZSelectItem;

public class TransFormerUtility {

   //utility class
   public class WhereTranslationResult {
      boolean participatesInTranslation;

      ZExp exp;
   }

   public class SelectTranslationResult {
      boolean participatesInTranslation;

      Vector<ZSelectItem> select;
   }

   public class FromTranslationResult {
      boolean participatesInTranslation;

      Vector<ZFromItem> from;
   }

   public class QueryTranslationResult {
      boolean participatesInTranslation;

      ZQuery query;
   }
}
