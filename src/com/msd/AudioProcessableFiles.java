package com.msd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class AudioProcessableFiles
{
	public static AudioProcessableFile make(File fileToProcess)
	{
		if(fileToProcess!=null)
		{
			return getAudioProcessableFile(fileToProcess);
		}
		else
		{
			String exString = "Passed Invalid File Path.";
            AssertTests.assertTrue(exString, false);
            return null;
		}
	}
	
	public static AudioProcessableFile getAudioProcessableFile(File fileToProcess)
	{
		AudioProcessableFile fileToReturn = null;
		String filePath = fileToProcess.getPath();
		String modFilePath = filePath.toLowerCase();
		AudioProcessableFile processableFile = null;
		if(modFilePath.endsWith(".wav"))
		{
			processableFile= new WAVAudioProcessableFile(fileToProcess);
		}
		else if(modFilePath.endsWith(".mp3"))
		{
			//processableFile = new MP3AudioProcessableFile(filePath);
			File mp3File = new File(filePath);
			convertMP3ToWAVFile(processableFile, mp3File);
			File wavFile = new File("/tmp/"+mp3File.getName()+".wav");
			FileInputStream audioFileInputStream;
			try 
			{
				audioFileInputStream = new FileInputStream(wavFile);
			} 
			catch (FileNotFoundException e) 
			{
				AssertTests.assertTrue(filePath + " File not found", false);
	        }
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
	
	public static void convertMP3ToWAVFile(AudioProcessableFile processableFile, File mp3File)
	{
		String newFilePath = "/tmp/" + mp3File.getName() + ".wav";
		System.out.println("Looking for "+newFilePath+":mp3Path"+mp3File.getPath());
        //String lamePath = "/course/cs5500f14/bin/lame";
        String lamePath = "/usr/local/bin/lame";
        ProcessBuilder pb = new ProcessBuilder(lamePath,"--decode", mp3File.getPath(), 
        		newFilePath);
        try 
        {
            Process p = pb.start();
            BufferedReader reader =
	    			 new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((reader.readLine()) != null){}
            p.waitFor();
            p.destroy();
        } 
        catch (IOException e) 
        {
            AssertTests.assertTrue("Unable to convert file: "+mp3File.getPath(), false);
        } 
        catch (InterruptedException e) 
        {
        	AssertTests.assertTrue("Unable to convert file: "+mp3File.getPath(), false);
        }
	}

	/** Implementation of AudioProcessableFile ADT */
    private static abstract class AudioProcessableBase implements AudioProcessableFile 
    {
    	protected final String WAV_CONVERTER_PATH = "/course/cs5500f14/bin/wav";
    	protected final String LAME_CONVERTER_PATH = "/course/cs5500f14/bin/lame";
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
        
        protected void executeConverter(String converterPath, String paramsString)
        {
        	ProcessBuilder pb = new ProcessBuilder(converterPath,paramsString);
            try 
            {
                Process p = pb.start();
                BufferedReader reader =
    	    			 new BufferedReader(new InputStreamReader(p.getErrorStream()));
                while ((reader.readLine()) != null){}
                p.waitFor();
                p.destroy();
            } 
            catch (IOException e) 
            {
                AssertTests.assertTrue("Unable to convert file: "+audioFile.getPath(), false);
            } 
            catch (InterruptedException e) 
            {
            	AssertTests.assertTrue("Unable to convert file: "+audioFile.getPath(), false);
            }
        }
        
    }
	
    /** Create new AudioProcessableFile */
    private static class WAVAudioProcessableFile extends AudioProcessableBase {
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
        private int samplesPerFrame;
        private int frameLength;
        private double duration;
        private AudioProcessableFile mp3Processable = null;
        private boolean toConvertToMonoOrResample = false;
        private boolean toChangeBitWidth = false;
        /**
         * Constructor : String -> WAVAudioProcessableFile
         * 
         * @param filePath
         *            : The file path for which an AudioProcessableFile has to
         *            be created
         * @effect : The constructor implicitly returns an instance of type
         *         Create
         */
        WAVAudioProcessableFile(File fileToProcess) 
        {
            this.fileName = fileToProcess.getName();
            this.audioFile = fileToProcess;
            filePath = fileToProcess.getPath();
            fetchFileIntoFileInputStream();
            validateFile();
        }
        
        private void updateFile()
        {
        	this.audioFile = new File(filePath);
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
                String notSupportedFormatError = " is not a supported format";
                // First 4 bytes are 'RIFF'
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
                samplesPerFrame = bytesPerFrame / bytesPerSample;
                frameLength = (int) fileLength / bytesPerFrame;
                duration = (double) fileLength / 
                		(sampleRate * noOfChannels * bitsPerSample /8);
                
                if(toChangeBitWidth)
                {
                	filePath = "/tmp/"+audioFile.getName();
                	executeConverter(WAV_CONVERTER_PATH, 
                			getChangeBitWidthCommandParams(audioFile.getPath(), filePath));
                }
                
                if(toConvertToMonoOrResample)
                {
                	//./lame -a --resample 44.1 wavfiletoconvert16.wav
                	String updatedFilePath = filePath+".mp3";
                	executeConverter(LAME_CONVERTER_PATH, 
                			getResampleCommandParams(filePath, updatedFilePath));

                	filePath = updatedFilePath + ".wav";
                	executeConverter(LAME_CONVERTER_PATH, 
                			getDecodeCommandParams(updatedFilePath, filePath));
                }
                
                if(toChangeBitWidth || toConvertToMonoOrResample)
                	updateFile();
                
            } catch (IOException e) {
                AssertTests
                        .assertTrue(fileName + " Invalid File Header", false);
                return false;
            }
            return true;
        }

        private String getResampleCommandParams(String srcFile, String destFile)
        {
        	StringBuilder resampleSB = new StringBuilder("-a --resample 44.1 ");
        	resampleSB.append(srcFile);
        	resampleSB.append(" ");
        	resampleSB.append(destFile);
        	return resampleSB.toString();
        }
        
        private String getDecodeCommandParams(String srcFile, String destFile)
        {
        	StringBuilder decodeSB = new StringBuilder("--decode ");
        	decodeSB.append(srcFile);
        	decodeSB.append(" ");
        	decodeSB.append(destFile);
        	return decodeSB.toString();
        }
        
        private String getChangeBitWidthCommandParams(String srcFile, String destFile)
        {
        	StringBuilder changeBitWidthSB = new StringBuilder("-bitwidth 16 ");
        	changeBitWidthSB.append(srcFile);
        	changeBitWidthSB.append(" ");
        	changeBitWidthSB.append(destFile);
        	return changeBitWidthSB.toString();
        }
        /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
        public String getFileShortName() {
        	if(mp3Processable!=null)
        		return mp3Processable.getFileShortName();
            return audioFile.getName();
        }
        
        
        /*
         * @see AudioProcessableFiles.AudioProcessableBase#compare
         * (AudioProcessableFile)
         */
        public void compare(AudioProcessableFile fileToCmp) 
        {
            if (duration != fileToCmp.getDuration()) 
            {
            	Utilities.printNoMatchAndExit(getFileShortName(), fileToCmp.getFileShortName());
            }
            boolean areFilesSame = MakeFFTComparisons.compare(this, fileToCmp);
            if(areFilesSame)
            {
            	Utilities.printMatchAndExit(getFileShortName(), fileToCmp.getFileShortName());
            }
        }
        
        public double getDuration()
        {
        	return duration;
        }
    }
}