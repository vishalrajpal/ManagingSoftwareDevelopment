package com.msd;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineArgsParser 
{   	
   /**
    * validateCommand : String[], String -> void
    * @param args : the command line arguments
    * @param pattern : The pattern against which to evaluate 'args'
    * @effect: Validates the 'args' against the 'pattern', 
    * If not valid prints standard error and exits with status other than 0.
    * Assumptions: Splits the pattern by " "
    * a) the index at which the '-' appears in the pattern and args should be
    * the same 
    * b) when a '<' is encountered, the corresponding index is ignored in args 
    */
   static void validateCommand(String[] args, String pattern)
   {
      boolean validArgs = true;
      String[] splitPattern = pattern.split(" ");
      int argsLen = args.length;
      if(splitPattern.length != argsLen)
      {
         validArgs = false;
      }
      else
      {
         for(int argCounter = 0; argCounter<argsLen; argCounter++)
         {
            String currentArg = args[argCounter];
            String patternArg = splitPattern[argCounter];
            if(patternArg.charAt(0) == '-')
            {
    	       ArrayList<String> allowedParams = new 
    	    		 ArrayList<String>(Arrays.asList(patternArg.split("\\|")));
          
    	       if(!allowedParams.contains(currentArg))
    	       {
    	          validArgs = false;
    	          break;
               }
            }
         }
      }
      AssertTests.assertTrue("incorrect command line", validArgs, true);
   }
}