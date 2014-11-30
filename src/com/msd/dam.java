package com.msd;

public class dam 
{
   /**
    * static main : String[] -> void
    * @param args : The command line arguments
    * Effect: The program starts execution from this method.
    *         It validates the arguments against 
    */
   public static void main(String[] args)
   {
      String pattern = "-f|-d <pathname> -f|-d <pathname>";
      CommandLineArgsParser.validateCommand(args, pattern);
   }
}