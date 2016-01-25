/*************************************************************************

  EventHandler
  
  	Sensor event routing interface
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc.utils;

import android.hardware.SensorEvent;

public interface SensorHandler {
	public void handleEvent(SensorEvent e);
}
