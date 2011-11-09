package uk.ac.gla.route2go;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import android.os.Bundle;


public class Map extends MapActivity{
   /** Called when the activity is first created. */
   @Override
   protected boolean isRouteDisplayed() {
       return false;
   }
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.map);
    
       MapView mapView = (MapView) findViewById(R.id.mapview);
       mapView.setBuiltInZoomControls(true);
   }
}
