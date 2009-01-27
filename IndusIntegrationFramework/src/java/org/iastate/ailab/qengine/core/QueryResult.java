package org.iastate.ailab.qengine.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.ResultSet;

public class QueryResult {
   private ResultSet rs = null;

   private String message = "";

   private BigInteger count = null;

   private BigDecimal avg = null;

   // I'm not sure if these should be doubles, what if it's a min/max date?
   private BigDecimal min = null;

   private BigDecimal max = null;

   public void setResultSet(ResultSet rs) {
      this.rs = rs;
   }

   public ResultSet getResultSet() {
      return rs;
   }

   public void setMessage(String message) {
      this.message = message;
   }

   public String getMessage() {
      return message;
   }

   public void setCount(BigInteger count) {
      this.count = count;
   }

   public BigInteger getCount() {
      return count;
   }

   public void setAverage(BigDecimal avg) {
      this.avg = avg;
   }

   public BigDecimal getAverage() {
      return avg;
   }

   public void setMin(BigDecimal min) {
      this.min = min;
   }

   public BigDecimal getMin() {
      return min;
   }

   public void setMax(BigDecimal max) {
      this.max = max;
   }

   public BigDecimal getMax() {
      return max;
   }

   public Object getResultObject() {
      Object result = null;

      if (rs != null) {
         result = rs;
      } else if (count != null) {
         result = count;
      } else if (avg != null) {
         result = avg;
      } else if (min != null) {
         result = min;
      } else if (max != null) {
         result = max;
      }

      return result;
   }
}
