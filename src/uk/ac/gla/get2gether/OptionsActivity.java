package uk.ac.gla.get2gether;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class OptionsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.g2g_options);
		
		Typeface green_pillow = Typeface.createFromAsset(getAssets(), "GREENPIL.otf");
		((TextView)findViewById(R.id.get)).setTypeface(green_pillow);
		((TextView)findViewById(R.id.together)).setTypeface(green_pillow);
	}

}
