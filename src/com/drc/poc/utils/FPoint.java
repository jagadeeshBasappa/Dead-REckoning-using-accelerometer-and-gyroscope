/*************************************************************************

  FloatPoint
  	
  	MH 3.27.2012
  	
*************************************************************************/

package com.drc.poc.utils;

public class FPoint {
	private float x;
	private float y;
	
	public float getX() { 
		return x; 
	}
	public float getY() { 
		return y; 
	}
	
	public FPoint(float ix, float iy) {
		x = ix;
		y = iy;
	}
	
	public void set(float newx, float newy) {
		x = newx;
		y = newy;
	}
	
	public void offset(float dx, float dy) {
		x += dx;
		y += dy;
	}
	
	public float distance() {
		return (float)Math.sqrt(Math.pow(x, 2) + Math.pow(y,2));
	}
	
	public float distanceFrom(FPoint other) {
		return (float)Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
	}
}
