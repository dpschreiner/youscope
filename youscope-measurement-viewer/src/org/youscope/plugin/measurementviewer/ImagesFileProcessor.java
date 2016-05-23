/**
 * 
 */
package org.youscope.plugin.measurementviewer;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Helper class to parse the images.csv file present in every measurement folder and return a tree representing the wells, positions and imaging jobs.
 * @author Moritz Lang
 *
 */
class ImagesFileProcessor
{
	private static final String SEPARATOR = ":\n \t\n\t:";
	
	public static ImageFolderNode processImagesFile(File imagesFile) throws Exception
	{
		// Hashmap to store the image data in while parsing the CSV file.
		final HashMap<String,ImageList> imageFolders = new HashMap<String,ImageList>();
		
		
		// Open the CSV file
		LineNumberReader lineReader = null;
		try
		{
			lineReader = new LineNumberReader(new FileReader(imagesFile));
			int lineNumber = 1;
			
			// Read header line of the CSV file.
			String line = lineReader.readLine();
			if(line == null)
			{
				// do nothing. Means not a single image was saved...
			}
			
			// Parse the CSV file
			try
			{
				while(true)
		        {
					lineNumber++;
		    		line = lineReader.readLine();
		            if (line == null)
		            {
		                break;
		            }
		            
		            // Read CSV entry
		            String[] tokens = line.split(";");
		            if(tokens.length == 1)
		            {
		            	// Probably empty line at the end of the file.
		            	break;
		            }
		            else if(tokens.length != 12)
		            {
		            	throw new Exception("Line does not contain exactly 11 elements. Probably incompatible versions of YouScope used for creating the measurement and viewing it now.");
		            }
		            
		            // normalize tokens
		            for(int i=0; i< tokens.length; i++)
		            {
		            	tokens[i] = tokens[i].trim();
		            	if(tokens[i].charAt(0) == '"' && tokens[i].charAt(tokens[i].length() - 1) == '"')
		            		tokens[i] = tokens[i].substring(1, tokens[i].length()-1);
		            }
		            
		            // Get the map entry for the given well, position and jobID. If it does not exist, yet, create it.
		            String imageFolderID = tokens[4]+SEPARATOR+ tokens[5]+SEPARATOR+ tokens[7];
		            ImageList imageFolder = imageFolders.get(imageFolderID);
		            if(imageFolder == null)
		            {
		            	imageFolder = new ImageList();
		            	imageFolders.put(imageFolderID, imageFolder);
		            }
		            
		            // Add image
		            String[] evalStrings = tokens[0].split(".");
		            int[] evals = new int[evalStrings.length];
		            for(int k=0; k<evals.length; k++)
		            {
		            	try
		            	{
		            		evals[k] = Integer.parseInt(evalStrings[k]);
		            	}
		            	catch(NumberFormatException e)
		            	{
		            		throw new Exception("String identifying evaluation number ("+tokens[0]+") must contain integers separated by a dot.", e);
		            	}
		            }
		            
		            imageFolder.add(tokens[6], evals);
		        }
			}
			catch(Exception e)
			{
				throw new Exception("Could not parse line " + Integer.toString(lineNumber) + " in file " + imagesFile.getAbsolutePath() + ".", e);
			}
			
			// Now we construct a tree out of map with the images
			ImageFolderNode rootNode = new ImageFolderNode(null, "", ImageFolderNode.ImageFolderType.ROOT);
			for(Entry<String, ImageList> imageFolder : imageFolders.entrySet())
			{
				String[] keys = imageFolder.getKey().split(SEPARATOR);
				rootNode.insertChild(keys, imageFolder.getValue());
			}
			
			// Return result.
			return rootNode;
		}
		finally
		{
			if(lineReader != null)
				lineReader.close();
		}
	}
}
