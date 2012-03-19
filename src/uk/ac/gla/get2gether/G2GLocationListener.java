package uk.ac.gla.get2gether;

import org.mapsforge.android.maps.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

public class G2GLocationListener implements LocationListener {
	
	private final Map map;
	
	G2GLocationListener (Map m) {
		map = m;
	}
	
	@Override
	public void onLocationChanged(Location l) {
		GeoPoint point = new GeoPoint(l.getLatitude(), l.getLongitude());
        map.overlayCircle.setCircleData(point, l.getAccuracy());
        map.overlayItem.setPoint(point);
        map.circleOverlay.requestRedraw();
        map.itemizedOverlay.requestRedraw();
        map.currentLocation = l;
        map.start = new org.idansof.otp.client.Location(l.getProvider(),
				l.getLatitude(),
				l.getLongitude());
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}

}
