package uk.ac.gla.get2gether;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class AndroidDashboardDesignActivity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_dashboard_layout);
        
        /**
         * Creating all buttons instances
         * */
        // Get2gether button
        Button btn_get2gether = (Button) findViewById(R.id.btn_get2gether);
        
        // Navig8 button
        Button btn_navig8 = (Button) findViewById(R.id.btn_navig8);
        
        /**
         * Handling all button click events
         * */
        
        btn_navig8.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent in = new Intent(getApplicationContext(), Map.class);
				startActivity(in);				
			}
		});
        
        // Listening to Get2gether button click
        btn_get2gether.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				Log.w("", "Please decouple so we can start FB");
				// Launching Get2gether Screen
				Intent i = new Intent(getApplicationContext(), G2G_Activity.class);
				startActivity(i);
				
				// Toggle the button state.
				// If coming from login transition to logout.
			}
		});
        
    }
}