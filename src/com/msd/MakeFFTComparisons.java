package com.msd;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.rmi.CORBA.Util;

public class MakeFFTComparisons 
{
	private double max_magnitude;
	
	private static double[] cos = null;
	private static double[] sin = null;
	int n, m;
	
	private static void computeCosSinTables(int n)
	{
		if(cos==null || n!=cos.length)
		{
			cos = new double[n/2];
			sin = new double[n/2];
			for(int i=0; i<n/2; i++) 
			{
				cos[i] = Math.cos(-2*Math.PI*i/n);
			    sin[i] = Math.sin(-2*Math.PI*i/n);
		    }
		}
	}
	
	public ArrayList<Double> getMagnitudes(AudioProcessableFile a1)
	{
		long frameRate = a1.getFrameRate();
		int numberOfSamplesToIncr = (int)(Utilities.BIN_SIZE * (1 - Utilities.OVERLAP_RATIO));
		int beginSample = 0;
		int endSample = beginSample + Utilities.BIN_SIZE;
		int noOfSamplesInFile1 = (int) (a1.getDuration() * frameRate);
		FileInputStream audioStreamFile1 = a1.getFileInputStream();
		ArrayList<Double> magnitudes = new ArrayList<Double>();
		ArrayList<Double> fileSamples = new ArrayList<Double>();
		do
		{
			readSamples(audioStreamFile1, fileSamples, Utilities.BIN_SIZE);
			getLimitedFFTSamples(fileSamples, magnitudes);
			beginSample+=numberOfSamplesToIncr;
			endSample+=numberOfSamplesToIncr;
			fileSamples.subList(0, numberOfSamplesToIncr).clear();
		}
		while(endSample<noOfSamplesInFile1);
		
		if((noOfSamplesInFile1-beginSample)>0)
		{
			readSamples(audioStreamFile1, fileSamples, (noOfSamplesInFile1-beginSample));
			getLimitedFFTSamples(fileSamples, magnitudes);
		}
		return magnitudes;
	}
	
	public void readSamples(FileInputStream audioFileInputStream, ArrayList<Double> samples, int endSample)
	{
		float toDivide = (float) (2 << 15);
		byte[] fourByteArray = new byte[2];
		for(int i=samples.size();i<endSample;i++)
		{
			try 
			{
				audioFileInputStream.read(fourByteArray);
				samples.add((double)Utilities.getLittleEndian(fourByteArray, 0, 2) / toDivide);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	public void getLimitedFFTSamples(ArrayList<Double> samples, ArrayList<Double> magnitudes)
	{
		max_magnitude = 0;
		double[] windowedSamples= applyHanningWindow(samples);
		int nearestPowerOfTwo = getNearestPowerOfTwo(windowedSamples.length);
		if(samples.size() != nearestPowerOfTwo)
		{
			windowedSamples = padArrayWithZeros(windowedSamples, nearestPowerOfTwo);
		}
		computeCosSinTables(windowedSamples.length);
		this.n = windowedSamples.length;
		this.m = (int)(Math.log(n) / Math.log(2));
		double imagArray[] = new double[windowedSamples.length];
		fft(windowedSamples, imagArray);
		//magnitudes.add(Math.round(Math.log(max_magnitude) * 1000)/1000.00);
		magnitudes.add(Math.log(max_magnitude));
	}
	
	private static double[] applyHanningWindow(ArrayList<Double> samples)
	{
		int noOfSamples = samples.size();
		double[] windowedSamples = new double[noOfSamples];
		for(int i = 0; i<noOfSamples; i++)
		{
			float piTimesIndex = (float) Math.PI * i;
			double windowReal = samples.get(i) * 
					(0.5f + 0.5f * (float) Math.cos(2.0f * piTimesIndex / noOfSamples));
			windowedSamples[i]=windowReal;
		}
		return windowedSamples;
	}
	
	private static int getNearestPowerOfTwo(int n)
	{
		if((n & (n-1)) == 0)
			return n;
	   
		for(int i = 1; i<32; i*=2)
		{
			n |= (n >> i);
		}
		return n + 1;
	}
	
	private static double[] padArrayWithZeros (double[] originalArray, int nearestPowerOfTwo)
	{
		double tempArray[] = new double[nearestPowerOfTwo];
		System.arraycopy(originalArray, 0, tempArray, 0, originalArray.length);
		for(int i = originalArray.length; i<nearestPowerOfTwo; i++)
		{
			tempArray[i] = 0.00;
		}
		return tempArray;
	}
	
	/***************************************************************
	* fft.c
	* Douglas L. Jones 
	* University of Illinois at Urbana-Champaign 
	* January 19, 1992 
	* http://cnx.rice.edu/content/m12016/latest/
	* 
	*   fft: in-place radix-2 DIT DFT of a complex input 
	* 
	*   input: 
	* n: length of FFT: must be a power of two 
	* m: n = 2**m 
	*   input/output 
	* x: double array of length n with real part of data 
	* y: double array of length n with imag part of data 
	* 
	*   Permission to copy and use this program is granted 
	*   as long as this header is included. 
	****************************************************************/
	public void fft(double[] x, double[] y)
	{
		int i,j,k,n1,n2,a;
		double c,s,e,t1,t2;
		// Bit-reverse
		j = 0;
		n2 = n/2;
		for (i=1; i < n - 1; i++) 
		{
			n1 = n2;
			while ( j >= n1 ) 
			{
				j = j - n1;
				n1 = n1/2;
			}
			j = j + n1;
	  
			if (i < j) 
			{
				t1 = x[i];
				x[i] = x[j];
				x[j] = t1;
		        t1 = y[i];
		        y[i] = y[j];
		        y[j] = t1;
		     }
		}
		// FFT
		n1 = 0;
		n2 = 1;
		
	    for (i=0; i < m; i++) 
	    {
	    	n1 = n2;
	    	n2 = n2 + n2;
	    	a = 0;
	   
	    	for (j=0; j < n1; j++) 
	    	{
	    		c = cos[a];
	    		s = sin[a];
	    		a +=  1 << (m-i-1);
	    		for (k=j; k < n; k=k+n2) 
	    		{
	    			t1 = c*x[k+n1] - s*y[k+n1];
	    			t2 = s*x[k+n1] + c*y[k+n1];
	    			x[k+n1] = x[k] - t1;
	    			y[k+n1] = y[k] - t2;
	    			x[k] = x[k] + t1;
	    			y[k] = y[k] + t2;
	    			double mag1 = Math.sqrt(x[k]*x[k] + y[k]*y[k]);
	    			double mag2 = Math.sqrt(x[k+n1]*x[k+n1] + y[k+n1]*y[k+n1]);
	    			int retVal = Double.compare(mag1, max_magnitude);
	    			if(retVal>0)
	    				max_magnitude=mag1;
	    			retVal = Double.compare(mag2, max_magnitude);
	    			if(retVal>0)
	    				max_magnitude=mag2;
	    		}
	    	}
	     }
	}
}