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
   static final int BIN_MATCH_COUNT = 435;
   static final double OVERLAP_RATIO = 31.0/32.0;
   static final int BIN_SIZE = 16384;
   /**
    * getLittleEndian : byte[], int, int -> long
    * 
    * @param arr
    *            : The array of type byte, the values of which will be
    *            converted to little endian
    * @param offset
    *            : The offset from where to start in 'arr'
    * @param numOfBytes
    *            : The number of indexes to convert
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
        for (int i = 0; i < numOfBytes; i++) {
            val = (val << 8) + (arr[--endIndex] & 0xFF);
        }
        return val;
    }
    
    static void printMatchAndExit(String fileName1, String fileName2, double firstOffset, double secondOffset) 
    {
        System.out.printf("MATCH " + fileName1 + " " + fileName2);
        System.out.printf(" %.1f %.1f%n", (Math.round(firstOffset*10))/10.0, Math.round(secondOffset*10)/10.0);
    }
	
    static void printNoMatchAndExit(String fileName1, String fileName2) 
    {
        System.out.println("NO MATCH " + fileName1 + " " + fileName2);
    }
        
    static void executeCommand(String... commandAndArgs)
    {
    	ProcessBuilder pb = new ProcessBuilder(commandAndArgs);
        try 
        {
            Process p = pb.start();
            BufferedReader reader =
	    			 new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String rd = null;
            if(DEBUG_MODE_ON)
            {
            	while ((rd = reader.readLine()) != null){
                	//System.out.println(rd);
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
    
    static void printMagnitudeLog(ArrayList<Double> magnitudes1, ArrayList<Double> magnitudes2)
    {
    	BufferedWriter outputWriter = null;
		try
		{
			outputWriter = new BufferedWriter(new FileWriter("log.txt"));
			int loopCounter = magnitudes1.size();
			ArrayList<Double> toIterate = magnitudes1;
			ArrayList<Double> toLessIterate = magnitudes2;
			
			if(loopCounter<magnitudes2.size())
			{
				loopCounter=magnitudes2.size();
				toIterate = magnitudes2;
				toLessIterate = magnitudes1;
			}
			int lessIterateSize = toLessIterate.size();
			for (int i = 0; i < loopCounter; i++) 
			{
			    outputWriter.write(toIterate.get(i)+ "  ");
			    if(i<lessIterateSize)
			    	outputWriter.write(toLessIterate.get(i)+"");
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
