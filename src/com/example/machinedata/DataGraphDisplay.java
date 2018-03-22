package com.example.machinedata;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.RectF;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;

/**
 * Class for displaying data stream graphs.
 * @author Tomi Leinonen
 *
 */
public class DataGraphDisplay extends View{
	
	private DataStream dataStream;
	private GestureDetector gestureDetector;
	private boolean measured = false;
	
	//graphic objects and variables
	private Paint paint = new Paint();
	private Paint textPaint = new Paint();
	private RectF dataArea = new RectF();
	private RectF highWarningArea = new RectF();
	private RectF lowWarningArea = new RectF();
	private RectF greenArea = new RectF();
	private Path graph = new Path();
	private float maxValue;
	private float minValue;
	private float highWarning;
	private float lowWarning;
	private float unitsPerPixel = 1;
	public int timePerPixel = 1;
	
	//scroll control
	private HorizontalScrollView scroll;
	private boolean autoScroll = false;
	
	public DataGraphDisplay(Context context) {
		super(context);
		Resources res = context.getResources();
		timePerPixel = res.getInteger(R.integer.time_per_pixel);
	}
	
	public DataGraphDisplay(Context context, AttributeSet attrs) {
		super(context, attrs);
		Resources res = context.getResources();
		timePerPixel = res.getInteger(R.integer.time_per_pixel);
	}
	
	public HorizontalScrollView getScroll() {
		return scroll;
	}

	public void setScroll(HorizontalScrollView scroll) {
		this.scroll = scroll;
		autoScroll = true;
	}

	/**
	 * Binds data stream to this view.
	 * @param ds Data stream to bind
	 */
	public void setDataStream(DataStream ds){
		dataStream = ds;

		minValue = dataStream.minValue;
		maxValue = dataStream.maxValue;
		lowWarning = dataStream.lowWarning;
		highWarning = dataStream.highWarning;
		
		dataStream.setGraphDisplay(this);
	}
	
	@Override
	public void onFinishInflate(){
		super.onFinishInflate();
		setFocusableInTouchMode(true);
		gestureDetector = new GestureDetector(getContext(), new GestureListener());
	}
	
	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.setMeasuredDimension(parentWidth, parentHeight);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int height = getMeasuredHeight();
		int width = getMeasuredWidth();
		//set area for data display
		dataArea.set(0, 0, width, height * 0.9f);
		unitsPerPixel = (maxValue - minValue) / dataArea.height();
		
		//set warning area rectangles
		lowWarningArea.set(dataArea.left, dataArea.bottom - ((lowWarning - minValue) / unitsPerPixel), dataArea.right, dataArea.bottom);
		if(lowWarningArea.top >= dataArea.bottom){
			lowWarningArea.setEmpty();
		}
		else if(lowWarningArea.bottom > dataArea.bottom){
			lowWarningArea.bottom = dataArea.bottom;
		}
		highWarningArea.set(dataArea.left, dataArea.top, dataArea.right, dataArea.top + ((maxValue - highWarning) / unitsPerPixel));
		if(highWarningArea.bottom <= dataArea.top){
			highWarningArea.setEmpty();
		}
		else if(highWarningArea.top < dataArea.top){
			highWarningArea.top = dataArea.top;
		}
		greenArea.set(dataArea.left, highWarningArea.bottom, dataArea.right, lowWarningArea.top);
		if(greenArea.bottom > dataArea.bottom || greenArea.bottom == 0){
			greenArea.bottom = dataArea.bottom;
		}
		if(greenArea.top < dataArea.top || greenArea.top == 0){
			greenArea.top = dataArea.top;
		}
		
		measured = true;
		buildGraph();
	}
	
	@Override
	public void onDraw(Canvas canvas){
		
		//draw color backgrounds
		paint.setColor(Color.argb(86, 0, 200, 0));
		paint.setStyle(Paint.Style.FILL);
		if(!greenArea.isEmpty()){
			canvas.drawRect(greenArea, paint);
		}
		
		paint.setColor(Color.argb(86, 200, 0, 0));
		if(!highWarningArea.isEmpty()){
			canvas.drawRect(highWarningArea, paint);
		}
		if(!lowWarningArea.isEmpty()){
			canvas.drawRect(lowWarningArea, paint);
		}
		
		
		//draw rectangle around data area
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(4);
		paint.setStyle(Paint.Style.STROKE);
		canvas.drawRect(dataArea, paint);
		
		//draw vertical lines
		paint.setStrokeWidth(1);
		paint.setColor(Color.argb(128, 0, 0, 0));
		for(int i = 20; i < (int) dataArea.width(); i += 20){
			canvas.drawLine((dataArea.left + i), 
					dataArea.bottom, 
					(dataArea.left + i), 
					dataArea.top, paint);
		}
		
		//draw horizontal lines
		for(int i = 1; i < 4; i++){
			canvas.drawLine(dataArea.left, 
					(dataArea.top + (((float) i/4) * dataArea.height())), 
					dataArea.right, 
					(dataArea.top + (((float) i/4) * dataArea.height())), paint);
		}
		
		//draw data graph
		paint.setStrokeWidth(2);
		paint.setColor(Color.BLACK);
		paint.setPathEffect(null);
		canvas.drawPath(graph, paint);
		
		//print time values
		textPaint.setColor(Color.BLACK);
		textPaint.setTextSize(13);
		textPaint.setTextAlign(Align.CENTER);

		if(dataStream != null){
			synchronized(dataStream){
				for(int i = 100; i < (int) dataArea.width(); i += 200){
					canvas.drawText((String) DateFormat.format("kk:mm:ss", (long) (dataStream.startTime + i * timePerPixel)), 
							dataArea.left + i, 
							getMeasuredHeight() * 0.97f, 
							textPaint);
				}
			}
		}
	}
	
	/**
	 * Update the view.
	 */
	public void update(){
		try{
			//rebuild graph from data if lost
			if(graph.isEmpty()){
				buildGraph();
			}
			
			//cut half of data when end of display area is reached
			if((dataStream.lastTime - dataStream.startTime) / timePerPixel >= dataArea.right){
				dataStream.removeData(0.5f);
			}
			
			//set ScrollView to correct position when autoscrolling
			if(autoScroll){
				synchronized(dataStream){
					scroll.scrollTo((int) ((dataStream.lastTime - dataStream.startTime) / timePerPixel - 800), 0);
				}
			}
			invalidate();
		}catch(java.lang.NullPointerException e){
			Log.e("ERR", "Null pointer exception in DataGraphDisplay.update");
		}
	}
	
	/**
	 * Add a point to graph.
	 * @param time Time (X) of point
	 * @param value Value (Y) of point
	 */
	public void addToGraph(long time, float value){
		if(measured){
			synchronized(dataStream){
				if(graph.isEmpty()){
					graph.moveTo(dataArea.left + ((time - dataStream.startTime) / timePerPixel), 
							dataArea.bottom - ((value - minValue) / unitsPerPixel));
				}
				graph.lineTo(dataArea.left + ((time - dataStream.startTime) / timePerPixel), 
						dataArea.bottom - ((value - minValue) / unitsPerPixel));
			}
		}
	}
	
	/**
	 * Build entire graph from data.
	 */
	public void buildGraph(){
		if(dataStream != null){
			graph.reset();
			synchronized(dataStream){
				for(DataEntry d : dataStream.getData()){
					if(graph.isEmpty()){
						graph.moveTo(dataArea.left + ((d.time - dataStream.startTime) / timePerPixel), 
								dataArea.bottom - ((d.value - minValue) / unitsPerPixel));
					}
					graph.lineTo(dataArea.left + ((d.time - dataStream.startTime) / timePerPixel), 
							dataArea.bottom - ((d.value - minValue) / unitsPerPixel));
				}
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
	    return gestureDetector.onTouchEvent(e);
	}
	
	private class GestureListener extends GestureDetector.SimpleOnGestureListener {

	    @Override
	    public boolean onDown(MotionEvent e) {
	        return true;
	    }
	    // event when double tap occurs
	    @Override
	    public boolean onDoubleTap(MotionEvent e) {
	    	if(scroll != null){
	    		autoScroll = !autoScroll;
	    	}
	        return true;
	    }
	}
}
