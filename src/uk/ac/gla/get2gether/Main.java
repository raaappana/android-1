package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;

public class Main extends Activity {

	private static final String[] PERMISSIONS = new String[] { "email",
			"offline_access", "publish_checkins", "publish_stream",
			"read_stream", "offline_access", "user_events", "create_event" };
	private TextView mText;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler = new Handler();
	private String eventID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Main Activity", "Started");
		setContentView(R.layout.main);

		// Get the status text line resource
		mText = (TextView) Main.this.findViewById(R.id.txt);

		// Define a spinner used when loading the friends over the network
//		mSpinner = new ProgressDialog(listView.getContext());
//		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		mSpinner.setMessage("Loading..");

		Utility.setResources(getResources());
		mFacebook = Utility.getFacebook();
		mAsyncRunner = Utility.getAsyncRunner();
	}

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
		if (requestCode == Utility.CREATE_EVENT_CODE) {
			Log.i("Main Activity", "CreateEvent Activity returned");
		}
	}

	/**
	 * WallPostRequestListener implements a request lister/callback for
	 * "wall post requests"
	 * 
	 * WE PROBABLY WON'T NEED THIS
	 */
	public class WallPostRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * Called when the wall post request has completed
		 */
		@Override
		public void onComplete(String response, Object state) {
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

	/**
	 * WallPostDialogListener implements a dialog lister/callback
	 * 
	 * NEITHER THIS
	 */
	public class WallPostDialogListener implements
			com.facebook.android.Facebook.DialogListener {

		/**
		 * Called when the dialog has completed successfully
		 */
		@Override
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Log.d("get2gether FB", "Dialog Success! post_id=" + postId);
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
				Log.d("get2gether FB", "No wall post made");
			}

		}

		@Override
		public void onFacebookError(FacebookError e) {
		}

		@Override
		public void onError(DialogError e) {
		}

		@Override
		public void onCancel() {
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
			Log.d("get2gether FB", "LoginDialogListener.onComplete()");
			// Dispatch on its own thread
			mHandler.post(new Runnable() {
				public void run() {
					mText.setText("Facebook login successful. Press Menu...");
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

	private class LogoutRequestListener implements RequestListener {

		/** Called when the request completes w/o error */
		@Override
		public void onComplete(String response, Object state) {
			// Only the original owner thread can touch its views
			Main.this.runOnUiThread(new Runnable() {
				public void run() {
					mText.setText("Thanks for using get2gether FB activity. Bye bye..");
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

	/**
	 * Invoked at the time to create the menu
	 * 
	 * @param menu
	 *            is the menu to create
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Invoked when preparing to display the menu
	 * 
	 * @param menu
	 *            is the menu to prepare
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(R.id.login);
		MenuItem postItem = menu.findItem(R.id.wallpost);
		MenuItem getFriendItem = menu.findItem(R.id.getfriends);
		MenuItem sendRequestItem = menu.findItem(R.id.sendreq);
		MenuItem createEventItem = menu.findItem(R.id.createev);
		MenuItem manageEventItem = menu.findItem(R.id.manageev);

		if (mFacebook.isSessionValid()) {
			loginItem.setTitle("Logout");
			postItem.setEnabled(true);
			getFriendItem.setEnabled(true);
			sendRequestItem.setEnabled(true);
			createEventItem.setEnabled(true);
			manageEventItem.setEnabled(true);
		} else {
			loginItem.setTitle("Login");
			postItem.setEnabled(false);
			getFriendItem.setEnabled(false);
			sendRequestItem.setEnabled(false);
			createEventItem.setEnabled(true);
			manageEventItem.setEnabled(false);
		}

		loginItem.setEnabled(true);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Invoked when a menu item has been selected
	 * 
	 * @param item
	 *            is the selected menu items
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Login/logout toggle
		case R.id.login:
			// Toggle the button state.
			// If coming from login transition to logout.
			if (mFacebook.isSessionValid()) {
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(
						mFacebook);
				asyncRunner.logout(this.getBaseContext(),
						new LogoutRequestListener());
			} else {
				// Toggle the button state.
				// If coming from logout transition to login (authorize).
				mFacebook.authorize(this, PERMISSIONS,
						new LoginDialogListener());
			}
			break;

		// Wall Post
		case R.id.wallpost: // Wall Post
			mFacebook.dialog(Main.this, "stream.publish",
					new WallPostDialogListener());
			break;

		// Get Friend's List
//		case R.id.getfriends:
//			// Get the authenticated user's friends
//			mAsyncRunner.request("me/friends", new FriendsRequestListener());
//			break;

		case R.id.showmap:
			Intent in = new Intent();
			in.setClass(this, Map.class);
			startActivity(in);
			break;
		case R.id.sendreq:
			Bundle params = new Bundle();
			params.putString("message", "Let's meet at Firwood Lake");
			mFacebook.dialog(Main.this, "apprequests", params,
					new AppRequestsListener());
			break;
		case R.id.createev:
			Intent intent = new Intent();
			intent.setClass(this, CreateEvent.class);
			Log.i("Main Activity", "Starting CreateEvent Activity");
			startActivityForResult(intent, Utility.CREATE_EVENT_CODE);
			break;
		case R.id.manageev :
			mAsyncRunner.request("me/events", new EventRequestListener());
			break;
		default:
			return false;
		}
		return true;
	}
	
	
	/**
	 * 
	 * @author dNd
	 *
	 * This class will be used for getting a list of compatible events
	 */
	private class EventRequestListener implements RequestListener {

		@Override
		public void onComplete(String arg0, Object arg1) {
			
			
		}

		@Override
		public void onFacebookError(FacebookError arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException arg0,
				Object arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onIOException(IOException arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException arg0,
				Object arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}

	/**
	 * 
	 * @author dNd
	 *
	 * NOT SURE IF WE'RE IMPLEMENTING THIS
	 */
	public class AppRequestsListener extends BaseDialogListener {

		@Override
		public void onComplete(Bundle values) {
			Toast toast = Toast.makeText(getApplicationContext(),
					"App request sent", Toast.LENGTH_SHORT);
			toast.show();
		}

		@Override
		public void onFacebookError(FacebookError error) {
			Toast.makeText(getApplicationContext(),
					"Facebook Error: " + error.getMessage(), Toast.LENGTH_SHORT)
					.show();
		}

		@Override
		public void onCancel() {
			Toast toast = Toast.makeText(getApplicationContext(),
					"App request cancelled", Toast.LENGTH_SHORT);
			toast.show();
		}

	}

}
