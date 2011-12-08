package uk.ac.gla.get2gether.pathcalc;

import java.io.IOException;
import java.util.List;

import org.idansof.otp.client.Geocoder;
import org.idansof.otp.client.GeocoderResult;
import org.idansof.otp.client.Location;
import org.mapsforge.android.maps.GeoPoint;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

public class Point {
	public GeoPoint latlng;
	public String comment;
	
	Point(int lat, int lng, String comment) {
		latlng = new GeoPoint(lat, lng);
		this.comment = comment;
	}
	
	Point(Context c, String addr, String comment) {
		final String ad_parm = addr;
		
		Runnable decoder = new Runnable() {
			public void run() {
				List<Location> locs;
				Geocoder g = new Geocoder("spurga.numeris.lt:8888",
						"opentripplanner-geocoder/geocode");
				try {
					GeocoderResult res = g.geodecode(ad_parm);
					locs = res.getLocations();
					int lat = (int) (locs.get(0).getLatitude() * 1e6);
					int lng = (int) (locs.get(0).getLongitude() * 1e6);
					latlng = new GeoPoint(lat, lng); 
					System.out.println(lat+" "+lng);
				} catch (IOException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
	
					throw new RuntimeException("Could not decode address");
				} catch (XmlPullParserException e) {
					
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread decoder_t = new Thread(decoder);
		decoder_t.start();
		try {
			decoder_t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// I would normally call the Point constructor here...
		// Stupid java.
		
		this.comment = comment;
	}
}
