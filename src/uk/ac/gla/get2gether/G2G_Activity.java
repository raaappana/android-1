package uk.ac.gla.get2gether;


import uk.ac.gla.get2gether.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class G2G_Activity extends Activity {
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.g2g_dashboard_layout);
        
        /**
         * Creating all buttons instances
         * */
        // Dashboard News feed button
        Button btn_events = (Button) findViewById(R.id.btn_events);
        
        // Dashboard Friends button
        Button btn_current = (Button) findViewById(R.id.btn_current);
        
        // Dashboard Messages button
        Button btn_statistics = (Button) findViewById(R.id.btn_statistics);
        
        // Dashboard Places button
        Button btn_options = (Button) findViewById(R.id.btn_options);
        
        
        
        /**
         * Handling all button click events
         * */
       /* 
        // Listening to Login button click
        btn_login.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), NewsFeedActivity.class);
				startActivity(i);
			}
		});
        
       // Listening Friends button click
        btn_friends.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), FriendsActivity.class);
				startActivity(i);
			}
		});
        
        // Listening Messages button click
        btn_showmap.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), MessagesActivity.class);
				startActivity(i);
			}
		});
        
        // Listening to Places button click
        btn_sendreq.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), PlacesActivity.class);
				startActivity(i);
			}
		});
        
        // Listening to Events button click
        btn_wallpost.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), EventsActivity.class);
				startActivity(i);
			}
		});
        
        // Listening to Photos button click
        btn_create.setOnClickListener(new View.OnClickListener() {
			
			//@Override
			public void onClick(View view) {
				// Launching News Feed Screen
				Intent i = new Intent(getApplicationContext(), PhotosActivity.class);
				startActivity(i);
			}
		}); */
    } 
}