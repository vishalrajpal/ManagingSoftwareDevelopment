package com.msd;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CommandLineArgsParser 
{   	
   /**
    * validateCommand : String[], String -> void
    * @param args : the command line arguments
    * @param pattern : The pattern against which to evaluate 'args'
    * @effect: Validates the 'args' against the 'pattern', 
    * If not valid prints standard error and exits with status other than 0.
    * Assumptions: Splits the pattern by " "
    * a) the index at which the '-' appears in the pattern and args should be
    * the same 
    * b) when a '<' is encountered, the corresponding index is ignored in args 
    */
   static void validateCommand(String[] args, String pattern)
   {
      boolean validArgs = true;
      String[] splitPattern = pattern.split(" ");
      int argsLen = args.length;
      if(splitPattern.length != argsLen)
      {
         validArgs = false;
      }
      else
      {
         for(int argCounter = 0; argCounter<argsLen; argCounter++)
         {
            String currentArg = args[argCounter];
            String patternArg = splitPattern[argCounter];
            if(patternArg.charAt(0) == '-')
            {
    	       ArrayList<String> allowedParams = new 
    	    		 ArrayList<String>(Arrays.asList(patternArg.split("\\|")));
          
    	       if(!allowedParams.contains(currentArg))
    	       {
    	          validArgs = false;
    	          break;
               }
            }
         }
      }
      AssertTests.assertTrue("incorrect command line", validArgs, true);
      if(validArgs)
      {
    	  CompareFiles cf = new CompareFiles(args);
    	  cf.generateComparisions();
      }
   }
   
   private static class CompareFiles 
   {
      Map<String, AudioProcessableFile> filesProcessedPath1;
      Map<String, AudioProcessableFile> filesProcessedPath2;
      private final String TEMP_DIR_1_PATH = "/tmp/TempDir1";
      private final String TEMP_DIR_2_PATH = "/tmp/TempDir2";
      private final String[] args;
      
      /**
   	* CompareFiles: String[] -> void
   	* @param args : the command line arguments
   	* @effect: Processes the path provided in the command line, if valid and
   	* prints the appropriate message.
   	*/
      CompareFiles(String[] args)
      {
    	  this.args = args;
      }
      
      public void generateComparisions()
      {
    	  File[] firstPathFiles;
       	  File[] secondPathFiles;
       	  firstPathFiles = parseArgAndPath(args[0], args[1]);
       	  secondPathFiles = parseArgAndPath(args[2], args[3]);
             filesProcessedPath1 = new HashMap<String, AudioProcessableFile>();
       	  filesProcessedPath2 = new HashMap<String, AudioProcessableFile>();
       	  Utilities.executeCommand("mkdir", TEMP_DIR_1_PATH);
             Utilities.executeCommand("mkdir", TEMP_DIR_2_PATH);
       	  compareAllFiles(firstPathFiles,secondPathFiles);	
      }
      
      /**
   	* parseArgAndPath: String String -> File[]
   	* @param arg : the first/third string in the command line which helps
   	* us identify if the path provided is a file or a directory.
   	* @param path : the path corresponding to the arg.
   	* @return : a File[] array that contains all the files present 
   	* corresponding to the given path, if a directory path is given, then
   	* all the files(success only if the file is .wav or .mp3) are stored in 
   	* an array and if its a file then only 1 file is stored in the array
   	* (after checking its format and header)
       */
      private File[] parseArgAndPath(String arg, String path)
      {
         File[] initArr = new File[1];
   	  if(arg.equals("-d"))
   	  {
   	     initArr = validateDirAndGetFiles(path);
   	  }
   	  else
         {
   	     try
            {
   		    File getFile = new File(path);
   		    initArr[0] = getFile;
   		 }
   		 catch(Exception e)
   		 {
   		    String exString = "Invalid File Path : "+ arg;
   		    AssertTests.assertTrue(exString, false);
            }
         }
   	  return initArr;
      }
   	  
      /**
   	* validateDirAndGetFiles: Sting -> File[]
   	* @param : path corresponding to -d
   	* @return : checks if the path corresponds to a directory, if yes 
   	* gets all the files present in the directory.
   	*/
      private File[] validateDirAndGetFiles(String path)
      {
   	  File dirOfFiles;
   	  File[] listOfFiles;
   	  if(path != null)
   	  {
   	     dirOfFiles = new File(path);
   	     if(!dirOfFiles.isDirectory())
   	     {
   		    AssertTests.assertTrue(path+":Invalid Directory", false,true);
   		    return null;
   		 }
   	     listOfFiles = dirOfFiles.listFiles();
   	  }
   	  else
         {
            AssertTests.assertTrue(path+":Invalid Directory", false,true);
   		 return null;
         }
   	  return listOfFiles;
      }	  
      
      /**
   	* compareAllFiles: File[] File[] -> void
   	* @param: firstPathNameFiles, files corresponding to the first pathname 
   	* @param: secondPathNameFiles, files corresponding to the second
   	* pathname
   	* @effect: Converts the files to AudioProcessableFile type and compares
   	* each AudioProcessableFile obtained from the first path with each
   	* AudioProcessableFile obtained from the second path. 
   	*/
      private void compareAllFiles(File[] firstPathNameFiles,
   		   File[] secondPathNameFiles)
      {
   	  int NoOfFilesInPath1 = firstPathNameFiles.length;
   	  int NoOfFilesInPath2 = secondPathNameFiles.length;
   	  String tmpDir1FilePath = TEMP_DIR_1_PATH+"/";
   	  String tmpDir2FilePath = TEMP_DIR_2_PATH+"/";
   	  for(int path1Count=0; path1Count<NoOfFilesInPath1; path1Count++)
   	  {
   	     File currentFile = firstPathNameFiles[path1Count];
   		 AudioProcessableFile path1File = 
   				 getProcessableFile(currentFile, tmpDir1FilePath,filesProcessedPath1);
   		 if(path1File==null) continue;
   		 for(int path2Count=0; path2Count<NoOfFilesInPath2;path2Count++)
   		 {
   		    File currentSecondFile = secondPathNameFiles[path2Count];
   			AudioProcessableFile path2File = 
   					getProcessableFile(currentSecondFile, tmpDir2FilePath, filesProcessedPath2);
   			if(path2File==null) continue;
   			if(Utilities.DEBUG_MODE_ON)
   			{
   			   System.out.println("Compare:"+currentFile.getName() 
   					   +" & "+currentSecondFile.getName());
   			   Calendar rightNow = Calendar.getInstance();
   			   System.out.println(rightNow.get(rightNow.MINUTE)+":"
   			   +rightNow.get(rightNow.SECOND));
   			}
   			path1File.compare(path2File);
   			if(Utilities.DEBUG_MODE_ON)
   			{
   		       Calendar rightNow1 = Calendar.getInstance();
   	           System.out.println(rightNow1.get(rightNow1.MINUTE)+":"
   		       +rightNow1.get(rightNow1.SECOND));
   		    }
   		 }
   	  }
   	  Utilities.executeCommand("rm", "-r", TEMP_DIR_1_PATH);
   	  Utilities.executeCommand("rm", "-r", TEMP_DIR_2_PATH);
         AssertTests.exitWithValidStatus();
      }
   	 
      private AudioProcessableFile getProcessableFile 
      (File fileToProcess, String tmpDirPath, Map<String, AudioProcessableFile> dirMap)
      {
         AudioProcessableFile f = 
       		  dirMap.get(fileToProcess.getPath());
   	  if(f == null)
   	  {
   	     f = AudioProcessableFiles.make(fileToProcess, tmpDirPath);
   	     dirMap.put(fileToProcess.getPath(), f);
   	  }
   	  return f;
      }
      
      /**
   	* getProcessableFile: String -> AudioProcessableFile
   	* @param filePath: Place where the required file is stored
   	* @return: AudioProcessibleFile of the given file, also the file 
   	* is added to the HashMap. // Memoization
   	*/

   }
}