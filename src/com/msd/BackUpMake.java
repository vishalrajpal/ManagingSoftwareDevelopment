package com.msd;

public class BackUpMake {
	
	
/*	package com.msd;

	import java.io.BufferedWriter;
	import java.io.FileInputStream;
	import java.io.FileWriter;
	import java.io.IOException;
	import java.util.ArrayList;

	public class MakeFFTComparisons 
	{
		private static double max_amplitude;
		double[] cos;
		double[] sin;
		public static boolean compare(AudioProcessableFile a1, AudioProcessableFile a2)
		{
			return overlapSamples(a1, a2);
			//return false;
		}
		
		public static ArrayList<Double> getMagnitudes(AudioProcessableFile a1)
		{
			double overlap = 31.0/32.0;
			long frameRate = a1.getFrameRate();
			int numberOfSamplesInWindow = 16384;//(int) Math.ceil((0.0116 * frameRateFile1));
			int numberOfSamplesToIncr = (int)(numberOfSamplesInWindow * (1 - overlap));
			int beginSample = 0;
			int endSample = beginSample + numberOfSamplesInWindow;
			long noOfSamplesInFile1 = (long) (a1.getDuration() * frameRate);
			FileInputStream audioStreamFile1 = a1.getFileInputStream();
			ArrayList<Double> magnitudes = new ArrayList<Double>();
			do
			{
				getLimitedFFTSamples(audioStreamFile1, numberOfSamplesInWindow, beginSample, endSample, magnitudes);
				beginSample+=numberOfSamplesToIncr;
				endSample+=numberOfSamplesToIncr;
			}
			while(endSample<noOfSamplesInFile1);
			return magnitudes;
		}
		
		public static boolean overlapSamples(AudioProcessableFile a1, AudioProcessableFile a2)
		{
			ArrayList<Double> magnitudes1 = getMagnitudes(a1);
			ArrayList<Double> magnitudes2 = getMagnitudes(a2);
			BufferedWriter outputWriter = null;
			try {
				outputWriter = new BufferedWriter(new FileWriter("log.txt"));
				for (int i = 0; i < magnitudes1.size(); i++) 
				{
				    outputWriter.write(magnitudes1.get(i)+ "  " + magnitudes2.get(i));
				    outputWriter.newLine();
				}
				outputWriter.flush();  
				outputWriter.close(); 
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		}
		
		
		public static ArrayList<ComplexNumber> getLimitedFFTSamples(FileInputStream audioFileInputStream, int numberOfSamples, int beginSample, int endSample, ArrayList<Double> magnitudes)
		{
			max_amplitude = 0;
			Float[] tempSamples = readLimitedSamples(audioFileInputStream, numberOfSamples, beginSample, endSample);
			Float[] windowedSamples = applyHanningWindow(tempSamples);
			int nearestPowerOfTwo = getNearestPowerOfTwo(windowedSamples.length);
			if(tempSamples.length != nearestPowerOfTwo)
			{
				windowedSamples = padArrayWithZeros(windowedSamples, nearestPowerOfTwo);
			}
			ArrayList<ComplexNumber> tempBuffers = performFFT(windowedSamples);
			return tempBuffers;
		}
		
		public static Float[] readLimitedSamples(FileInputStream audioFileInputStream, int numberOfSamples, int beginSample, int endSample)
		{
			float toDivide = (float) (2 << 15);
			Float[] samples = new Float[numberOfSamples];
			byte[] fourByteArray = new byte[4];
			//byte[] twoByteArray = new byte[2];
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
				if(sampleCount==0)
					continue;
				double amplitude = Math.sqrt((test1.getRealPart()*test1.getRealPart()) + (test1.getImaginaryPart()*test1.getImaginaryPart())); 
				if(amplitude>max_amplitude)
					max_amplitude = amplitude;
			}
			return result;
		}
	}
*/
	

}
