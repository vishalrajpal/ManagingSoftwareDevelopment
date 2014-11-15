package com.msd;

public abstract class Utilities 
{
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
    protected static long getLittleEndian(byte[] arr, int offset, int numOfBytes) 
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
    
    protected static void printMatchAndExit(String fileName1, String fileName2) 
    {
        System.out.println("MATCH " + fileName1 + " " + fileName2);
    }
	
    protected static void printNoMatchAndExit(String fileName1, String fileName2) 
    {
        System.out.println("NO MATCH " + fileName1 + " " + fileName2);
    }
}
