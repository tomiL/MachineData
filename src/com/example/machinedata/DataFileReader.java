package com.example.machinedata;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.util.Log;

/**
 * Class for reading data from .csv files.
 * @author Tomi Leinonen
 *
 */
public class DataFileReader {
	
	private BufferedReader reader;
	private String[] currentLine;
	
	public DataFileReader(InputStream inputStream){	
		InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
		reader = new BufferedReader(inputStreamReader);
		nextLine(0);
	}
	
	/**
	 * Reads next line in file and splits it into array.
	 * @param skip Number of lines to skip before reading
	 * @return True if successful
	 */
	public boolean nextLine(int skip){
		try{
			for(int i = 1; i < skip; i++){
				reader.readLine();
			}
			String line = reader.readLine();
			if(line != null){
				line = line.replace(',', '.');
				currentLine = line.split(";");
			}
			else{
				reader.close();
				return false;
			}
		}catch(IOException e){
			Log.e("ERR", "IOException in DataFileReader.nextLine");
			return false;
		}
		return true;
	}
	
	/**
	 * Returns value of specified column in current line.
	 * @param column Number of column to read
	 * @return Value of specified column
	 */
	public float getValue(int column){
		return Float.parseFloat(currentLine[column]);
	}
}
