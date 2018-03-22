package com.example.machinedata;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Class for displaying raw data numbers and bars.
 * @author Tomi Leinonen
 *
 */
public class RawDataDisplay extends View{

	private DataStream stream;
	
	//graphic and visible text objects
	private Paint textPaint = new Paint();
	private Paint paint = new Paint();
	private RectF bar = new RectF();
	private RectF rect = new RectF();
	private String unit;
	private String format;
	
	//bar value variables
	private float value;	
	private float maxValue;
	private float minValue;
	private float lowWarning;
	private float highWarning;
	
	public RawDataDisplay(Context context) {
		super(context);
	}
	
	public RawDataDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	/**
	 * Binds data stream to this view.
	 * @param ds Data stream to bind
	 */
	public void setDataStream(DataStream ds) {
		stream = ds;
		ds.setRawDataDisplay(this);
		
		unit = ds.unit;
		format = ds.format;
		lowWarning = ds.lowWarning;
		highWarning = ds.highWarning;
		minValue = ds.minValue;
		maxValue = ds.maxValue;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.setMeasuredDimension(parentWidth, parentHeight);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		//set surrounding rectangle
		rect.set(0, 0, getMeasuredWidth() - 1, getMeasuredHeight() - 1);
	}
	
	@Override
	public void onDraw(Canvas canvas){
		
		//set bar and text color
		if(value > highWarning || value < lowWarning){
			//warning colors
			paint.setColor(Color.argb(80, 240, 0, 0));
			textPaint.setColor(Color.RED);
		}
		else{
			//normal colors
			paint.setColor(Color.argb(80, 0, 20, 100));
			textPaint.setColor(Color.BLACK);
		}
		
		//draw bar
		bar.set((getMeasuredWidth() -((value - minValue) / (maxValue - minValue)) * getMeasuredWidth()), 0, getMeasuredWidth(), getMeasuredHeight());
		paint.setStyle(Paint.Style.FILL);
		canvas.drawRect(bar, paint);
		
		//draw surrounding rectangle
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(1);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(rect, paint);
		
		//draw value text
		textPaint.setTextSize(20);	
		textPaint.setTextAlign(Paint.Align.RIGHT);
		try{
			canvas.drawText(String.format(format, value) + " " + unit, getMeasuredWidth() - 2, getMeasuredHeight() - 2, textPaint);
		}catch(java.util.IllegalFormatException e){
			Log.e("ERR", "Format exception in RawDataDisplay.onDraw");
			format = "%.2f";
		}
	}
	
	/**
	 * Updates view with new value.
	 * @param entry Data entry to update with
	 */
	public void update(DataEntry entry){
		value = entry.value;
		invalidate();
	}

}
