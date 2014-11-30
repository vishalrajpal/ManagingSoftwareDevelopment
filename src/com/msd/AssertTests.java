package com.msd;

public class AssertTests
{
   // Initially exitStatus is 0, if any error occurs this will chage to 1.
   private static int exitStatus = 0;

   /**
    * assertTrue : String, Boolean -> Boolean
    * 
    * @param errorMsg
    *           : The errorMsg to print if 'isTrue' is false
    * @param isTrue
    *           : if false the 'errorMsg' is printed through standard error and
    *           the program exits with status other than 0.
    * @return : returns false if isTrue is false else returns true
    */
   public static boolean assertTrue(String errorMsg, boolean isTrue)
   {
      if (!isTrue)
      {
         exitStatus = 1;
         System.err.println("ERROR: " + errorMsg);
         return false;
      }
      return isTrue;
   }

   /**
    * assertTrue : String, Boolean, Boolean -> Boolean/void
    * 
    * @param errorMsg
    *           : The error message to print if isTrue is false
    * @param isTrue
    *           : if false the 'errorMsg' is printed through standard error and
    *           the program exits with status other than 0.
    * @param toExit
    *           : if true and isTrue is false program exits with status other
    *           than 0
    * @return : returns the value of isTrue;
    */
   public static boolean assertTrue(String errorMsg, boolean isTrue,
         boolean toExit)
   {
      assertTrue(errorMsg, isTrue);
      if (!isTrue && toExit)
      {
         System.exit(1);
      }
      return isTrue;
   }

   /**
    * exitWithValidStatus : Void -> Void when called, program exits with exit
    * status 0.
    */
   public static void exitWithValidStatus()
   {
      System.exit(exitStatus);
   }
}