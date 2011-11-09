package uk.ac.gla.route2go;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

//import com.facebook.android.
import android.os.Bundle;

public class Main extends MapActivity{
   public static final String APP_ID = "";
   private Facebook mFacebook;
   private AsyncFacebookRunner mAsyncRunner;

   /** Called when the activity is first created. */
   @Override
   protected boolean isRouteDisplayed() {
       return false;
   }
   @Override
   public void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.main);
    
        mFacebook = new Facebook(APP_ID);
        mAsyncRunner = new AsyncFacebookRunner(mFacebook);

       MapView mapView = (MapView) findViewById(R.id.mapview);
       mapView.setBuiltInZoomControls(true);
   }
}
