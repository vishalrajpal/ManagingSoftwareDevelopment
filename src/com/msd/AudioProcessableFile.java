package com.msd;

import java.io.FileInputStream;
import java.util.ArrayList;

public interface AudioProcessableFile
{
   /**
    * validateFile: -> void
    * 
    * @effect: Validates the file to check if it is one of the supported formats
    */
   void validateFile();

   /**
    * compare : AudioProcessableFile -> void
    * 
    * @param fileToCmp
    *           : The AudioProcessableFile to compare
    * @effect: Compares this and 'fileToCmp' in two steps a) Generates the
    *          magnitudes of the samples and stores peak of every bin b)
    *          Compares the peak magnitudes of two files through
    *          LongestCommonSubString algorithm and MATCH is printed if the
    *          match is equal to or more than 5 seconds (435 bins)
    */
   void compare(AudioProcessableFile fileToCmp);

   /**
    * getFileShortName : -> String
    * 
    * @return String : The short name of the file corresponding to this
    */
   String getFileShortName();

   /**
    * isValidFile : -> boolean
    * 
    * @return boolean : Determines if the file is valid or not
    */
   boolean isValidFile();

   /**
    * getDuration : -> double
    * 
    * @return double : returns the duration of the file in seconds
    */
   double getDuration();

   /**
    * getFrameRate : -> long
    * 
    * @return long : returns the frame rate of the file
    */
   long getFrameRate();

   /**
    * getFileInputStream : -> FileInputStream
    * 
    * @return long : returns the FileInputStream for the File represented by the
    *         implementation of this interface
    */
   FileInputStream getFileInputStream();

   /**
    * getMagnitudes : -> ArrayList<Double>
    * 
    * @return ArrayList<Double> : returns the peak of every bin as an ArrayList
    */
   ArrayList<Double> getMagnitudes();

   /**
    * getWAVAudioProcessableFile : -> AudioProcessableFile
    * 
    * @return AudioProcessableFile : It returns a WAV file of the canonical
    *         format
    */
   AudioProcessableFile getWAVAudioProcessableFile();
}