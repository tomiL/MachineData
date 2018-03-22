package com.example.machinedata;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

/**
 * Adapter class for adapting data streams to graph views.
 * @author Tomi Leinonen
 *
 */
public class DataGraphAdapter extends ArrayAdapter<DataStream>{

	private Context context;
    private int viewResourceId;   
    private List<DataStream> dataStreams;
    
    public int timePerPixel;
	
	public DataGraphAdapter(Context context, int viewResourceId, List<DataStream> dataStreams) {
		super(context, viewResourceId, dataStreams);
		
		this.context = context;
		this.viewResourceId = viewResourceId;
		this.dataStreams = dataStreams;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		View v = convertView;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.data_view, null);
		
        DataStream stream = dataStreams.get(position);
        if (stream != null) {
        	//bind datastream to display
        	DataGraphDisplay display = (DataGraphDisplay) v.findViewById(R.id.dataGraphDisplay);
        	HorizontalScrollView scrollView = (HorizontalScrollView) v.findViewById(R.id.dataGraphScrollView);
        	display.setDataStream(stream);
        	display.setScroll(scrollView);   	
        	display.timePerPixel = timePerPixel;
        	
        	//set title and vertical value texts 
        	TextView titleText = (TextView) v.findViewById(R.id.graphTitleText);
        	TextView valueTextTop = (TextView) v.findViewById(R.id.graphValueTextTop);
        	TextView valueTextTopMid = (TextView) v.findViewById(R.id.graphValueTextMidTop);
        	TextView valueTextMid = (TextView) v.findViewById(R.id.graphValueTextMid);
        	TextView valueTextLowMid = (TextView) v.findViewById(R.id.graphValueTextMidLow);
        	TextView valueTextLow = (TextView) v.findViewById(R.id.graphValueTextLow);
        	
        	titleText.setText(stream.name);
        	try{
	        	valueTextTop.setText(String.format(stream.format, stream.maxValue) + stream.unit);
	        	valueTextTopMid.setText(String.format(stream.format, ((stream.maxValue * 3 + stream.minValue) / 4)) + stream.unit);
	        	valueTextMid.setText(String.format(stream.format, ((stream.maxValue + stream.minValue) / 2)) + stream.unit);
	        	valueTextLowMid.setText(String.format(stream.format, ((stream.minValue * 3 + stream.maxValue) / 4)) + stream.unit);
	        	valueTextLow.setText(String.format(stream.format, stream.minValue) + stream.unit);
        	}catch(java.util.IllegalFormatException e){
        		Log.e("ERR", "Format exception in DataGraphAdapter.getView");
        		stream.format = "%.2f";
        	}
        }
        
		return v;
	}

}
