package uk.ac.gla.get2gether;

import org.mapsforge.android.maps.ArrayItemizedOverlay;
import org.mapsforge.android.maps.GeoPoint;
import org.mapsforge.android.maps.MapView;
import org.mapsforge.android.maps.OverlayItem;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;

public class G2GItemizedOverlay extends ArrayItemizedOverlay {

	Map map;
	
	public G2GItemizedOverlay(Drawable defaultMarker, Context context) {
		super(defaultMarker, context);
		map = (Map) context;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onLongPress(GeoPoint p, MapView mapView) {
		 // TODO Auto-generated method stub
		map.launchDestinationDialog(p);
		
		 return true;
	}

}
