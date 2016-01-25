/*************************************************************************

  AccelerationHandler
  
  	Event routing for events of TYPE_ACCELEROMETER
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc.utils;

import android.hardware.SensorEvent;

public class AccHandler implements SensorHandler {
	public void handleEvent(SensorEvent e) {
		PositionHandler.accelerationUpdate(e);
	}
}
