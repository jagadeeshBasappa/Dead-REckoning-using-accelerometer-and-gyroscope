/*************************************************************************

  RotationHandler
  
  	Event routing for events of TYPE_ROTATION_VECTOR
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc.utils;

import android.hardware.SensorEvent;

public class RotationHandler implements SensorHandler {
	public void handleEvent(SensorEvent e) {
		PositionHandler.rotationUpdate(e);
	}
}
