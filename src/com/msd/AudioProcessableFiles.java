package com.msd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public abstract class AudioProcessableFiles
{
	protected final static String WAV_CONVERTER_PATH = "/course/cs5500f14/bin/wav";
	protected final static String LAME_CONVERTER_PATH = "/usr/local/bin/lame";
	
	public static AudioProcessableFile make(File fileToProcess, String tmpDirPath)
	{
		if(fileToProcess!=null)
		{
			return getAudioProcessableFile(fileToProcess, tmpDirPath);
		}
		else
		{
			String exString = "Passed Invalid File Path.";
            AssertTests.assertTrue(exString, false);
            return null;
		}
	}
	
	public static AudioProcessableFile getAudioProcessableFile(File fileToProcess, String tmpDirPath)
	{
		AudioProcessableFile fileToReturn = null;
		String filePath = fileToProcess.getPath();
		String modFilePath = filePath.toLowerCase();
		AudioProcessableFile processableFile = null;
		if(modFilePath.endsWith(".wav"))
		{
			processableFile= new WAVAudioProcessableFile(fileToProcess, tmpDirPath, fileToProcess.getName());
		}
		else if(modFilePath.endsWith(".mp3"))
		{
			File wavFile = convertMP3ToWAVFile(fileToProcess, tmpDirPath);
			processableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath, fileToProcess.getName());
		}
		else
		{
			String exString = "File Format not found : " + filePath;
            AssertTests.assertTrue(exString, false);
		}
		
		if(processableFile!=null)
		{
			fileToReturn = processableFile;
		}
		return fileToReturn;
	}
	
	public static File convertMP3ToWAVFile(File mp3File, String tmpDirPath)
	{
		String updatedFilePath = tmpDirPath+mp3File.getName();
    	Utilities.executeCommand(LAME_CONVERTER_PATH, "-a", "--resample", "44.1", mp3File.getPath(), updatedFilePath);
    	String newFilePath = updatedFilePath + ".wav";
    	Utilities.executeCommand(LAME_CONVERTER_PATH,"--decode", updatedFilePath, newFilePath);
    	return new File(newFilePath);
	}
	
	

	/** Implementation of AudioProcessableFile ADT */
    private static abstract class AudioProcessableBase implements AudioProcessableFile 
    {
        protected boolean isValidFile = true;
        protected float[] samples = null;
        protected FileInputStream audioFileInputStream;
        protected File audioFile;
        protected String fileName;
        protected long sampleRate;
        protected String filePath;
        
        /* @see AudioProcessableFile#validateFile() */
        public abstract boolean validateFile();

        /* @see AudioProcessableFile#compare(AudioProcessableFile) */
        public abstract void compare(AudioProcessableFile ap);

        /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
        public abstract String getFileShortName();

        public abstract double getDuration();
        
        public abstract ArrayList<Double> getMagnitudes();
        
        public boolean isValidFile() 
        {
            return isValidFile;
        }
        
        public long getFrameRate()
        {
        	return sampleRate;
        }
        
        public FileInputStream getFileInputStream()
        {
        	return audioFileInputStream;
        }
        
        /**
         * fetchFileIntoFileInputStream : -> void
         * 
         * @effect: Creates and loads a new FileInputStream with the file
         *          located at 'filePath'. If file not found throws
         *          FileNotFound Exception.
         */
        protected void fetchFileIntoFileInputStream() 
        {
            try 
            {
                audioFileInputStream = new FileInputStream(audioFile);
            } 
            catch (FileNotFoundException e) 
            {
                AssertTests.assertTrue(fileName + " File not found", false);
                isValidFile = false;
            }
        }
        
    }
	
    /** Create new AudioProcessableFile */
    private static class WAVAudioProcessableFile extends AudioProcessableBase 
    {
        // Constants
        private final static int RIFF_HEXA_EQUIVALENT = 0x46464952;
        private final static int WAVE_HEXA_EQUIVALENT = 0x45564157;
        private final static int fmt_HEXA_EQUIVALENT = 0x20746D66;
        private final static int data_HEXA_EQUIVALENT = 0x61746164;
        private final static int AUDIO_FORMAT_EQUIVALENT = 1;
        private final static int STEREO_EQUIVALENT = 2;
        private final static int MONO_EQUIVALENT = 1;
        private final static int WAVE_SAMPLING_RATE_11025 = 11025;
        private final static int WAVE_SAMPLING_RATE_22050 = 22050;
        private final static int WAVE_SAMPLING_RATE_44100 = 44100;
        private final static int WAVE_SAMPLING_RATE_48000 = 48000;
        private final static int BITS_PER_SAMPLE_8 = 8;
        private final static int BITS_PER_SAMPLE_16 = 16;

        // Instance Variables
        private long fileLength;
        private int bitsPerSample;
        private int bytesPerSample;
        private int noOfChannels;
        private int bytesPerFrame;
        private double duration;
        private boolean toConvertToMonoOrResample = false;
        private boolean toChangeBitWidth = false;
        private String tmpDirPath;
        private String tmpFilePath;
        private ArrayList<Double> magnitudes = null;
        /**
         * Constructor : String -> WAVAudioProcessableFile
         * 
         * @param filePath
         *            : The file path for which an AudioProcessableFile has to
         *            be created
         * @effect : The constructor implicitly returns an instance of type
         *         Create
         */
        WAVAudioProcessableFile(File fileToProcess, String tmpDirPath, String fileName) 
        {
            this.fileName = fileName;
            this.audioFile = fileToProcess;
            this.filePath = fileToProcess.getPath();
            this.tmpDirPath = tmpDirPath;
            this.tmpFilePath = this.filePath;
            fetchFileIntoFileInputStream();
            validateFile();
        }
        
        private void updateFile()
        {
        	this.audioFile = new File(tmpFilePath);
        	fetchFileIntoFileInputStream();
        }
        
        /* @see AudioProcessableFiles.AudioProcessableBase#validateFile() */
        public boolean validateFile() 
        {
        	if(!isValidFile())
        		return false;
            byte[] arrayFor2Bytes = new byte[2];
            byte[] arrayFor4Bytes = new byte[4];
            try {
            	// First 4 bytes are 'RIFF'
            	String notSupportedFormatError = " is not a supported format";
                audioFileInputStream.read(arrayFor4Bytes);
                String riffErr = fileName + notSupportedFormatError;
                long riffLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
                isValidFile = AssertTests.assertTrue(riffErr,
                        riffLitEnd == RIFF_HEXA_EQUIVALENT);
                if (!isValidFile)
                    return isValidFile;
                // Skip the chunkSize
                audioFileInputStream.skip(4);

                // These 4 bytes should be WAVE'
                audioFileInputStream.read(arrayFor4Bytes);
                String waveErr = fileName + notSupportedFormatError;
                long waveLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
                isValidFile = AssertTests.assertTrue(waveErr,
                        waveLitEnd == WAVE_HEXA_EQUIVALENT);
                if (!isValidFile)
                    return isValidFile;
                // These 4 bytes should be 'fmt '
                audioFileInputStream.read(arrayFor4Bytes);
                String fmtError = fileName + " The chunk should be type fmt";
                long fmtLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
                isValidFile = AssertTests.assertTrue(fmtError,
                        fmtLitEnd == fmt_HEXA_EQUIVALENT);
                if (!isValidFile)
                    return false;
                // Skip the chunkSize
                audioFileInputStream.skip(4);

                // The AudioFormat should be 1 i.e. PCM (Linear Quantization)
                audioFileInputStream.read(arrayFor2Bytes);
                String pcmError = fileName
                        + " The Audio Format should be of type PCM";
                long pcmLitEnd = Utilities.getLittleEndian(arrayFor2Bytes, 0, 2);
                isValidFile = AssertTests.assertTrue(pcmError,
                        pcmLitEnd == AUDIO_FORMAT_EQUIVALENT);
                if (!isValidFile)
                    return isValidFile;
                // These 2 bytes should mention number of channels & should be
                // 2(Stereo) or 1(Mono)
                audioFileInputStream.read(arrayFor2Bytes);
                String noOfChanError = fileName
                        + " The audio should be of type Stereo or Mono";
                noOfChannels = (int) Utilities.getLittleEndian(arrayFor2Bytes, 0, 2);
                isValidFile = AssertTests.assertTrue(noOfChanError,
                        noOfChannels == STEREO_EQUIVALENT
                                || noOfChannels == MONO_EQUIVALENT);
                if (!isValidFile)
                    return isValidFile;
                // The Sample rate should be 11.025kHz or 22.05kHz or 44.1kHz
                // or 48kHz
                audioFileInputStream.read(arrayFor4Bytes);
                String samRtError = fileName + "The sampling rate should be "
                        + "11.025 kHz or 22.05 kHz or 44.1 kHz or 48kHz";
                sampleRate = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
                isValidFile = AssertTests.assertTrue(samRtError,
                		sampleRate == WAVE_SAMPLING_RATE_11025
                                || sampleRate == WAVE_SAMPLING_RATE_22050
                                || sampleRate == WAVE_SAMPLING_RATE_44100
                                || sampleRate == WAVE_SAMPLING_RATE_48000);
                if (!isValidFile)
                    return isValidFile;
                
                if(sampleRate != WAVE_SAMPLING_RATE_44100 || 
                		noOfChannels == STEREO_EQUIVALENT)
                {
                	toConvertToMonoOrResample = true;
                }
                
                // Skip the ByteRate(4 Bytes) and BlockAlign(2 Bytes)
                audioFileInputStream.skip(6);

                // Bits per Sample should be 8 or 16
                audioFileInputStream.read(arrayFor2Bytes);
                String bitError = fileName
                        + " There should be 8 or 16 bits/sample";
                bitsPerSample = (int) Utilities.getLittleEndian(arrayFor2Bytes, 0, 2);
                isValidFile = AssertTests.assertTrue(bitError,
                		(bitsPerSample == BITS_PER_SAMPLE_8 
                		|| bitsPerSample == BITS_PER_SAMPLE_16));
                if (!isValidFile)
                    return isValidFile;
                if(bitsPerSample != BITS_PER_SAMPLE_16)
                	toChangeBitWidth = true;
                bytesPerSample = bitsPerSample / 8;

                // The data chunk gets started and should start with 'data' for
                // 4 bytes
                audioFileInputStream.read(arrayFor4Bytes);
                String dataError = fileName
                        + " There should be a proper data chunk";
                long dataLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
                isValidFile = AssertTests.assertTrue(dataError,
                        dataLitEnd == data_HEXA_EQUIVALENT);
                if (!isValidFile)
                    return isValidFile;
                // The next 4 bytes determine the length of the data chunk
                audioFileInputStream.read(arrayFor4Bytes);
                fileLength = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);

                bytesPerFrame = bytesPerSample * noOfChannels;
                duration = (double) fileLength / 
                		(sampleRate * noOfChannels * bitsPerSample /8);
                
                if(toChangeBitWidth)
                {
                	tmpFilePath = tmpDirPath+audioFile.getName();
                	Utilities.executeCommand(WAV_CONVERTER_PATH, "-bitwidth", "16", filePath, tmpFilePath);
                }
                
                if(toConvertToMonoOrResample)
                {
                	String updatedFilePath = tmpDirPath+audioFile.getName()+".mp3";
                	Utilities.executeCommand(LAME_CONVERTER_PATH,"-a", "--resample", "44.1", tmpFilePath, updatedFilePath);
                	tmpFilePath = updatedFilePath + ".wav";
                	Utilities.executeCommand(LAME_CONVERTER_PATH,"--decode", updatedFilePath, tmpFilePath);
                }
                
                if(toChangeBitWidth || toConvertToMonoOrResample)
                	updateFile();
                
            } 
            catch (IOException e) 
            {
                AssertTests
                        .assertTrue(fileName + " Invalid File Header", false);
                return false;
            }
            return true;
        }

        /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
        public String getFileShortName() 
        {
        	return fileName;
        }
        
        /*
         * @see AudioProcessableFiles.AudioProcessableBase#compare
         * (AudioProcessableFile)
         */
        public void compare(AudioProcessableFile fileToCmp) 
        {
        	int longestMatchSubSequenceLength = Utilities.longestCommonSubLen(getMagnitudes(), fileToCmp.getMagnitudes());
    		if(Utilities.DEBUG_MODE_ON)
    		{
    			Utilities.printMagnitudeLog(getMagnitudes(), fileToCmp.getMagnitudes());
    			System.out.println(longestMatchSubSequenceLength);
    		}
    		if(longestMatchSubSequenceLength>Utilities.binThreshholdMatch)
    			Utilities.printMatchAndExit(getFileShortName(), fileToCmp.getFileShortName());
        }
        
        public double getDuration()
        {
        	return duration;
        }
        
        public ArrayList<Double> getMagnitudes()
        {
        	if(magnitudes==null)
        	{
        		MakeFFTComparisons mf = new MakeFFTComparisons();
        		magnitudes = mf.getMagnitudes(this);
        	}
        	return magnitudes;
        }
    }
}