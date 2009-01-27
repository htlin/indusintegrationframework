package org.iastate.ailab.qengine.core.util;

import org.iastate.ailab.qengine.core.exceptions.ConversionException;

import com.primalworld.math.MathEvaluator;

public class ExpressionEvaluator {

   //TODO: Make this an Impl class of the Evluation
   MathEvaluator eval;;

   /**
    * 
    * @param expression An expression with variable x
    * @param value The value to be assigned to x
    * @return evaluation of exception
    */
   public static double evluateExpression(String expression, double value)
         throws ConversionException {
      try {
         MathEvaluator m = new MathEvaluator(expression);
         m.addVariable("x", value); //assuming expression has x as variable
         return m.getValue();
      } catch (Exception e) {
         throw new ConversionException("Error evaluation expression:"
               + expression, e);
      }

   }

   /**
    * An string representation of the expression to be evaluated The
    * variable in expression must be x
    * 
    * @param expression
    */
   public ExpressionEvaluator(String expression) {
      try {
         eval = new MathEvaluator(expression);
      } catch (Exception e) {
         throw new ConversionException("Error instantiating expression:"
               + expression, e);
      }
   }

   /**
    * 
    * This evaluates the expression with variable taking the value passed
    * The expression is set by call to constructor
    * 
    * @param value the value to be assigned to the variable
    * @return
    */
   public double getValue(double value) {
      try {
         eval.addVariable("x", value); //assuming expression has x as variable
         return eval.getValue();
      } catch (Exception e) {
         throw new ConversionException("Error evaluation expression:" + e
               + " on value " + value);

      }
   }

   /**
    * Evalutes the expression
    * 
    * @param doubleValue The value of the variable passed as String
    * @return eveluated value represented as a String
    * @throws ConversionException
    */
   public String getValue(String doubleValue) throws ConversionException {
      //TODO: Catch NumberFormatException for parsing
      Double varVal = Double.parseDouble(doubleValue);
      double expVal = getValue(varVal.doubleValue());
      return Double.toString(expVal);

   }

   public String printExpression() {
      return eval.toString();
   }

   /**
    * A Local test for the evaluator
    * 
    * @param args
    */
   public static void main(String[] args) {

      String exp = "9/5 * (x)+32"; // note 9/5(x) + 32 won't work
      //String exp1 = "-5-6/(-2) + sqr(15+x)"; //another example
      int val = (int) ExpressionEvaluator.evluateExpression(exp, 10);
      System.out.println("10 C maps to " + val + "F");
      ExpressionEvaluator eval = new ExpressionEvaluator("(x-32)*5/9");
      System.out.println("33F maps to: " + eval.getValue(33) + "C");
      System.out.println("41F maps to:" + eval.getValue(41) + "C");

   }
}
