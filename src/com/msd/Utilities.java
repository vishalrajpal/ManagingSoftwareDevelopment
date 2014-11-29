package com.msd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public abstract class Utilities
{
   static final boolean DEBUG_MODE_ON = false;
   static final int BIN_SIZE = 16384;
   static final double OVERLAP_RATIO = 31.0 / 32.0;
   //435 * 512 = 222720
   //222720 / 44100 = 5.05
   static final int BIN_MATCH_COUNT = 435;
   
   
   

   /**
    * getLittleEndian : byte[], int, int -> long
    * 
    * @param arr
    *           : The array of type byte, the values of which will be converted
    *           to little endian
    * @param offset
    *           : The offset from where to start in 'arr'
    * @param numOfBytes
    *           : The number of indexes to convert
    * @return val : The little endian value of the values in the arr starting
    *         from 'offset' and ending at 'numOfBytes' from 'offset'
    */
   static long getLittleEndian(byte[] arr, int offset, int numOfBytes)
   {
      numOfBytes--;
      int endIndex = offset + numOfBytes;
      long val = 0;
      if (endIndex > arr.length)
         return val;

      val = arr[endIndex] & 0xFF;
      for (int i = 0; i < numOfBytes; i++)
      {
         val = (val << 8) + (arr[--endIndex] & 0xFF);
      }
      return val;
   }

   static long convertBytesToLong(byte[] arr, int shift, byte individualMask)
   {
	   	long val = 0;
	   	int len = arr.length;
   		for (int i = 0; i < len; i++) 
   		{
   			val = (val << shift) + (arr[i] & individualMask);
   		}
   		return val;
   }
   /**
    * printMatchAndExit : String String double double -> void
    * 
    * @param fileName1 : The file name of the first compared file
    * @param fileName2 : The file name of the second compared file
    *          
    * @param firstOffset : The starting offset in first file in seconds
    * @param secondOffset : The starting offset in second file in seconds
    * @effect : Prints MATCH with filenames and their offsets in time of the
    * matching audio to Standard output
    */
   static void printMatchAndExit(String fileName1, String fileName2,
         double firstOffset, double secondOffset)
   {
      System.out.printf("MATCH " + fileName1 + " " + fileName2);
      System.out.printf(" %.1f %.1f%n", (Math.round(firstOffset * 10)) / 10.0,
            Math.round(secondOffset * 10) / 10.0);
   }

   /**
    * executeCommand : String... -> void
    * 
    * @param commandAndArgs : The command to be executed for a process
    * 
    * @effect : Executes a new process with 'commandArgs' as its parameter/s
    */
   static void executeCommand(String... commandAndArgs)
   {
      ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
      try
      {
         Process p = pb.start();
         BufferedReader reader = new BufferedReader(new InputStreamReader(
               p.getErrorStream()));
         String rd = null;
         if (DEBUG_MODE_ON)
         {
            while ((rd = reader.readLine()) != null)
            {
               System.out.println(rd);
            }
         } 
         else
         {
            while ((rd = reader.readLine()) != null){};
         }
         p.waitFor();
         p.destroy();
      } 
      catch (IOException e)
      {
         AssertTests.assertTrue("Unable to execute lame/wav: ", false);
      } 
      catch (InterruptedException e)
      {
         AssertTests.assertTrue("Unable to execute lame/wav", false);
      }
   }

   /**
    * printMagnitudeLog : ArrayList<Double> ArrayList<Double> -> void
    * 
    * @param magnitudes1 : The ArrayList containing peaks of magnitude bins 
    * for first file
    * @param magnitudes2 : The ArrayList containing peaks of magnitude bins
    * for second file
    * 
    * @effect : Prints the magnitudes to a 'log.txt' file, used for debugging
    */
   static void printMagnitudeLog(ArrayList<Double> magnitudes1,
         ArrayList<Double> magnitudes2)
   {
      BufferedWriter outputWriter = null;
      try
      {
         outputWriter = new BufferedWriter(new FileWriter("log.txt"));
         int loopCounter = magnitudes1.size();
         ArrayList<Double> toIterate = magnitudes1;
         ArrayList<Double> toLessIterate = magnitudes2;

         if (loopCounter < magnitudes2.size())
         {
            loopCounter = magnitudes2.size();
            toIterate = magnitudes2;
            toLessIterate = magnitudes1;
         }
         int lessIterateSize = toLessIterate.size();
         for (int i = 0; i < loopCounter; i++)
         {
            outputWriter.write(toIterate.get(i) + "  ");
            if (i < lessIterateSize)
               outputWriter.write(toLessIterate.get(i) + "");
            outputWriter.newLine();
         }
         outputWriter.flush();
         outputWriter.close();
      } 
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
}