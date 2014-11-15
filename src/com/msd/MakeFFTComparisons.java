package com.msd;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.sampled.AudioInputStream;

public class MakeFFTComparisons 
{
	public static int max_index = 0;
	public static double max_magnitude = 0;
	
	public static boolean compare(AudioProcessableFile a1, AudioProcessableFile a2)
	{
		return overlapSamples(a1, a2);
		//return false;
	}
	
	
	public static boolean overlapSamples(AudioProcessableFile a1, AudioProcessableFile a2)
	{
		double overlap = 31.0/32.0;
		
		long frameRateFile1 = a1.getFrameRate();
		int numberOfSamplesInWindowFile1 = 16384;//(int) Math.ceil((0.0116 * frameRateFile1));
		int numberOfSamplesToIncrFile1 = (int)(numberOfSamplesInWindowFile1 * (1 - overlap));
		int beginSampleFile1 = 0;
		int endSampleFile1 = beginSampleFile1 + numberOfSamplesInWindowFile1;
		long noOfSamplesInFile1 = (long) (a1.getDuration() * frameRateFile1);
		FileInputStream audioStreamFile1 = a1.getFileInputStream();
		ArrayList<Double> magnitudes1 = new ArrayList<Double>();
		ArrayList<Double> magnitudes2 = new ArrayList<Double>();
		
		long frameRateFile2 = a2.getFrameRate();
		int numberOfSamplesInWindowFile2 = (int) Math.ceil((0.0116 * frameRateFile2));
		int numberOfSamplesToIncrFile2 = (int)(numberOfSamplesInWindowFile2 * (1 - overlap));
		int beginSampleFile2 = 0;
		int endSampleFile2 = beginSampleFile2 + numberOfSamplesInWindowFile2;
		int noOfSamplesInFile2 = (int) (a2.getDuration() * frameRateFile2);
		FileInputStream audioStreamFile2 = a2.getFileInputStream();
		
		//ArrayList<ComplexNumber> totalBuffers = new ArrayList<ComplexNumber>();
		int magnitudeIndex = 0;
		do
		{
			max_index = 0;
			max_magnitude = 0;
			ArrayList<ComplexNumber> tempBuffersFile1 = getLimitedFFTSamples(audioStreamFile1, numberOfSamplesInWindowFile1, beginSampleFile1, endSampleFile1, magnitudes1);
			//System.out.println("\n");
			
			ArrayList<ComplexNumber> tempBuffersFile2 = getLimitedFFTSamples(audioStreamFile2, numberOfSamplesInWindowFile2, beginSampleFile2, endSampleFile2, magnitudes2);
			if(!magnitudes1.get(magnitudeIndex).equals(magnitudes2.get(magnitudeIndex)))
			{
				return false;
			}
			magnitudeIndex++;
			//totalBuffers.addAll(tempBuffers);
			
			beginSampleFile1+=numberOfSamplesToIncrFile1;
			endSampleFile1+=numberOfSamplesToIncrFile1;
			beginSampleFile2+=numberOfSamplesToIncrFile2;
			endSampleFile2+=numberOfSamplesToIncrFile2;
		}
		while(endSampleFile1<44100);
		return true;
		//System.out.println(totalBuffers.size());
	}
	
	public static ArrayList<ComplexNumber> getLimitedFFTSamples(FileInputStream audioFileInputStream, int numberOfSamples, int beginSample, int endSample, ArrayList<Double> magnitudes)
	{
		Float[] tempSamples = readLimitedSamples(audioFileInputStream, numberOfSamples, beginSample, endSample);
		Float[] windowedSamples = applyHanningWindow(tempSamples);
		int nearestPowerOfTwo = getNearestPowerOfTwo(windowedSamples.length);
		if(tempSamples.length != nearestPowerOfTwo)
		{
			windowedSamples = padArrayWithZeros(windowedSamples, nearestPowerOfTwo);
		}
		ArrayList<ComplexNumber> tempBuffers = performFFT(windowedSamples);
		magnitudes.add(max_magnitude);
		
		System.out.println("Max Value:"+max_magnitude);
		System.out.println("Max Index:"+max_index);
		max_index = 0;
		max_magnitude = 0;
		return tempBuffers;
	}
	
	public static Float[] readLimitedSamples(FileInputStream audioFileInputStream, int numberOfSamples, int beginSample, int endSample)
	{
		float toDivide = (float) (2 << 15);
		Float[] samples = new Float[numberOfSamples];
		byte[] fourByteArray = new byte[4];
		byte[] twoByteArray = new byte[2];
		for(int i=0;i<numberOfSamples;i++)
		{
			try 
			{
				audioFileInputStream.read(fourByteArray);
				samples[i] = Utilities.getLittleEndian(fourByteArray, 0, 4) / toDivide;
				//audioFileInputStream.skip(2);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return samples;
	}
	
	private static Float[] applyHanningWindow(Float[] samples)
	{
		int noOfSamples = samples.length;
		Float[] windowedSamples = new Float[noOfSamples];
		for(int i = 0; i<noOfSamples; i++)
		{
			float piTimesIndex = (float) Math.PI * i;
			float windowReal = samples[i] * 
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
	
	private static Float[] padArrayWithZeros (Float[] originalArray, int nearestPowerOfTwo)
	{
		Float tempArray[] = new Float[nearestPowerOfTwo];
		System.arraycopy(originalArray, 0, tempArray, 0, originalArray.length);
		for(int i = originalArray.length; i<nearestPowerOfTwo; i++)
		{
			tempArray[i] = 0.0f;
		}
		return tempArray;
	}
	
	private static ArrayList<ComplexNumber> performFFT(Float[] tempSamples)
	{
		int samplesLen = tempSamples.length;
		ArrayList<ComplexNumber> result = new ArrayList<ComplexNumber>(samplesLen);
		if(samplesLen == 1)
		{	
			result.add(ComplexNumbers.make(tempSamples[0], 0));
			return result;
		}
	   
		int samplesLenBy2 = tempSamples.length / 2;
	   
		// Even Samples
		Float[] evenSamples = new Float[samplesLenBy2];
		for (int sampleCount = 0; sampleCount < samplesLenBy2; sampleCount++) 
		{
			evenSamples[sampleCount] = tempSamples[(2 * sampleCount)];
		}
		ArrayList<ComplexNumber> evenFFTSamples = performFFT(evenSamples);
	   
		// Odd Samples
		Float[] oddSamples = new Float[samplesLenBy2];
		for (int sampleCount = 0; sampleCount < samplesLenBy2; sampleCount++) 
		{
			oddSamples[sampleCount] = tempSamples[(2 * sampleCount) + 1];
		}
		ArrayList<ComplexNumber> oddFFTSamples = performFFT(oddSamples);
	   
		//Combining the Even And Odd Samples
		for(int sampleCount = 0; sampleCount<samplesLenBy2; sampleCount++)
		{
			// 2*PI*i*k/n
			double nthRootOfUnity = (-2 * Math.PI * sampleCount) / samplesLen;
			ComplexNumber omega = ComplexNumbers.make(Math.cos(nthRootOfUnity),
					Math.sin(nthRootOfUnity));
			ComplexNumber omegaMulOdd = omega.multiply(oddFFTSamples.get(sampleCount));
			ComplexNumber test1 = evenFFTSamples.get(sampleCount).add(omegaMulOdd);
			ComplexNumber test2 = evenFFTSamples.get(sampleCount).subtract(omegaMulOdd); 
			result.add(sampleCount, test1); 
			result.add(test2);
			double magnitude = Math.sqrt((test1.getRealPart()*test1.getRealPart()) + (test1.getImaginaryPart()*test1.getImaginaryPart())); 
			if(magnitude > max_magnitude)
			{
				max_magnitude = magnitude;
				max_index = sampleCount;
			}
		}
		return result;
	}
}
