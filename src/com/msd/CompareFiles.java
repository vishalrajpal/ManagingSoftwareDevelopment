package com.msd;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class: CompareFiles: This class processes the paths of files/directories 
 * provided to us, gets those files and stores it in an array. The 2 array's 
 * that we get (1 array for the first path provided, if valid and the 2nd 
 * array for the second path, if valid) are then converted to 
 * AudioProcessableFiles type and stored in a 
 * HashMap<String, AudioProcessableFile> where the String corresponds to the
 * file path and AudioProcessableFile is the file present following the path.
 * We then compare the files and print the appropriate message.
 * 
 *
 */

public class CompareFiles 
{
	 Map<String, AudioProcessableFile> filesProcessedPath1;
	 Map<String, AudioProcessableFile> filesProcessedPath2;
	 
	 /**
	  * CompareFiles: String[] -> void
	  * @param args : the command line arguments
	  * @effect: Processes the path provided in the command line, if valid and
	  * prints the appropriate message.
	  */
	 CompareFiles(String[] args)
	 {
		  File[] firstPathFiles;
		  File[] secondPathFiles;
		  firstPathFiles = parseArgAndPath(args[0], args[1]);
		  secondPathFiles = parseArgAndPath(args[2], args[3]);
		  filesProcessedPath1 = new HashMap<String, AudioProcessableFile>();
		  filesProcessedPath2 = new HashMap<String, AudioProcessableFile>();
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
				    AssertTests.assertTrue(path+":Invalid Directory", false, 
						   true);
		    	 return null;
			   }
			   listOfFiles = dirOfFiles.listFiles();
		  }
		  else
		  {
			   AssertTests.assertTrue(path+":Invalid Directory", false, 
					   true);
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
	 private void compareAllFiles(File[] firstPathNameFiles,File[] secondPathNameFiles)
	 {
		  int NoOfFilesInPath1 = firstPathNameFiles.length;
		  int NoOfFilesInPath2 = secondPathNameFiles.length;
		  for(int path1Count=0; path1Count<NoOfFilesInPath1; path1Count++)
		  {
			   File currentFile = firstPathNameFiles[path1Count];
			   AudioProcessableFile path1File = getDirOneProcessableFile(currentFile);
			   if(path1File==null)
				   continue;
			   for(int path2Count=0; path2Count<NoOfFilesInPath2;path2Count++)
			   {
				    File currentSecondFile = secondPathNameFiles[path2Count];
				    AudioProcessableFile path2File = 
						  getProcessableFile2(currentSecondFile);
				    System.out.println("Compare:"+currentFile.getName() +" & "+currentSecondFile.getName());
				    if(path2File==null)
					    continue;
				    Calendar rightNow = Calendar.getInstance();
					System.out.println(rightNow.get(rightNow.MINUTE)+":"+rightNow.get(rightNow.SECOND));

				    path1File.compare(path2File);
				    
					Calendar rightNow1 = Calendar.getInstance();
					System.out.println(rightNow1.get(rightNow1.MINUTE)+":"+rightNow1.get(rightNow1.SECOND));

			   }
		  }
		  deleteAllMp3Files(filesProcessedPath1);
		  deleteAllMp3Files(filesProcessedPath2);
		  AssertTests.exitWithValidStatus();
	 }
	 
	 /**
	  * getProcessableFile: String -> AudioProcessableFile
	  * @param filePath: Place where the required file is stored
	  * @return: AudioProcessibleFile of the given file, also the file 
	  * is added to the HashMap.
	  */
	 private AudioProcessableFile getDirOneProcessableFile(File fileToProcess)
	 {
		 AudioProcessableFile f = filesProcessedPath1.get(fileToProcess.getPath());
	 	 if(f == null)
	 	 {
			   f = AudioProcessableFiles.make(fileToProcess);
			   filesProcessedPath1.put(fileToProcess.getPath(), f);
		  }
		  return f;
	 }
	 
	 private AudioProcessableFile getProcessableFile2(File fileToProcess)
	 {
		 AudioProcessableFile f = filesProcessedPath2.get(fileToProcess.getPath());
	 	 if(f == null)
	 	 {
			   f = AudioProcessableFiles.make(fileToProcess);
			   filesProcessedPath2.put(fileToProcess.getPath(), f);
		  }
		  return f;
	 }
	 
	 /**
	 * deleteAllMp3Files : -> void
	 * @effect: Deletes all the files created in the '/tmp' directory
	 */
	 private void deleteAllMp3Files(Map<String, AudioProcessableFile> filesProcessed)
	 {
		  Iterator<Entry<String, AudioProcessableFile>> mapIterator = 
				  filesProcessed.entrySet().iterator();
		  while(mapIterator.hasNext())
		  {
			   Map.Entry<String, AudioProcessableFile> currentPath = 
					   (Entry<String, AudioProcessableFile>) mapIterator.next();
			
			   if(currentPath.getKey().endsWith(".mp3"))
			   {
				    AudioProcessableFile currentProc = currentPath.getValue();
				    if(currentProc!=null)
				    {
					     String toDeleteFilePath = 
							      "/tmp/"+currentProc.getFileShortName();
					     ProcessBuilder pb = new ProcessBuilder("rm",toDeleteFilePath);
					     try 
					     {
					    	 Process p = pb.start();
					    	 BufferedReader reader =
					    			 new BufferedReader(new InputStreamReader(p.getInputStream()));
					    			 while ((reader.readLine()) != null) {}
					    	 p.waitFor();
					     } 
					     catch (IOException e) 
					     {
					    	 AssertTests.assertTrue("Unable to delete file: "+toDeleteFilePath, false);
					     } 
					     catch (InterruptedException e) 
					     {
					    	 AssertTests.assertTrue("Unable to delete file: "+toDeleteFilePath, false); 
				    	 }
				    }
			   }		
		  }
	 }
}
