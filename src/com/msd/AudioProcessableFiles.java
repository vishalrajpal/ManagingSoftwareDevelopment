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
   private final static String LAME_CONVERTER_PATH = 
		   "/usr/local/bin/lame";
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
         File wavFile = convertMP3ToWAVFile(fileToProcess, tmpDirPath);
         processableFile = new WAVAudioProcessableFile(wavFile, tmpDirPath,
               fileToProcess.getName());
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
      public abstract boolean validateFile();

      /* @see AudioProcessableFile#compare(AudioProcessableFile) */
      public abstract void compare(AudioProcessableFile ap);

      /* @see AudioProcessableFiles.AudioProcessableBase#getFileShortName() */
      public abstract String getFileShortName();

      /* @see AudioProcessableFile#getDuration() */
      public abstract double getDuration();

      /* @see AudioProcessableFile#getMagnitudes() */
      public abstract ArrayList<Double> getMagnitudes();

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
      public boolean validateFile()
      {
         if (!isValidFile())
            return false;
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
            noOfChannels = (int) Utilities
                  .getLittleEndian(arrayFor2Bytes, 0, 2);
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
               return isValidFile;
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
            if (!isValidFile)
               return isValidFile;
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
            return false;
         }
         return true;
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
}