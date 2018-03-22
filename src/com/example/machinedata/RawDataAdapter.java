package com.example.machinedata;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Adapter class for adapting streams to raw number data displays.
 * @author Tomi Leinonen
 *
 */
public class RawDataAdapter extends ArrayAdapter<DataStream>{
	
	private Context context;
    private int viewResourceId;   
    private List<DataStream> dataStreams;
    
	public RawDataAdapter(Context context, int viewResourceId, List<DataStream> dataStreams) {
		super(context, viewResourceId, dataStreams);
		
		this.context = context;
		this.viewResourceId = viewResourceId;
		this.dataStreams = dataStreams;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		
		View v = convertView;
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        v = inflater.inflate(R.layout.raw_data, null);
		
        DataStream stream = dataStreams.get(position);
        if (stream != null) {
        	//set title text
        	TextView titleText = (TextView) v.findViewById(R.id.rawTitleText);
        	try{
        		titleText.setText(stream.name + " (" + String.format(stream.format, stream.lowWarning) + " - " + String.format(stream.format, stream.highWarning) + stream.unit + ")");
        	}catch(java.util.IllegalFormatException e){
        		Log.e("ERR", "Format exception in RawDataAdapter.getView");
    			stream.format = "%.2f";
        	}
        	//bind datastream to display 
        	RawDataDisplay display = (RawDataDisplay) v.findViewById(R.id.rawDataDisplay);
        	display.setDataStream(stream);
        }

		return v;
	}
}
