package com.msd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public abstract class AudioProcessableFiles
{
   private final static String WAV_CONVERTER_PATH = 
		   "/course/cs5500f14/bin/wav";
   private final static String LAME_CONVERTER_PATH = "/usr/local/bin/lame";
		   //"/course/cs5500f14/bin/lame";
   private final static String OGG_CONVERTER_PATH = "/usr/local/bin/oggdec";
   /**
    * make : File, String -> AudioProcessableFile
    * @param fileToProcess : The File to process and this File will be ready
    * for comparison
    * @param tmpDirPath : The temperory directory path if needed to modify 
    * this file though eith wav or lame converter
    * @return AudioProcessableFile : The AudioProcessableFile represting the 
    * given File
    */
   public static AudioProcessableFile make(File fileToProcess,
         String tmpDirPath)
   {
      if (fileToProcess != null)
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
   
   /**
    * getAudioProcessableFile : File, String -> AudioProcessableFile
    * @param fileToProcess : The File to process and this File will be ready
    * for comparison
    * @param tmpDirPath : The temperory directory path if needed to modify 
    * this file though eith wav or lame converter
    * @return AudioProcessableFile : The AudioProcessableFile represting the 
    * given File
    */
   public static AudioProcessableFile getAudioProcessableFile(
         File fileToProcess, String tmpDirPath)
   {
      AudioProcessableFile fileToReturn = null;
      String filePath = fileToProcess.getPath();
      String modFilePath = filePath.toLowerCase();
      AudioProcessableFile processableFile = null;
      if (modFilePath.endsWith(".wav"))
      {
         processableFile = new WAVAudioProcessableFile(fileToProcess,
               tmpDirPath, fileToProcess.getName());
      } 
      else if (modFilePath.endsWith(".mp3"))
      {
    	  /*File wavFile = convertMP3ToWAVFile(fileToProcess, tmpDirPath);
          processableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath,
                fileToProcess.getName());*/
    	 processableFile = new MP3AudioProcessableFile(fileToProcess, tmpDirPath);
    	 if(processableFile.isValidFile())
    	 {
    		 processableFile = processableFile.getWAVAudioProcessableFile();
    	 }
      } 
      else if (modFilePath.endsWith(".ogg"))
      {
/*    	  File wavFile = convertOGGToWAVFile(fileToProcess, tmpDirPath);
          processableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath,
                fileToProcess.getName());*/
    	  processableFile = new OGGAudioProcessableFile(fileToProcess, tmpDirPath);
     	 if(processableFile.isValidFile())
     	 {
     		 processableFile = processableFile.getWAVAudioProcessableFile();
     	 }
      }
    		
      else
      {
         String exString = "File Format not found : " + filePath;
         AssertTests.assertTrue(exString, false);
      }

      if (processableFile != null && processableFile.isValidFile())
      {
         fileToReturn = processableFile;
      }
      return fileToReturn;
   }
   
   
   static File convertMP3ToWAVFile(File mp3File, String tmpDirPath)
   {
      String updatedFilePath = tmpDirPath + mp3File.getName();
      Utilities.executeCommand(LAME_CONVERTER_PATH, "-a", "--resample", "44.1",
            mp3File.getPath(), updatedFilePath);
      String newFilePath = updatedFilePath + ".wav";
      Utilities.executeCommand(LAME_CONVERTER_PATH, "--decode",
            updatedFilePath, newFilePath);
      return new File(newFilePath);
   }
   
   static File convertOGGToWAVFile(File oggFile, String tmpDirPath)
   {
	   String newFilePath = tmpDirPath + oggFile.getName() + ".wav";
	   Utilities.executeCommand(OGG_CONVERTER_PATH, oggFile.getPath(), "-b", 
			   "16", "-o", newFilePath);
	   return new File(newFilePath);
   }

   /** Implementation of AudioProcessableFile ADT */
   private static abstract class AudioProcessableBase implements
         AudioProcessableFile
   {
      protected boolean isValidFile = true;
      protected float[] samples = null;
      protected FileInputStream audioFileInputStream;
      protected File audioFile;
      protected String fileName;
      protected long sampleRate;
      protected String filePath;

      /* @see AudioProcessableFile#validateFile() */
      public abstract void validateFile();

      /* @see AudioProcessableFile#compare(AudioProcessableFile) */
      public abstract void compare(AudioProcessableFile ap);

      /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
      public abstract String getFileShortName();

      /* @see AudioProcessableFile#getDuration() */
      public abstract double getDuration();

      /* @see AudioProcessableFile#getMagnitudes() */
      public abstract ArrayList<Double> getMagnitudes();

      public abstract AudioProcessableFile getWAVAudioProcessableFile();
      
      /* @see AudioProcessableFile#isValidFile() */      
      public boolean isValidFile()
      {
         return isValidFile;
      }

      /* @see AudioProcessableFile#getFrameRate() */
      public long getFrameRate()
      {
         return sampleRate;
      }

      /* @see AudioProcessableFile#getFrameRate() */
      public FileInputStream getFileInputStream()
      {
         return audioFileInputStream;
      }

      /**
       * fetchFileIntoFileInputStream : -> void
       * 
       * @effect: Creates and loads a new FileInputStream with the file located
       *          at 'filePath'. If file not found throws FileNotFound
       *          Exception.
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
       *           : The file path for which an AudioProcessableFile has to be
       *           created
       * @effect : The constructor implicitly returns an instance of type 
       * Create
       */
      WAVAudioProcessableFile(File fileToProcess, String tmpDirPath,
            String fileName)
      {
         this.fileName = fileName;
         this.audioFile = fileToProcess;
         this.filePath = fileToProcess.getPath();
         this.tmpDirPath = tmpDirPath;
         this.tmpFilePath = this.filePath;
         fetchFileIntoFileInputStream();
         validateFile();
      }

      /**
       * updateFile : -> void
       * 
       * @effect: Upddates the FileInputStream for the File represented by this
       * to tmpFile after lame/wav conversion
       */
      private void updateFile()
      {
         this.audioFile = new File(tmpFilePath);
         fetchFileIntoFileInputStream();
      }

      /* @see AudioProcessableFiles.AudioProcessableBase#validateFile() */
      public void validateFile()
      {
         if (!isValidFile)
            return;
         byte[] arrayFor2Bytes = new byte[2];
         byte[] arrayFor4Bytes = new byte[4];
         try
         {
            // First 4 bytes are 'RIFF'
            String notSupportedFormatError = " is not a supported format";
            audioFileInputStream.read(arrayFor4Bytes);
            String riffErr = fileName + notSupportedFormatError;
            long riffLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
            isValidFile = AssertTests.assertTrue(riffErr,
                  riffLitEnd == RIFF_HEXA_EQUIVALENT);
            if (!isValidFile)
               return;
            // Skip the chunkSize
            audioFileInputStream.skip(4);

            // These 4 bytes should be WAVE'
            audioFileInputStream.read(arrayFor4Bytes);
            String waveErr = fileName + notSupportedFormatError;
            long waveLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
            isValidFile = AssertTests.assertTrue(waveErr,
                  waveLitEnd == WAVE_HEXA_EQUIVALENT);
            if (!isValidFile)
               return;
            // These 4 bytes should be 'fmt '
            audioFileInputStream.read(arrayFor4Bytes);
            String fmtError = fileName + " The chunk should be type fmt";
            long fmtLitEnd = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
            isValidFile = AssertTests.assertTrue(fmtError,
                  fmtLitEnd == fmt_HEXA_EQUIVALENT);
            if (!isValidFile)
               return;
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
               return;
            // These 2 bytes should mention number of channels & should be
            // 2(Stereo) or 1(Mono)
            audioFileInputStream.read(arrayFor2Bytes);
            String noOfChanError = fileName
                  + " The audio should be of type Stereo or Mono";
            noOfChannels = (int) Utilities
                  .getLittleEndian(arrayFor2Bytes, 0, 2);
            isValidFile = AssertTests.assertTrue(noOfChanError,
                  noOfChannels == STEREO_EQUIVALENT
                        || noOfChannels == MONO_EQUIVALENT);
            if (!isValidFile)
               return;
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
               return;

            if (sampleRate != WAVE_SAMPLING_RATE_44100
                  || noOfChannels == STEREO_EQUIVALENT)
            {
               toConvertToMonoOrResample = true;
            }

            // Skip the ByteRate(4 Bytes) and BlockAlign(2 Bytes)
            audioFileInputStream.skip(6);

            // Bits per Sample should be 8 or 16
            audioFileInputStream.read(arrayFor2Bytes);
            String bitError = fileName + " There should be 8 or 16 bits/sample";
            bitsPerSample = (int) Utilities.getLittleEndian(arrayFor2Bytes, 0,
                  2);
            isValidFile = AssertTests
                  .assertTrue(
                        bitError,
                        (bitsPerSample == BITS_PER_SAMPLE_8 || 
                        bitsPerSample == BITS_PER_SAMPLE_16));
            
            if (!isValidFile)
               return;
            
            if (bitsPerSample != BITS_PER_SAMPLE_16)
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
            
            // The next 4 bytes determine the length of the data chunk
            audioFileInputStream.read(arrayFor4Bytes);
            fileLength = Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);

            bytesPerFrame = bytesPerSample * noOfChannels;
            duration = (double) fileLength
                  / (sampleRate * noOfChannels * bitsPerSample / 8);

            if (toChangeBitWidth)
            {
               tmpFilePath = tmpDirPath + audioFile.getName();
               Utilities.executeCommand(WAV_CONVERTER_PATH, "-bitwidth", "16",
                     filePath, tmpFilePath);
            }

            if (toConvertToMonoOrResample)
            {
               String updatedFilePath = tmpDirPath + audioFile.getName()
                     + ".mp3";
               Utilities.executeCommand(LAME_CONVERTER_PATH, "-a",
                     "--resample", "44.1", tmpFilePath, updatedFilePath);
               tmpFilePath = updatedFilePath + ".wav";
               Utilities.executeCommand(LAME_CONVERTER_PATH, "--decode",
                     updatedFilePath, tmpFilePath);
            }

            if (toChangeBitWidth || toConvertToMonoOrResample)
               updateFile();

         } 
         catch (IOException e)
         {
            AssertTests.assertTrue(fileName + " Invalid File Header", false);
         }
         return;
      }

      /* @see AudioProcessableFiles.AudioProcessableBase#compare */
      public void compare(AudioProcessableFile fileToCmp)
      {
         if(Utilities.DEBUG_MODE_ON)
         {
            Utilities.printMagnitudeLog(getMagnitudes(),
                  fileToCmp.getMagnitudes());
         }
         compareLongestSubString(getMagnitudes(), fileToCmp.getMagnitudes(),
               fileToCmp);
      }

      /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
      public String getFileShortName()
      {
         return fileName;
      }
      
      /* @see AudioProcessableFiles.AudioProcessableBase#getDuration() */
      public double getDuration()
      {
         return duration;
      }

      /* @see AudioProcessableFiles.AudioProcessableBase#getMagnitudes() */
      public ArrayList<Double> getMagnitudes()
      {
         if (magnitudes == null)
         {
            MakeFFTComparisons mf = new MakeFFTComparisons();
            magnitudes = mf.getMagnitudes(this);
         }
         return magnitudes;
      }
      
      public AudioProcessableFile getWAVAudioProcessableFile()
      {
    	  return this;
      }
      /**
       * compareLongestSubString : -> ArrayList<Double>, ArrayList<Double>,
       *                              AudioProcessableFile
       * 
       * @effect: Determines the length of longest common matching indexes,
       * of the bin magnitudes, also determines the time of the starting bin,
       * if the length of the longext common sequence is more than 350
       * (5 seconds), prints MATCH. 
       *   
       */
      private void compareLongestSubString(ArrayList<Double> magnitudes1,
            ArrayList<Double> magnitudes2, AudioProcessableFile fileToCmp)
      {
         int lenOfStr1 = magnitudes1.size();
         int lenOfStr2 = magnitudes2.size();
         int arraysize = Math.max(lenOfStr1, lenOfStr2);
         int[] prevRow = new int[arraysize + 1];
         int[] currRow = new int[arraysize + 1];

         float epsilon = 0.05f;
         int longestSubStringLength = 0;
         int beginIndex1 = -1;
         int beginIndex2 = -1;

         for (int i = 1; i <= lenOfStr1; i++)
         {
            double list1CurrentMag = magnitudes1.get(i - 1);
            for (int j = 1; j <= lenOfStr2; j++)
            {
               if(Math.abs(list1CurrentMag - magnitudes2.get(j - 1)) 
                     < epsilon)
               {
                  int curVal = 1 + prevRow[j - 1];
                  currRow[j] = curVal;
                  if (curVal > longestSubStringLength)
                  {
                     longestSubStringLength = curVal;
                     beginIndex1 = i - longestSubStringLength;
                     beginIndex2 = j - longestSubStringLength;
                  }
               } 
               else
               {
                  currRow[j] = 0;
               }
            }
            System.arraycopy(currRow, 0, prevRow, 0, arraysize + 1);
         }

         if (Utilities.DEBUG_MODE_ON)
         {
            System.out.println("Common Length" + longestSubStringLength);
            System.out.println("Begin Index1:" + beginIndex1);
            System.out.println("Begin Index2:" + beginIndex2);
         }
         int noOfUniqueSamplesInOneBin = (int) (Utilities.BIN_SIZE * 
               (1 - Utilities.OVERLAP_RATIO));
         float firstOffset = (beginIndex1 * noOfUniqueSamplesInOneBin)
               / (float) WAVE_SAMPLING_RATE_44100;
         float secondOffset = (beginIndex2 * noOfUniqueSamplesInOneBin)
               / (float) WAVE_SAMPLING_RATE_44100;
         
         if(longestSubStringLength >= Utilities.BIN_MATCH_COUNT)
         {
            Utilities.printMatchAndExit(getFileShortName(),
                  fileToCmp.getFileShortName(), firstOffset, secondOffset);
         }
      }
   }

   private static class MP3AudioProcessableFile extends AudioProcessableBase
   {
	   private final static int VERSION_1 = 0x08;
	   private final static int LAYER_3 = 0x02;
	   private AudioProcessableFile wavProcessableFile = null;
	   
	   MP3AudioProcessableFile(File fileToProcess, String tmpDirPath)
	   {
		   this.audioFile = fileToProcess;
		   fetchFileIntoFileInputStream();
		   validateFile();
		   if(isValidFile)
		   {
			   File wavFile = convertMP3ToWAVFile(fileToProcess, tmpDirPath);
			   wavProcessableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath,
	               fileToProcess.getName());
		   }
	   }

	
	public void validateFile() {
		if (!isValidFile())
            return;	
		byte[] arrayFor1Bytes = new byte[1];
        try
        {
           audioFileInputStream.skip(1);
           audioFileInputStream.read(arrayFor1Bytes);
           int nextByte = (int) Utilities.getLittleEndian(arrayFor1Bytes, 0, 1);
           String versionError = "MP3 file should be version 1";
           isValidFile = AssertTests.assertTrue(versionError,
        		   (nextByte & 0x08) == VERSION_1);
        
           if(!isValidFile)
        	   return;
           
           String layerError = "MP3 file should be Layer 3";
           isValidFile = AssertTests.assertTrue(layerError, 
        		   (nextByte & 0x02) == LAYER_3);
        }
        catch (IOException e)
        {
        	AssertTests.assertTrue(fileName + " Invalid File Header", false);
        }
        return;
	}
	

	@Override
	public void compare(AudioProcessableFile ap) 
	{
		wavProcessableFile.compare(ap);
	}

	@Override
	public String getFileShortName() 
	{
		return wavProcessableFile.getFileShortName();
	}

	@Override
	public double getDuration() 
	{
		return wavProcessableFile.getDuration();
	}

	@Override
	public ArrayList<Double> getMagnitudes() 
	{
		return wavProcessableFile.getMagnitudes();
	}
	   
	public AudioProcessableFile getWAVAudioProcessableFile()
    {
  	  return wavProcessableFile;
    }
	   /**
	    * convertMP3ToWAVFile : File, String -> File
	    * @param mp3File : The mp3File which is to be converted
	    * @param tmpDirPath : The temperory directory path in which the temperory
	    * file will be created
	    * @return File : The temperory File after converting the given original
	    * File 
	    */
	   static File convertMP3ToWAVFile(File mp3File, String tmpDirPath)
	   {
	      String updatedFilePath = tmpDirPath + mp3File.getName();
	      Utilities.executeCommand(LAME_CONVERTER_PATH, "-a", "--resample", "44.1",
	            mp3File.getPath(), updatedFilePath);
	      String newFilePath = updatedFilePath + ".wav";
	      Utilities.executeCommand(LAME_CONVERTER_PATH, "--decode",
	            updatedFilePath, newFilePath);
	      return new File(newFilePath);
	   }
   }

   private static class OGGAudioProcessableFile extends AudioProcessableBase
   {
	   private AudioProcessableFile wavProcessableFile = null;
	   private static final int Oggs_HEXA_EQUIVALENT = 0X5367674F;
	   private static final int vor_HEXA_EQUIVALENT = 0X726F76;
	   private static final int bis_HEXA_EQUIVALENT = 0X736962;
	   OGGAudioProcessableFile(File fileToProcess, String tmpDirPath)
	   {
		   this.audioFile = fileToProcess;
		   fetchFileIntoFileInputStream();
		   validateFile();
		   if(isValidFile)
		   {
			   File wavFile = convertOGGToWAVFile(fileToProcess, tmpDirPath);
			   wavProcessableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath,
	               fileToProcess.getName());
		   }
	   }

	   public void validateFile()
	   {
		   if (!isValidFile())
	            return;	
			byte[] arrayFor4Bytes = new byte[4];
			byte[] arrayFor3Bytes = new byte[3];
	        try
	        {
	        	String oggsError = "Improper Ogg file";
	           audioFileInputStream.read(arrayFor4Bytes);
	           int oggsValue = (int) Utilities.getLittleEndian(arrayFor4Bytes, 0, 4);
	           isValidFile = AssertTests.assertTrue(oggsError, 
	        		   oggsValue == Oggs_HEXA_EQUIVALENT);
	           
	           if(!isValidFile)
	        	   return;
	           
	           audioFileInputStream.skip(25);
	           
	           String vorbisError = "The .ogg file should be of vorbis format";
	           audioFileInputStream.read(arrayFor3Bytes);
	           int vorVal = (int) Utilities.getLittleEndian(arrayFor3Bytes, 0, 3);
	           isValidFile = AssertTests.assertTrue(vorbisError, 
	        		   vorVal == vor_HEXA_EQUIVALENT);
	           
	           if(!isValidFile)
	        	   return;
	           
	           audioFileInputStream.read(arrayFor3Bytes);
	           int bisVal = (int) Utilities.getLittleEndian(arrayFor3Bytes, 0, 3);
	           isValidFile = AssertTests.assertTrue("Vor Error", 
	        		   bisVal == bis_HEXA_EQUIVALENT);
	        }
	        catch (IOException e)
	        {
	        	AssertTests.assertTrue(fileName + " Invalid File Header", false);
	        }
	        return;
	   }
	   
	   public void compare(AudioProcessableFile ap) 
	   {
			wavProcessableFile.compare(ap);
	   }

		@Override
		public String getFileShortName() 
		{
			return wavProcessableFile.getFileShortName();
		}

		@Override
		public double getDuration() 
		{
			return wavProcessableFile.getDuration();
		}

		@Override
		public ArrayList<Double> getMagnitudes() 
		{
			return wavProcessableFile.getMagnitudes();
		}
		   
		public AudioProcessableFile getWAVAudioProcessableFile()
	    {
	  	  return wavProcessableFile;
	    }

	   static File convertOGGToWAVFile(File oggFile, String tmpDirPath)
	   {
		   String newFilePath = tmpDirPath + oggFile.getName() + ".wav";
		   Utilities.executeCommand(OGG_CONVERTER_PATH, oggFile.getPath(), "-b", 
				   "16", "-o", newFilePath);
		   return new File(newFilePath);
	   }
   }
}