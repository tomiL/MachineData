package com.example.machinedata;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.ListView;

/**
 * Main activity class.
 * @author Tomi Leinonen
 *
 */
public class MainActivity extends Activity {

	private List<DataStream> streams = new LinkedList<DataStream>();
	
	//data reader objects
	private DataFileReader dataReader;
	private UdpReceiver udpReceiver;
	private AssetManager assets;
	double[] receivedData;
	
	//timer and thread managing  
	private Timer dataStreamTimer;
	private TimerTask dataStreamTask;
	private RefreshHandler handler = new RefreshHandler();
	private boolean isDataStreamActive = false;
	private boolean currentlyStreaming = false;
	
	//setting variables
	private boolean sampleDataMode = true;
	private int timePerPixel;
	private int queryInterval;
	private final int port = 4107;
	private final int totalSensors = 26;
	
	//view and adapter objects
	private ListView dataGraphListView;
	private ListView rawDataListView;
	private DataGraphAdapter graphAdapter;
	private RawDataAdapter rawDataAdapter;
	private Intent settingsScreenIntent;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_screen);
		
		//set options and intents
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		settingsScreenIntent = new Intent(this, SettingsScreenActivity.class);
		
		//get app resources and assets
		Resources res = getResources();
		assets = getAssets();
		queryInterval = res.getInteger(R.integer.query_interval);
		timePerPixel = res.getInteger(R.integer.time_per_pixel);
		
		//open sample data file 
		try{
			dataReader = new DataFileReader(assets.open("data.csv"));
		}catch (IOException e){
			Log.e("ERR", "IOException in MainActivity.onCreate");
		}
		//create udp connection
		udpReceiver = new UdpReceiver(port, totalSensors);
		receivedData = new double[totalSensors];
		
		//create datastreams for sensors
		streams.add(new DataStream("Pressure of Steering Valve: A-port", "bar", "%.0f", 0, 300, 0, 250, 1));
		streams.add(new DataStream("Pressure of Steering Valve: B-port", "bar", "%.0f", 0, 300, 0, 250, 2));
		streams.add(new DataStream("Pressure of Boom Valve: A-port", "bar", "%.0f", 0, 300, 0, 250, 3));
		streams.add(new DataStream("Pressure of Boom Valve: B-port", "bar", "%.0f", 0, 300, 0, 250, 4));
		streams.add(new DataStream("Pressure of Drive Pump: A-port", "bar", "%.0f", 0, 500, 0, 400, 9));
		streams.add(new DataStream("Pressure of Drive Pump: B-port", "bar", "%.0f", 0, 500, 0, 400, 10));
		streams.add(new DataStream("Steering Command of Driver", "%", "%.2f", -100, 100, -100, 100, 11));
		streams.add(new DataStream("Boom Command of Driver", "%", "%.2f", -100, 100, -100, 100, 12));
		streams.add(new DataStream("Bucket Command of Driver", "%", "%.2f", -100, 100, -100, 100, 13));
		streams.add(new DataStream("Joint Angle of the Boom", "deg", "%.2f", -50, 50, -45, 45, 14));
		streams.add(new DataStream("Joint Angle of the Bucket", "deg", "%.2f", -80, 80, -75, 75, 15));
		
		//create timed thread to read data
		dataStreamTimer = new Timer();
		
		dataStreamTask = new TimerTask(){
        	public void run() {
        		if (currentlyStreaming) {
        			synchronized(streams){
        				//sample data reading
        				if(sampleDataMode){
		        			for(DataStream ds : streams){
		        				synchronized(ds){
			        				ds.addEntry(System.currentTimeMillis(), dataReader.getValue(ds.dataColumn));
		        				}
		        			}
		        			//read next line in file, start over if fails
		        			if(!dataReader.nextLine((int) (queryInterval / 10) - 1)){
		        				try{
		        					dataReader = new DataFileReader(assets.open("data.csv"));
		        				}catch (IOException e){
		        					Log.e("ERR", "IOException in dataStreamTask.run");
		        				}
		        			}
        				}
        				//udp receiving
        				else{
        					if(udpReceiver.receive(receivedData)){
        						for(DataStream ds : streams){
			        				synchronized(ds){
				        				ds.addEntry(System.currentTimeMillis(), (float) receivedData[ds.dataColumn]);
			        				}
        						}
        					}
        				}
        			}
        			//stop thread until next query
        			try {
        				synchronized(this){
        					this.wait(queryInterval);
        				}
					} catch (InterruptedException e) {
						Log.e("ERR", "InterruptedException in TimerTask.run");
					}
        		}
        	}
		};
		
		//create adapter for data graph views
		graphAdapter = new DataGraphAdapter(this, R.layout.data_view, streams);
		graphAdapter.timePerPixel = timePerPixel;
		rawDataAdapter = new RawDataAdapter(this, R.layout.raw_data, streams);
		
		//start UI update loop
		dataStreamUpdate(queryInterval);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
		//settings screen closed with OK
    	if(resultCode == Activity.RESULT_OK && requestCode == 1){
    		//get new settings
    		Bundle newSettings = data.getExtras();
    		
    		queryInterval = newSettings.getInt("queryInterval", queryInterval);
    		timePerPixel = newSettings.getInt("timePerPixel", timePerPixel);
    		sampleDataMode = newSettings.getBoolean("sampleDataMode", sampleDataMode);
    		
    		//set time per pixel settings to graphs and adapters
    		graphAdapter.timePerPixel = timePerPixel;
    		for(DataStream ds : streams){
    			synchronized(ds){
    				ds.setGraphSettings(timePerPixel);
    			}
    		}
    	}
    }
	
	/**
	 * Update all views bound to datastreams. Call from UI thread.
	 * @param delay Time until next update (ms)
	 */
	public void dataStreamUpdate(int delay){
		synchronized(streams){
			for(DataStream s: streams){
				s.update();
			}
		}
		handler.sleep(delay);
	}
	
	/**
	 * onClick to access data screen.
	 */
	public void dataScreen(View view){
		setContentView(R.layout.data_view_screen);
		
		dataGraphListView = (ListView) findViewById(R.id.dataGraphListView);
		dataGraphListView.setAdapter(graphAdapter);
	}
	
	/**
	 * onClick to access home screen.
	 */
	public void homeScreen(View view){
		setContentView(R.layout.home_screen);
	}
	
	/**
	 * onClick to access raw data screen.
	 */
	public void rawDataScreen(View view){
		setContentView(R.layout.raw_data_screen);
		
		rawDataListView = (ListView) findViewById(R.id.rawDataListView);
		rawDataListView.setAdapter(rawDataAdapter);
	}
	
	/**
	 * onClick to access settings screen.
	 */
	public void settingsScreen(View view){
		settingsScreenIntent.putExtra("queryInterval", queryInterval);
		settingsScreenIntent.putExtra("timePerPixel", timePerPixel);
		settingsScreenIntent.putExtra("sampleDataMode", sampleDataMode);
		
		startActivityForResult(settingsScreenIntent, 1);
	}
	
	/**
	 * onClick to start/stop data stream.
	 */
	public void updateTest(View view){
		if(!isDataStreamActive){
			//start timed thread if not active
			currentlyStreaming = true;
			isDataStreamActive = true;
			dataStreamTimer.schedule(dataStreamTask, 10, 1);
		}
		else{
			//switch stream idling on/off
			currentlyStreaming = !currentlyStreaming;
		}
	}
	
	/**
	 * onClick to clear stored data.
	 */
	public void clearDataTest(View view){
		synchronized(streams){
			for(DataStream s: streams){
				s.clearData();
			}
		}
	}
	
	/**
	 * Inner class for update handling.
	 */
	class RefreshHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
        	dataStreamUpdate(queryInterval);
        }
        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
	}
}
