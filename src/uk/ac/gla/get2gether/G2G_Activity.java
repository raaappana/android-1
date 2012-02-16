package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import uk.ac.gla.get2gether.Main.LoginDialogListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

public class G2G_Activity extends Activity {

	private Handler mHandler;
	TextView footer;
	Facebook mFacebook;
	AsyncFacebookRunner asyncRunner;

	private static final String[] PERMISSIONS = new String[] { "email",
			"offline_access", "publish_checkins", "publish_stream",
			"read_stream", "offline_access", "user_events", "create_event" };
	
	/**
	 * Code to be executed when control returns to this Activity
	 * @param requestCode the code which identifies the Activity which was performed prior to returning here
	 * @param resultCode code identifying whether the previous Activity was performed successfully
	 * @param data data passed from the previous Activity
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("get2gether Main", "onActivityResult(): " + requestCode);
		
		// The following method has to be called when returning to the Activity
		// (it's a bit vague what it does but it's in the Facebook specs)
		mFacebook.authorizeCallback(requestCode, resultCode, data);
		}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.g2g_dashboard_layout);
		

		Log.i("G2G_Activity", "Started");

		mHandler = new Handler();
		footer = (TextView) findViewById(R.id.g2g_footer);

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

		Utility.setResources(getResources());
		mFacebook = Utility.getFacebook();
		asyncRunner = Utility.getAsyncRunner();
//		if (mFacebook.isSessionValid()) {
//			// asyncRunner = new AsyncFacebookRunner(
//			// mFacebook);
//			asyncRunner.logout(this.getBaseContext(),
//					new LogoutRequestListener());
//		} else {
			// Toggle the button state.
			// If coming from logout transition to login (authorize).
			mFacebook.authorize(this, PERMISSIONS, new LoginDialogListener());
//		}

		/**
		 * Handling all button click events
		 * */
		/*
		 * // Listening to Login button click btn_login.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * NewsFeedActivity.class); startActivity(i); } });
		 * 
		 * // Listening Friends button click btn_friends.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * FriendsActivity.class); startActivity(i); } });
		 * 
		 * // Listening Messages button click btn_showmap.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * MessagesActivity.class); startActivity(i); } });
		 * 
		 * // Listening to Places button click
		 * btn_sendreq.setOnClickListener(new View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * PlacesActivity.class); startActivity(i); } });
		 * 
		 * // Listening to Events button click
		 * btn_wallpost.setOnClickListener(new View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * EventsActivity.class); startActivity(i); } });
		 * 
		 * // Listening to Photos button click btn_create.setOnClickListener(new
		 * View.OnClickListener() {
		 * 
		 * //@Override public void onClick(View view) { // Launching News Feed
		 * Screen Intent i = new Intent(getApplicationContext(),
		 * PhotosActivity.class); startActivity(i); } });
		 */
	}

	private class LogoutRequestListener implements RequestListener {

		/** Called when the request completes w/o error */
		@Override
		public void onComplete(String response, Object state) {
			// Only the original owner thread can touch its views
			G2G_Activity.this.runOnUiThread(new Runnable() {
				public void run() {

					footer.setText("Thanks for using get2gether FB activity. Bye bye..");
				}
			});

			// Dispatch on its own thread
			mHandler.post(new Runnable() {
				public void run() {
				}
			});

		}

		@Override
		public void onIOException(IOException e, Object state) {
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
		}
	}

	public final class LoginDialogListener implements
			com.facebook.android.Facebook.DialogListener {

		/**
		 * Called when the dialog has completed successfully
		 */
		@Override
		public void onComplete(Bundle values) {
			// Process onComplete

			Log.i("LoginDialogListener", "onComplete");
			// Dispatch on its own thread
			mHandler.post(new Runnable() {
				public void run() {
					footer.setText("Facebook login successful.");
				}
			});
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// Process error
			Log.d("get2gether FB", "LoginDialogListener.onFacebookError()");
		}

		@Override
		public void onError(DialogError e) {
			// Process error message
			Log.d("get2gether FB", "LoginDialogListener.onError()");
		}

		@Override
		public void onCancel() {
			// Process cancel message
			Log.d("get2gether FB", "LoginDialogListener.onCancel()");
		}

	}

}