package com.msd;

import java.io.FileInputStream;
import java.util.ArrayList;

public interface AudioProcessableFile
{
   /**
    * validateFile: -> void
    * 
    * @effect: Validates the file to check if it is one of the supported 
    * formats
    */
   void validateFile();

   /**
    * compare : AudioProcessableFile -> void
    * 
    * @param fileToCmp
    *           : The AudioProcessableFile to compare
    * @effect: Compares this and 'fileToCmp' in two steps a) Compares the 
    *          length of two files. If same proceeds to Step 2 b) Compares the
    *          FFT result of both files through Mean Squared Error If MSE is
    *          equal to 0 the files are same and a 'MATCH' message is printed
    *          through Standard output else a 'NO MATCH' message is printed In
    *          either case the program exits with a status of 0.
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
    * @return long : returns the FileInputStream for the File represented by 
    * the implementation of this interface
    */
   FileInputStream getFileInputStream();

   /**
    * getMagnitudes : -> ArrayList<Double>
    * 
    * @return ArrayList<Double> : returns the peak of every magnitude bin as
    * an ArrayList
    */
   ArrayList<Double> getMagnitudes();
   
   AudioProcessableFile getWAVAudioProcessableFile();
}