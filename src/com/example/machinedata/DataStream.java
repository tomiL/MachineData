package com.example.machinedata;

import java.util.LinkedList;
import java.util.List;

/**
 * Class for storing streamed data.
 * @author Tomi Leinonen
 *
 */
public class DataStream {
	
	private List <DataEntry> data = new LinkedList<DataEntry>();
	
	//display objects stream is bound to
	private DataGraphDisplay graphDisplay;
	private RawDataDisplay rawDataDisplay;
	
	//stream values
	public String name;
	public String unit;
	public String format;
	public int dataColumn;
	public float minValue;
	public float maxValue;
	public float highWarning;
	public float lowWarning;
	public long startTime = 0;
	public long lastTime = 0;
	
	/**
	 * Create a data stream for sensor.
	 * @param name Name of sensor
	 * @param unit Unit of sensor value
	 * @param format Format of sensor value to use, see String.format()
	 * @param minValue Minimum value displayed by graph
	 * @param maxValue Maximum value displayed by graph
	 * @param lowWarning Lower threshold of safe value range
	 * @param highWarning Upper threshold of safe value range
	 * @param dataColumn Column of received datapacket used by this sensor
	 */
	public DataStream(String name, String unit, String format, float minValue, float maxValue, float lowWarning, float highWarning, int dataColumn){
		this.name = name;
		this.unit = unit;
		this.format = format;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.lowWarning = lowWarning;
		this.highWarning = highWarning;
		this.dataColumn = dataColumn;
	}
	
	public DataGraphDisplay getGraphDisplay() {
		return graphDisplay;
	}

	public void setGraphDisplay(DataGraphDisplay display) {
		this.graphDisplay = display;
	}
	
	public RawDataDisplay getRawDataDisplay() {
		return rawDataDisplay;
	}

	public void setRawDataDisplay(RawDataDisplay rawDataDisplay) {
		this.rawDataDisplay = rawDataDisplay;
	}

	public List<DataEntry> getData(){
		return data;
	}
	
	/**
	 * Add a new data entry.
	 * @param time Time of entry (s)
	 * @param value Value of entry
	 */
	public void addEntry(long time, float value){
		//set start time
		if(data.size() == 0){
			startTime = time;
		}
		//ignore entry if time goes backward
		else if(time < lastTime){
			return;
		}
		data.add(new DataEntry(time, value));
		lastTime = time;
		//add value to display
		if(graphDisplay != null){
			graphDisplay.addToGraph(time, value);
		}
	}
	
	/**
	 * Clear all data.
	 */
	public void clearData(){
		data.clear();
		if(graphDisplay != null){
			graphDisplay.buildGraph();
		}
	}
	
	/**
	 * Remove part of data from the beginning.
	 * @param part Portion of total data to remove (0 - 1)
	 */
	public void removeData(float part){
		int size = data.size();
		if(part > 1){
			part = 1;
		}
		else if(part < 0){
			part = 0;
		}
		
		for(int i = 0; i < (int) (size*part); i++){
			data.remove(0);
		}
		//set start time and rebuild graph
		startTime = data.get(0).time;
		if(graphDisplay != null){
			graphDisplay.buildGraph();
		}
	}
	
	/**
	 * Callback to update graph display stream is bound to.
	 */
	public void update(){
		if(graphDisplay != null){
			graphDisplay.update();
		}
		if(rawDataDisplay != null && !data.isEmpty()){
			rawDataDisplay.update(data.get(data.size() - 1));
		}
	}
	
	/**
	 * Pass new settings to existing graph and redraw it.
	 * @param timePerPixel
	 */
	public void setGraphSettings(int timePerPixel){
		if(graphDisplay != null){
			graphDisplay.timePerPixel = timePerPixel;
			graphDisplay.buildGraph();
		}
	}
}
