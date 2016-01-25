/*************************************************************************

  GlAndSensorsActivity
  
  	Top-level activity for OpenGL location tracker.
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc;

import com.drc.poc.utils.PositionHandler;
import com.drc.poc.utils.SensorUtil;
import com.drc.poc.utils.OpenGlSurfaceView;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
	
	private MySurfaceView mSurfaceView;
	private boolean isSensorAvailable;
	private SensorUtil mSensorUtil;
	TextView tv;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSensorUtil = new SensorUtil(this);
        PositionHandler.init();
        if (mSensorUtil.systemMeetsRequirements()) {
        	
        	requestWindowFeature(Window.FEATURE_NO_TITLE);
        	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	
        	mSurfaceView = new MySurfaceView(this);
        	setContentView(R.layout.main);
        	LinearLayout ll = (LinearLayout) findViewById(R.id.parent);
        	tv = (TextView) findViewById(R.id.textView2);
        	ll.addView(mSurfaceView);
        	isSensorAvailable = true;
        } else {
        	
        	setContentView(R.layout.main);
        	isSensorAvailable = false;
        }
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	if (isSensorAvailable) {
    		mSensorUtil.registerListeners();
    		mSurfaceView.onResume();
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (isSensorAvailable) {
    		mSensorUtil.unregisterListeners();
    		mSurfaceView.onPause();
    	}
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event) {
    	PositionHandler.reset();
        return true; 
    } 

    public void onSensorChanged(SensorEvent event) {
    	mSensorUtil.routeEvent(event);
    	tv.setText(PositionHandler.getTotalDistance()+"\n"+PositionHandler.getTotalDistanceFeet());
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	
    }
}

class MySurfaceView extends GLSurfaceView {
	public MySurfaceView(Context context) {
		super(context);
		
		setRenderer(new OpenGlSurfaceView());
	}
}