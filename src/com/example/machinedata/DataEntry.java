package com.example.machinedata;

/**
 * Class for single data entry.
 * @author Tomi Leinonen
 *
 */
public class DataEntry {

	public long time;
	public float value;
	
	public DataEntry(long t, float v){
		time = t;
		value = v;
	}
}
