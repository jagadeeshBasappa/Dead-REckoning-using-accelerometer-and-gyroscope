package com.drc.poc.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class PositionHandler {
	
	private static double latitide = 0.0;
	private static double longitude = 0.0;
	
	//variables and constants
	//*************************************************************************
	//*************************************************************************
	
	//step parameters
	//*************************************************************************
	private static final float	STEP_INCREMENT = 0.015f;
	private static final float	STEP_ACCEL_THRESHOLD = 1.0f;
	private static final int	NUM_ACCEL_SAMPLES = 8;
	private static final float	SCALE_TO_FEET = 7.0f;
	
	//visual trail parameters
	//*************************************************************************
	private static final float	CRUMB_RADIUS = 0.09f;
	private static final int	MAX_POINTS = 250;

	//location and orientation
	//*************************************************************************
	private static FPoint mLocation = new FPoint(0,0);
	private static float[] mRotationVector = new float[4];
	private static float[] mRotationMatrix = new float[16];
	private static float mAccelerationAvg = 0;
	private static float mAzimuth;
	private static float mAzimuthOffset = 0;
	private static float mSpeed;
	private static float mTotalDistance = 0;
	
	//crumb trail
	//*************************************************************************
	private static Deque<FPoint> mDataPoints;
	private static FloatBuffer mDataPointsBuffer;
	private static float mDataPointCoords[] = new float[MAX_POINTS * 3];
	
	//averaging
	//*************************************************************************
	private static int sampleIndex = 0;
	private static boolean averageReady = false;
	private static float samples[] = new float[NUM_ACCEL_SAMPLES];
	
	//member functions
	//*************************************************************************
	//*************************************************************************
	
	//reset and init
	//*************************************************************************
	public static void reset() {
		initPosition(0,0);
	}
	
	public static void initPosition(float x, float y) {
		mDataPoints.clear();
		mDataPoints.add(new FPoint(x,y));
		mLocation.set(x, y);
		mTotalDistance = 0;		
	}
	
	public static void init() {
		mDataPoints = new LinkedList<FPoint>();
		mDataPoints.add(new FPoint(0,0));
		mTotalDistance = 0;
		
		ByteBuffer cbb = ByteBuffer.allocateDirect(mDataPointCoords.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mDataPointsBuffer = cbb.asFloatBuffer();
		mDataPointsBuffer.put(mDataPointCoords);
		mDataPointsBuffer.position(0);
	}
	
	//rotationUpdate
	//
	//Takes a 3d rotation vector and isolates the rotation about the z-axis
	//*************************************************************************
	public static void rotationUpdate(SensorEvent e) {
		float projected[] = new float[3];
		
		mRotationVector = Arrays.copyOf(e.values, e.values.length);
		SensorManager.getRotationMatrixFromVector(mRotationMatrix, e.values);
		SensorManager.getOrientation(mRotationMatrix, projected);
		
		mAzimuth = -projected[0];
		updateLocation();
	}

	//accelerationUpdate
	//
	//Keeps a running average of the magnitude of the phone's acceleration.
	//When the instantaneous acceleration exceeds the average acceleration by
	//a threshold value, a step is recorded
	//*************************************************************************
	public static void accelerationUpdate(SensorEvent e) {
		float magnitude = (float)Math.sqrt(
			Math.pow(e.values[0],2) + 
			Math.pow(e.values[1],2) + 
			Math.pow(e.values[2],2)
			);
		
		samples[(sampleIndex++) % NUM_ACCEL_SAMPLES] = magnitude;
		
		if (sampleIndex > NUM_ACCEL_SAMPLES || averageReady) {
			averageReady = true;
			mAccelerationAvg = samples[0];
			for (int i=1; i<NUM_ACCEL_SAMPLES; ++i)
				mAccelerationAvg += samples[i];
			
			mAccelerationAvg /= (float)NUM_ACCEL_SAMPLES;
			
			if (Math.abs(magnitude-mAccelerationAvg) > STEP_ACCEL_THRESHOLD) {
				takeStep();
				updateLocation();
			}
		}
	}
	
	//takeStep / speedDecay
	//Control the velocity associated with taking a step
	//*************************************************************************
	public static void takeStep() {
		mSpeed = STEP_INCREMENT;
	}
	
	public static void speedDecay() {
		mSpeed = 0;
	}
	
	public static void setLocation(float x, float y) {
		
		mLocation.set(x, y);
		
		FPoint last = mDataPoints.getLast();
		float dd = mLocation.distanceFrom(last);
		System.out.println("dist: "+dd);
		
		updateCrumbs(dd);
	}
	
	//updateLocation
	//*************************************************************************
	public static void updateLocation() {
		float dx = (float)Math.cos(getCurrentAzimuth()) * getCurrentSpeed();
		float dy = (float)Math.sin(getCurrentAzimuth()) * getCurrentSpeed();
		float dd = (float)Math.sqrt(Math.pow(dx,2) + Math.pow(dy,2));
		mLocation.offset(dx, dy);
//		System.out.println("dx: "+dx+" dy: "+dy+"  ang: "+getCurrentAzimuthDegrees());
		float angle = getCurrentAzimuthDegrees();
		double [] latlng = GeoCoOrdinatesUtil.findGeoCoordinates(dd/325, -((angle<0)?((360+angle)):angle), latitide, longitude);
		updateRefLatitude(latlng [0], latlng [1]);
		speedDecay();
		updateCrumbs(dd);
	}
	
	private static void updateCrumbs(float dd) {
		FPoint last = mDataPoints.getLast();		
		mTotalDistance += dd;
		
		if ( mLocation.distanceFrom(last) >= CRUMB_RADIUS) {
			
			if (mDataPoints.size() >= MAX_POINTS)
				mDataPoints.removeFirst();
			
			mDataPoints.addLast(new FPoint(
				mLocation.getX(),
				mLocation.getY())
				);
			
			int i=0;
			for( FPoint p : mDataPoints) {
				mDataPointCoords[i++] = p.getX();
				mDataPointCoords[i++] = p.getY();
				mDataPointCoords[i++] = 0;
			}
			
			mDataPointsBuffer.put(mDataPointCoords);
			mDataPointsBuffer.position(0);
		}
	}
	
	//Generic accessor functions
	//*************************************************************************
	public static float[] getCurrentRotationMatrix() {
		return mRotationMatrix;
	}
	
	public static float[] getCurrentRotationVector() {
		return mRotationVector;
	}
	
	public static void setAzimuthOffset(float o) {
		mAzimuthOffset = o;
	}
	
	public static float getCurrentAzimuth() {
		return mAzimuth + mAzimuthOffset;
	}
	
	public static float getCurrentAzimuthDegrees() {
		return getCurrentAzimuth() * (float)(180/Math.PI);
	}
	
	public static float getCurrentSpeed() {
		return mSpeed;
	}
	
	public static FPoint getCurrentLocation() {
		return mLocation;
	}
	
	public static float getAverageAcceleration() {
		return mAccelerationAvg;
	}
	
	public static FloatBuffer getCrumbBuffer() {
		return mDataPointsBuffer;
	}
	
	public static int getCrumbBufferSize() {
		return mDataPoints.size();
	}
	
	public static float getTotalDistance() {
		return mTotalDistance;
	}
	
	public static float getTotalDistanceFeet() {
		return mTotalDistance * SCALE_TO_FEET;
	}
	
	public static void updateRefLatitude (double lat, double lng) {
		latitide = lat;
		longitude = lng;
	}
	
	public static double [] getLocation () {
		return new double [] {latitide, longitude};
	}
	
/*	private static double [] getLocValues () {
		double [] latlng = GeoCoOrdinatesUtil.findGeoCoordinates(getCurrentSpeed(), getCurrentAzimuthDegrees(), latitide, longitude);
		updateRefLatitude(latlng [0], latlng [1]);
		return latlng;
	}*/
}
