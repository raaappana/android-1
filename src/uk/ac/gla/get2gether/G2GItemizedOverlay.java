package uk.ac.gla.get2gether;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;

import android.content.Context;
import android.graphics.drawable.Drawable;

public class G2GItemizedOverlay extends ArrayItemizedOverlay {

	Map map;
	
	public G2GItemizedOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker, context);
		map = (Map) context;
	}
	
	@Override
	public boolean onLongPress(GeoPoint p, MapView mapView) {
		map.launchDestinationDialog(p);		
		 return true;
	}

}
