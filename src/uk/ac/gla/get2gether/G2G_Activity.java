package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class G2G_Activity extends Activity {

	private Handler mHandler;
	TextView footer;
	Facebook mFacebook;
	AsyncFacebookRunner asyncRunner;
	ImageView profilePic;

	private static final String[] PERMISSIONS = new String[] { "email",
			"offline_access", "publish_stream",
			"read_stream", "offline_access", "user_events", "create_event", "rsvp_event" };

	/**
	 * Code to be executed when control returns to this Activity
	 * 
	 * @param requestCode
	 *            the code which identifies the Activity which was performed
	 *            prior to returning here
	 * @param resultCode
	 *            code identifying whether the previous Activity was performed
	 *            successfully
	 * @param data
	 *            data passed from the previous Activity
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("get2gether Main", "onActivityResult(): " + requestCode);
		

		// The following method has to be called when returning to the Activity
		// (it's a bit vague what it does but it's in the Facebook specs)
		mFacebook.authorizeCallback(requestCode, resultCode, data);
	}
	
	private void styleButton(Button bt, Typeface tf){
		bt.setTypeface(tf);
		bt.setTextSize(35);
		//bt.setTextColor(color);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.g2g_dashboard_layout);

		Typeface green_pillow = Typeface.createFromAsset(getAssets(), "GREENPIL.otf");
		((TextView) findViewById(R.id.get)).setTypeface(green_pillow);
		((TextView) findViewById(R.id.together)).setTypeface(green_pillow);
		
		Log.i("G2G_Activity", "Started");

		
		// XXX maybe a better way??
		SharedPreferences settings = getSharedPreferences("get2gether", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean("onEditMode", false);
		editor.commit();

		mHandler = new Handler();
		footer = (TextView) findViewById(R.id.g2g_footer);
		profilePic = (ImageView) findViewById(R.id.profile_pic);

		/**
		 * Creating all buttons instances
		 * */
		
		
		// Dashboard Events button
		Button btn_events = (Button) findViewById(R.id.btn_events);
		styleButton(btn_events, green_pillow);//, R.color.black);
		// Dashboard Current Event button
		Button btn_current = (Button) findViewById(R.id.btn_current);
		styleButton(btn_current, green_pillow);//, R.color.black);
		// Dashboard Statistics button
		Button btn_statistics = (Button) findViewById(R.id.btn_statistics);
		styleButton(btn_statistics, green_pillow);//, R.color.black);
		// Dashboard Options button
		Button btn_options = (Button) findViewById(R.id.btn_options);
		styleButton(btn_options, green_pillow);//, R.color.black);
		
		//Logout Button
		ImageButton btn_logout = (ImageButton) findViewById(R.id.btn_logout);
		//NEED TO IMPLEMENT

		Utility.setResources(getResources());
		mFacebook = Utility.getFacebook();
		asyncRunner = Utility.getAsyncRunner();

		// Login/logout button functionality
		// if (mFacebook.isSessionValid()) {
		// // asyncRunner = new AsyncFacebookRunner(
		// // mFacebook);
		// asyncRunner.logout(this.getBaseContext(),
		// new LogoutRequestListener());
		// } else {
		// Toggle the button state.
		// If coming from logout transition to login (authorize).
		mFacebook.authorize(this, PERMISSIONS, new LoginDialogListener());
		// }
		btn_events.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(getApplicationContext(), EventsActivity.class);
				//i.setClass(G2G_Activity.this, GetEvents.class);
				Log.i("G2G_Activity", "Starting GetEvents Activity");
				startActivity(i);
			}
		});

		btn_statistics.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent();
				i.setClass(G2G_Activity.this, StatisticsActivity.class);
				Log.i("G2G_Activity", "Starting Statistics Activity");
				startActivity(i);
			}
		});

		btn_options.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 Intent i = new Intent();
				 i.setClass(G2G_Activity.this, OptionsActivity.class);
				 startActivity(i);
				Log.i("G2G_Activity", "Not implemented");
				// startActivity(i);
			}
		});
		
		btn_current.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 Intent i = new Intent();
				 i.setClass(G2G_Activity.this, Map.class);
				 SharedPreferences settings = getSharedPreferences("get2gether",
							0);
				 String eventID= settings.getString("eventID", null);
				 //TODO: get the event object (Kostis)
				 /*
				Event e = null;
				Bundle bundle = new Bundle();
				bundle.putSerializable("startTime", event.startTime);
				bundle.putDouble("latitude", event.latitude);
				bundle.putDouble("longitude", event.longitude); */
				 startActivity(i);
			}
		});

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

			// Log.i("FB ID: ", values.getString("id"));

			Log.i("LoginDialogListener", "onComplete");

			// Dispatch on its own thread
			mHandler.post(new Runnable() {
				public void run() {
					footer.setText("Facebook login successful.");
				}
			});

			Bundle params = new Bundle();
			params.putString("fields", "id,first_name");
			asyncRunner.request("me", params, new RequestListener() {

				@Override
				public void onMalformedURLException(MalformedURLException e,
						Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onIOException(IOException e, Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onFileNotFoundException(FileNotFoundException e,
						Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onFacebookError(FacebookError e, Object state) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onComplete(String response, Object state) {
					Log.i("id, first_name FB response", response);

					// JSONArray jarray = new JSONA
					try {
						JSONObject json = new JSONObject(response);
						String id = json.getString("id");

						SharedPreferences settings = getSharedPreferences(
								"get2gether", 0);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString("facebookID", id);
						editor.commit();

						final String firstName = json.getString("first_name");

						URL profilePicURL = new URL(
								"http://graph.facebook.com/" + id
										+ "/picture?type=normal");
						final Bitmap profilePicBitmap = BitmapFactory
								.decodeStream(profilePicURL.openConnection()
										.getInputStream());

						mHandler.post(new Runnable() {
							public void run() {
								profilePic.setImageBitmap(profilePicBitmap);
								footer.setText("Welcome " + firstName);
							}
						});

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// Dispatch on its own thread
					mHandler.post(new Runnable() {
						public void run() {
						}
					});
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