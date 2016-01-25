package com.drc.poc;

import com.drc.poc.utils.PositionHandler;
import com.drc.poc.utils.SensorUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class MainMapActivity extends FragmentActivity implements SensorEventListener {
	
	GoogleMap googleMap;
	Marker marker;
	private SensorUtil mSensorUtil;
	private boolean isSensorAvailable;
	private double refLat = 12.981620, refLng = 77.722737;
	private Button viewChangeBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
    	setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_main);
		viewChangeBtn = (Button) findViewById(R.id.button1);
		SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
		googleMap = fm.getMap();
		googleMap.setMyLocationEnabled(true);	
		
        marker = googleMap.addMarker(new MarkerOptions()
                                  .position(new LatLng(refLat, refLng))
                                  .draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.blue)));
        
        googleMap.setOnMapLongClickListener(new OnMapLongClickListener() {
			
			@Override
			public void onMapLongClick(LatLng point) {
				Vibrator v = (Vibrator) MainMapActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
				 v.vibrate(500);
				refLat = point.latitude;
				refLng = point.longitude;
				initSensorHandler();
				googleMap.setOnMapLongClickListener(null);
			}
		});
        
        viewChangeBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String txt = (String) viewChangeBtn.getText();
				if (txt.equalsIgnoreCase("Satellite")) {
					googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
					viewChangeBtn.setText("Map");
				} else {
					googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
					viewChangeBtn.setText("Satellite");
				}
			}
		});
        
        mSensorUtil = new SensorUtil(this);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom((new LatLng(refLat, refLng)), 19));
	}
	
	private void initSensorHandler () {
		PositionHandler.init();
        PositionHandler.updateRefLatitude(refLat, refLng);
        if (mSensorUtil.systemMeetsRequirements()) {
        	isSensorAvailable = true;
        	mSensorUtil.registerListeners();
        } else {
        	isSensorAvailable = false;
        }
	}
	
	
	private void displayLocation (double latitude, double longitude) {
				LatLng latLng = new LatLng(latitude, longitude);
//				googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//				googleMap.moveCamera(CameraUpdateFactory.zoomTo(19));
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
				if (marker != null) {
					marker.setPosition(latLng);
				}
	}
	

	public void onLocationChanged(Location location) {
		double latitude = location.getLatitude();
		double longitude = location.getLongitude();		
		LatLng latLng = new LatLng(latitude, longitude);
		googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
		googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	if (isSensorAvailable) {
    		mSensorUtil.registerListeners();
    	}
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	if (isSensorAvailable) {
    		mSensorUtil.unregisterListeners();
    	}
    }
    
    @Override 
    public boolean onTouchEvent(MotionEvent event) {
    	PositionHandler.reset();
        return true; 
    } 

    public void onSensorChanged(SensorEvent event) {
    	mSensorUtil.routeEvent(event);
    	double l [] = PositionHandler.getLocation();
    	displayLocation(l[0], l[1]);
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    	
    }
}
