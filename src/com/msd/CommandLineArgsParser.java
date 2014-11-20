package com.msd;

import java.util.ArrayList;
import java.util.Arrays;

public class CommandLineArgsParser 
{
   /**
    * static validateCommand : String[], String -> void
    * @param args : The arguments passed to the command line
    * @param pattern : The pattern against which to evaluate 'args'
    * @effect: Validates the 'args' against the 'pattern', 
    * If not valid prints standard error and exits with status other than 0.
    */
   public static void validateCommand(String[] args, String pattern)
   {
      CommandLineArgsParser cp = new CommandLineArgsParser();
      cp.validateCommandLineArgs(args, pattern);
   }
    	
   /**
    * validateCommandLineArgs : String[] -> void
    * @param args : the command line arguments
    * @effect: Validates the 'args' against the instance variable 'pattern', 
    * If not valid prints standard error and exits with status other than 0.
    * Assumptions: Splits the pattern by " "
    * a) if any splitted sub pattern has '-',
    *    assumes that the args at that index should be same as this sub pattern
    * b) if any splitted sub pattern has '<'
    *    assumes that it is a value and does not checks it  
    */
   private void validateCommandLineArgs(String[] args, String pattern)
   {
      boolean validArgs = true;
      String[] splittedPattern = pattern.split(" ");
      int argsLen = args.length;
      if(splittedPattern.length != argsLen)
      {
         validArgs = false;
      }
      else
      {
         for(int argCounter = 0; argCounter<argsLen; argCounter++)
         {
            String currentArg = args[argCounter];
            String patternArg = splittedPattern[argCounter];
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