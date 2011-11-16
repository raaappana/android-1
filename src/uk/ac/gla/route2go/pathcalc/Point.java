package uk.ac.gla.route2go.pathcalc;

import java.io.IOException;
import java.util.List;

import com.google.android.maps.GeoPoint;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

public class Point {
	public GeoPoint latlng;
	public String comment;
	
	Point(int lat, int lng, String comment) {
		latlng = new GeoPoint(lat, lng);
		this.comment = comment;
	}
	
	Point(Context c, String addr, String comment) {
		Geocoder g = new Geocoder(c);
		List<Address> address;
		try {
			address = g.getFromLocationName(addr, 1);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not fetch address");
		}
		int lat = (int) (address.get(0).getLatitude() * 1e6);
		int lng = (int) (address.get(0).getLongitude() * 1e6);
		// I would normally call the Point constructor here...
		// Stupid java.
		
		this.comment = comment;
		latlng = new GeoPoint(lat, lng); 
	}
}