package com.msd;

public class Backup {

	/*public static boolean overlapSamples(AudioProcessableFile a1, AudioProcessableFile a2)
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
		int numberOfSamplesInWindowFile2 = 16384;//(int) Math.ceil((0.0116 * frameRateFile2));
		int numberOfSamplesToIncrFile2 = (int)(numberOfSamplesInWindowFile2 * (1 - overlap));
		int beginSampleFile2 = 0;
		int endSampleFile2 = beginSampleFile2 + numberOfSamplesInWindowFile2;
		int noOfSamplesInFile2 = (int) (a2.getDuration() * frameRateFile2);
		FileInputStream audioStreamFile2 = a2.getFileInputStream();
		
		//ArrayList<ComplexNumber> totalBuffers = new ArrayList<ComplexNumber>();
		
		do
		{
			//max_amplitude = 0;
			ArrayList<ComplexNumber> tempBuffersFile1 = getLimitedFFTSamples(audioStreamFile1, numberOfSamplesInWindowFile1, beginSampleFile1, endSampleFile1, magnitudes1);
			//System.out.println("\n");
			
			ArrayList<ComplexNumber> tempBuffersFile2 = getLimitedFFTSamples(audioStreamFile2, numberOfSamplesInWindowFile2, beginSampleFile2, endSampleFile2, magnitudes2);
			//totalBuffers.addAll(tempBuffers);
			
			beginSampleFile1+=numberOfSamplesToIncrFile1;
			endSampleFile1+=numberOfSamplesToIncrFile1;
			beginSampleFile2+=numberOfSamplesToIncrFile2;
			endSampleFile2+=numberOfSamplesToIncrFile2;
		}
		while(endSampleFile1<44100);
		return true;
		//System.out.println(totalBuffers.size());
	}*/
}
