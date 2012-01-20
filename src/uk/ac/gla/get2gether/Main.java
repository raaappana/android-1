package uk.ac.gla.get2gether;

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
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
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
	private ListView listView;
	private FriendsArrayAdapter friendsArrayAdapter;
	private final ArrayList<Friend> friends = new ArrayList<Friend>();
	private ProgressDialog mSpinner;
	private Facebook mFacebook;
	private AsyncFacebookRunner mAsyncRunner;
	private Handler mHandler = new Handler();
	private String lastEventID;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Get the status text line resource
		mText = (TextView) Main.this.findViewById(R.id.txt);

		// Setup the ListView Adapter that is loaded when selecting
		// "get friends"
		listView = (ListView) findViewById(R.id.friendsview);
//		friendsArrayAdapter = new FriendsArrayAdapter(this, R.layout.rowlayout,
//				friends);
		listView.setAdapter(friendsArrayAdapter);

		// Define a spinner used when loading the friends over the network
		mSpinner = new ProgressDialog(listView.getContext());
		mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
		mSpinner.setMessage("Loading..");

		mFacebook = new Facebook(getResources().getString(R.string.fb_appid));
		mAsyncRunner = new AsyncFacebookRunner(mFacebook);
		
//		Utility.setResources(getResources());
//		mFacebook = Utility.getFacebook();
//		mAsyncRunner = Utility.getAsyncRunner();

		/*
		 * facebook.authorize(this, PERMISSIONS, new DialogListener() {
		 * 
		 * @Override public void onComplete(Bundle values) { }
		 * 
		 * @Override public void onFacebookError(FacebookError error) { }
		 * 
		 * @Override public void onError(DialogError e) { }
		 * 
		 * @Override public void onCancel() { } });
		 */
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		Log.d("get2gether Main", "onActivityResult(): " + requestCode);
		mFacebook.authorizeCallback(requestCode, resultCode, data);
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
				
//				setContentView(R.layout.main);

				Main.this.runOnUiThread(new Runnable() {
					public void run() {
						Log.i("FriendsRequestListener", "run() is running");
						friendsArrayAdapter = new FriendsArrayAdapter(
								Main.this, R.layout.rowlayout, friends);
						listView.setAdapter(friendsArrayAdapter);
						friendsArrayAdapter.notifyDataSetChanged();
						listView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> arg0,
									View v, int position, long arg3) {
								Bundle params = new Bundle();
								mAsyncRunner.request("/" + lastEventID + "/invited/" + friends.get(position).id, params, "POST", new RequestListener() {
									
									@Override
									public void onMalformedURLException(MalformedURLException e, Object state) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onIOException(IOException e, Object state) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onFileNotFoundException(FileNotFoundException e, Object state) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onFacebookError(FacebookError e, Object state) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onComplete(String response, Object state) {
										// TODO Auto-generated method stub
										
									}
								}, new Object());

							}

						});
					}
				});
			} catch (JSONException e) {
				Log.w("get2gether FB", "JSON Error in response");
			}
		}

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

		if (mFacebook.isSessionValid()) {
			loginItem.setTitle("Logout");
			postItem.setEnabled(true);
			getFriendItem.setEnabled(true);
			sendRequestItem.setEnabled(true);
			createEventItem.setEnabled(true);
		} else {
			loginItem.setTitle("Login");
			postItem.setEnabled(false);
			getFriendItem.setEnabled(false);
			sendRequestItem.setEnabled(false);
			createEventItem.setEnabled(false);
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
		case R.id.getfriends:
			// Get the authenticated user's friends
			mSpinner.show();
			mAsyncRunner.request("me/friends", new FriendsRequestListener());
			break;

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
//			Intent intent = new Intent();
//			intent.pu
			
			setContentView(R.layout.create_event);
			final EditText name = (EditText) findViewById(R.id.meetup_name);
			final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
			final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
			Button arrange = (Button) findViewById(R.id.arrange_button);
			arrange.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Bundle eventParams = new Bundle();
					eventParams.putString("name", name.getEditableText()
							.toString());
					eventParams.putString(
							"start_time",
							getDateTimeString(datePicker.getYear(),
									datePicker.getMonth() + 1,
									datePicker.getDayOfMonth(),
									timePicker.getCurrentHour(),
									timePicker.getCurrentMinute()));
					// eventParams.putString(
					// "end_time",
					// getDateTimeString(datePicker.getYear(),
					// datePicker.getMonth() + 1,
					// datePicker.getDayOfMonth(),
					// timePicker.getCurrentHour() + 1,
					// timePicker.getCurrentMinute()));
					mAsyncRunner.request("me/events", eventParams, "POST",
							new RequestListener() {

								@Override
								public void onMalformedURLException(
										MalformedURLException e, Object state) {
									Log.e("MALFORMED URL", "" + e.getMessage());
								}

								@Override
								public void onIOException(IOException e,
										Object state) {
									Log.e("IOEX", "" + e.getMessage());
								}

								@Override
								public void onFileNotFoundException(
										FileNotFoundException e, Object state) {
									Log.e("FILENOTFOUNDEX", "" + e.getMessage());
								}

								@Override
								public void onFacebookError(FacebookError e,
										Object state) {
									Toast.makeText(
											getApplicationContext(),
											"Facebook Error: " + e.getMessage(),
											Toast.LENGTH_SHORT).show();
								}

								@Override
								public void onComplete(String response,
										Object state) {

									try {
										JSONObject jo = new JSONObject(response);
										lastEventID = jo.getString("id");
										Log.i("LAST EVENT ID", lastEventID);
									} catch (JSONException e) {
										e.printStackTrace();
									}
									// Toast toast =
									// Toast.makeText(getApplicationContext(),
									// "Event created", Toast.LENGTH_SHORT);
									// toast.show();
//									mSpinner.show();
									mAsyncRunner.request("me/friends", new FriendsRequestListener());
								}
							}, new Object());
				}
			});
			//setContentView(R.layout.main);
//			mSpinner.show();
//			mAsyncRunner.request("me/friends", new FriendsRequestListener());
			break;
		default:
			return false;
		}
		return true;
	}

	/**
	 * @return the ISO-8601 formated date/time, used in FB events
	 */
	private String getDateTimeString(int year, int month, int day, int hour,
			int minute) {
		String s = Integer.toString(year) + "-" + addLeadingZeros(month) + "-"
				+ addLeadingZeros(day) + "T" + addLeadingZeros(hour) + ":"
				+ addLeadingZeros(minute) + ":00";
		Log.i("DATE/TIME STRING", s);
		return s;
	}

	private String addLeadingZeros(int i) {
		if (i > 10)
			return Integer.toString(i);
		else
			return "0" + i;
	}

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
