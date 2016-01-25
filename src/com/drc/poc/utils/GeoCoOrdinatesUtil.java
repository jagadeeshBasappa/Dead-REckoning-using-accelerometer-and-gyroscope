package com.drc.poc.utils;

public class GeoCoOrdinatesUtil {
	/**
	 * get Lat/Lon by providing distance, bearing angle and reference lat/lon
	 * @param distanceInKM - distance from ref location in KM.
	 * @param bearingAngleInRadians - angular direction from north direction.
	 * @param refLatitude - reference latitude.
	 * @param refLongitude - reference longitude.
	 * @return latitude longitude in double array.
	 */
	public static double[] findGeoCoordinates(double distanceInMeters, double azimuthAngleInDegrees, double refLatitude,
			double refLongitude) {
		double lat1 = Math.toRadians(refLatitude); // Current lat point
													// converted to radians
		double lon1 = Math.toRadians(refLongitude); // Current long point
													// converted to radians
		double radius = getEarthRadiusForLatitude(lat1); // #Radius of the Earth
		
		azimuthAngleInDegrees = Math.toRadians(azimuthAngleInDegrees);

		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(distanceInMeters / radius)
				+ Math.cos(lat1) * Math.sin(distanceInMeters / radius) * Math.cos(azimuthAngleInDegrees));

		double lon2 = lon1
				+ Math.atan2(Math.sin(azimuthAngleInDegrees) * Math.sin(distanceInMeters / radius) * Math.cos(lat1),
						Math.cos(distanceInMeters / radius) - Math.sin(lat1) * Math.sin(lat2));

		return new double[] { Math.toDegrees(lat2), Math.toDegrees(lon2) };
	}

	private static double getEarthRadiusForLatitude(double latitude) {
		double equatorRadius = 6378.137; // equatorial radius in km
		double polarRadius = 6356.7523142; // polar radius in km
		return equatorRadius
				* Math.sqrt(Math.pow(polarRadius, 4) / Math.pow(equatorRadius, 4) * Math.pow((Math.sin(latitude)), 2)
						+ Math.pow(Math.cos(latitude), 2))
				/ Math.sqrt(1 - (1 - (polarRadius * polarRadius) / (equatorRadius * equatorRadius))
						* Math.pow(Math.sin(latitude), 2));
	}
}
