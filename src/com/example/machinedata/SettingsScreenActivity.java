package com.example.machinedata;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

/**
 * Activity class for settings screen.
 * @author Tomi Leinonen
 *
 */
public class SettingsScreenActivity extends Activity {

	private Intent data = new Intent();
	private Resources res;
	
	//view object holders
	private SeekBar queryIntervalBar;
	private TextView queryIntervalValue;
	private SeekBar timePerPixelBar;
	private TextView timePerPixelValue;
	private RadioGroup dataSourceRadioGroup;

	//currently selected settings
	private boolean sampleDataMode;
	private int timePerPixel;
	private int queryInterval;
		
	//bar value variables
	private final int barMultiplier = 10;
	private int minQueryInterval;
	private int minTimePerPixel;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_screen);
		
		//get view objects
		queryIntervalBar = (SeekBar) findViewById(R.id.queryIntervalSeekBar);
		queryIntervalValue = (TextView) findViewById(R.id.queryIntervalValue);
		timePerPixelBar = (SeekBar) findViewById(R.id.timePerPixSeekBar);
		timePerPixelValue = (TextView) findViewById(R.id.timePerPixValue);
		dataSourceRadioGroup = (RadioGroup) findViewById(R.id.dataSourceRadioGroup);
		
		//get app resources
		res = getResources();
		minTimePerPixel = res.getInteger(R.integer.min_time_per_pixel);
		minQueryInterval = res.getInteger(R.integer.min_query_interval);
		
		//set max values for bars
		queryIntervalBar.setMax((res.getInteger(R.integer.max_query_interval) - minQueryInterval) / barMultiplier);
		timePerPixelBar.setMax((res.getInteger(R.integer.max_time_per_pixel) - minTimePerPixel) / barMultiplier);
		
		//set change listeners for bars
		queryIntervalBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				queryInterval = progress * barMultiplier + minQueryInterval;
				queryIntervalValue.setText(String.valueOf(queryInterval));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
        });
		
		timePerPixelBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				timePerPixel = progress * barMultiplier + minTimePerPixel;
				timePerPixelValue.setText(String.valueOf(timePerPixel));
			}
			public void onStartTrackingTouch(SeekBar seekBar) {}
			public void onStopTrackingTouch(SeekBar seekBar) {}
        });
		
		//set listener for radio button
		dataSourceRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener(){
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.sampleRadioButton){
					sampleDataMode = true;
				}
				else if(checkedId == R.id.wlanRadioButton){
					sampleDataMode = false;
				}
			}
		});
		
		//get current setting values
		Bundle currentSettings = this.getIntent().getExtras();
		queryInterval = currentSettings.getInt("queryInterval", 100);
		timePerPixel = currentSettings.getInt("timePerPixel", 100);
		sampleDataMode = currentSettings.getBoolean("sampleDataMode", true);
		
		//set bar positions
		queryIntervalBar.setProgress((queryInterval - minQueryInterval) / barMultiplier);
		timePerPixelBar.setProgress((timePerPixel - minTimePerPixel) / barMultiplier);
		
		//set redio button
		if(sampleDataMode){
			dataSourceRadioGroup.check(R.id.sampleRadioButton);
		}
		else{
			dataSourceRadioGroup.check(R.id.wlanRadioButton);
		}
		
	}
	
	/**
	 * onClick to close the settings screen and apply new settings.
	 * @param view
	 */
	public void close(View view){
		//put settings in intent
		data.putExtra("queryInterval", queryInterval);
		data.putExtra("timePerPixel", timePerPixel);
		data.putExtra("sampleDataMode", sampleDataMode);
		
		setResult(Activity.RESULT_OK, data);
		finish();
	}
	
	/**
	 * onClick to close the settings screen without applying settings.
	 * @param view
	 */
	public void cancel(View view){
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
	
	/**
	 * onClick to restore default settings.
	 * @param view
	 */
	public void restoreDefaults(View view){
		//get default settings from resources
		queryInterval = res.getInteger(R.integer.query_interval);
		timePerPixel = res.getInteger(R.integer.time_per_pixel);
		sampleDataMode = true;
		
		//set bar positions
		queryIntervalBar.setProgress((queryInterval - minQueryInterval) / barMultiplier);
		timePerPixelBar.setProgress((timePerPixel - minTimePerPixel) / barMultiplier);
	}
}
