package uk.ac.gla.route2go;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

public class Main extends Activity {

	private static final String[] PERMISSIONS = new String[] { "email",
			"offline_access", "publish_checkins", "publish_stream",
			"read_stream", "offline_access" };
	private TextView mText;
	private ListView listView;
	private FriendsArrayAdapter friendsArrayAdapter;
	private final ArrayList<Friend> friends = new ArrayList<Friend>();
	private ProgressDialog mSpinner;
	private Facebook facebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get the status text line resource
		mText = (TextView) Main.this.findViewById(R.id.txt);

		// Setup the ListView Adapter that is loaded when selecting
		// "get friends"
		listView = (ListView) findViewById(R.id.friendsview);
		friendsArrayAdapter = new FriendsArrayAdapter(this, R.layout.rowlayout,
				friends);
		listView.setAdapter(friendsArrayAdapter);

		// Define a spinner used when loading the friends over the network
		mSpinner = new ProgressDialog(listView.getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading..");

		facebook = new Facebook("134601229976419");
		mAsyncRunner = new AsyncFacebookRunner(facebook);

		/*facebook.authorize(this, PERMISSIONS, new DialogListener() {
			@Override
			public void onComplete(Bundle values) {
			}

			@Override
			public void onFacebookError(FacebookError error) {
			}

			@Override
			public void onError(DialogError e) {
			}

			@Override
			public void onCancel() {
			}
		});*/
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("FB Sample App", "onActivityResult(): " + requestCode);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

	public class FriendsRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * Called when the request to get friends has been completed. Retrieve
		 * and parse and display the JSON stream.
		 */
		@Override
		public void onComplete(final String response, Object state) {
			mSpinner.dismiss();
			try {
				// process the response here: executed in background thread
				Log.d("Facebook-Example-Friends Request", "response.length(): "
						+ response.length());
				Log.d("Facebook-Example-Friends Request", "Response: "
						+ response);

				final JSONObject json = new JSONObject(response);
				JSONArray jsonAr = json.getJSONArray("data");
				int len = (jsonAr != null ? jsonAr.length() : 0);
				Log.d("Facebook-Example-Friends Request", "d.length(): " + len);

				for (int i = 0; i < len; i++) {
					JSONObject jsonObj = jsonAr.getJSONObject(i);
					String name = jsonObj.getString("name");
					String id = jsonObj.getString("id");
					Friend f = new Friend();
					f.id = id;
					f.name = name;
					friends.add(f);
				}

				Main.this.runOnUiThread(new Runnable() {
					public void run() {
						friendsArrayAdapter = new FriendsArrayAdapter(
								Main.this, R.layout.rowlayout, friends);
						listView.setAdapter(friendsArrayAdapter);
						friendsArrayAdapter.notifyDataSetChanged();
					}
				});
			} catch (JSONException e) {
				Log.w("Facebook-Example", "JSON Error in response");
			}
		}

//		public void onFacebookError(FacebookError e) {
//			// Ignore Facebook errors
//			mSpinner.dismiss();
//		}
//
//		public void onFileNotFoundException(FileNotFoundException e) {
//			// Ignore File not found errors
//			mSpinner.dismiss();
//		}
//
//		public void onIOException(IOException e) {
//			// Ignore IO Facebook errors
//			mSpinner.dismiss();
//		}
//
//		public void onMalformedURLException(MalformedURLException e) {
//			// Ignore Malformed URL errors
//			mSpinner.dismiss();
//		}

//		@Override
//		public void onComplete(String response, Object state) {
//			mSpinner.dismiss();
//		}

		@Override
		public void onIOException(IOException e, Object state) {
			mSpinner.dismiss();
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			mSpinner.dismiss();
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			mSpinner.dismiss();
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			mSpinner.dismiss();
		}
	}

	/**
	 * WallPostRequestListener implements a request lister/callback for
	 * "wall post requests"
	 */
	public class WallPostRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
         * Called when the wall post request has completed
         */
//		@Override
//		public void onComplete(final String response) {
//            Log.d("Facebook-Example", "Got response: " + response);
//		}
		@Override
		public void onComplete(String response, Object state) {
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
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
		
	}

	/**
     * WallPostDialogListener implements a dialog lister/callback
     */
	public class WallPostDialogListener implements com.facebook.android.Facebook.DialogListener {

		
		/**
         * Called when the dialog has completed successfully
         */
		@Override
		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
                Log.d("FB Sample App", "Dialog Success! post_id=" + postId);
				mAsyncRunner.request(postId, new WallPostRequestListener());
			} else {
                Log.d("FB Sample App", "No wall post made");
			}
			
		}

		@Override
		public void onFacebookError(FacebookError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onError(DialogError e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onCancel() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public final class LoginDialogListener implements com.facebook.android.Facebook.DialogListener {

		/**
         * Called when the dialog has completed successfully
         */
		@Override
		public void onComplete(Bundle values) {
			// Process onComplete
            Log.d("FB Sample App", "LoginDialogListener.onComplete()");
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
            Log.d("FB Sample App", "LoginDialogListener.onFacebookError()");
		}

		@Override
		public void onError(DialogError e) {
            // Process error message
            Log.d("FB Sample App", "LoginDialogListener.onError()");
		}

		@Override
		public void onCancel() {
            // Process cancel message
            Log.d("FB Sample App", "LoginDialogListener.onCancel()");
		}		
		
	}
	
	private class LogoutRequestListener implements RequestListener {

		/** Called when the request completes w/o error */
		@Override
		public void onComplete(String response, Object state) {
            // Only the original owner thread can touch its views
			Main.this.runOnUiThread(new Runnable() {
				public void run() {
					mText.setText("Thanks for using Route2Go FB activity. Bye bye..");
					friends.clear();
					friendsArrayAdapter.notifyDataSetChanged();
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFileNotFoundException(FileNotFoundException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onMalformedURLException(MalformedURLException e,
				Object state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onFacebookError(FacebookError e, Object state) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
     * Invoked at the time to create the menu
     * @param menu is the menu to create
     */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}
	
	/**
     * Invoked when preparing to display the menu
     * @param menu is the menu to prepare
     */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem loginItem = menu.findItem(R.id.login);
		MenuItem postItem = menu.findItem(R.id.wallpost);
		MenuItem getFriendItem = menu.findItem(R.id.getfriends);
		
		if (facebook.isSessionValid()) {
			loginItem.setTitle("Logout");
			postItem.setEnabled(true);
			getFriendItem.setEnabled(true);
		} else {
			loginItem.setTitle("Login");
			postItem.setEnabled(false);
			getFriendItem.setEnabled(false);
		}
		
		loginItem.setEnabled(true);
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
     * Invoked when a menu item has been selected
     * @param item is the selected menu items
     */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			// Login/logout toggle
		case R.id.login:
			// Toggle the button state.
            // If coming from login transition to logout.
			if (facebook.isSessionValid()) {
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(facebook);
				asyncRunner.logout(this.getBaseContext(), new LogoutRequestListener());
			} else {
				// Toggle the button state.
                //  If coming from logout transition to login (authorize).
				facebook.authorize(this, PERMISSIONS, new LoginDialogListener());
			}
			break;
			
		// Wall Post
		case R.id.wallpost: // Wall Post
			facebook.dialog(Main.this, "stream.publish", new WallPostDialogListener());
			break;
			
		// Get Friend's List
		case R.id.getfriends:
			// Get the authenticated user's friends
			mSpinner.show();
			mAsyncRunner.request("me/friends", new FriendsRequestListener());
			break;
			
		default:
			return false;
		}
		return true;
	}
	
	
	
	
	
	
}
