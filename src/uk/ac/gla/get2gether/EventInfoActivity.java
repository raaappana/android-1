package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class EventInfoActivity extends Activity {

	private Event event;
	private AsyncFacebookRunner mAsyncRunner;
	private ListView friendsListView;
	private List<String> invitedNames;
	private ArrayAdapter<String> arrayAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("EventInfoActivity", "Started");

		setContentView(R.layout.event_info);

		Button useButton = (Button) findViewById(R.id.eventinfo_use_button);
		useButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i("EventInfoActivity", "Event clicked: " + event.id + ", "
						+ event.name);
				SharedPreferences settings = getSharedPreferences("get2gether",
						0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("eventID", event.id);
				editor.commit();
				Bundle bundle = new Bundle();
				bundle.putSerializable("startTime", event.startTime);
				//bundle.putString("address", event.address);
				bundle.putDouble("latitude", event.latitude);
				bundle.putDouble("longitude", event.longitude);
				Intent i = new Intent(getApplicationContext(), Map.class);
				i.putExtras(bundle);
				startActivity(i);

				finish();
			}
		});

		Button editButton = (Button) findViewById(R.id.eventinfo_edit_button);
		editButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i("EventInfoActivity", "Event clicked: " + event.id + ", "
						+ event.name);
				mAsyncRunner.request("/" + Utility.getEvent().id, new RequestListener() {
					
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
						Log.i("Event details response", response);
						
						String ownerID = "";
						try {
							JSONObject json = new JSONObject(response);
							JSONObject ownerObj;
							ownerObj = json.getJSONObject("owner");
							ownerID = ownerObj.getString("id");
						} catch (JSONException e) {
							e.printStackTrace();
						}
						
						Log.i("Event owner ID", ownerID);
						SharedPreferences settings = getSharedPreferences("get2gether",
								0);
						String currentUserID = settings.getString("facebookID", "");
						Log.i("Current user id", currentUserID);
						if (ownerID.equals(currentUserID)) {
							
							SharedPreferences.Editor editor = settings.edit();
							editor.putBoolean("onEditMode", true);
							editor.commit();
							
							Intent i = new Intent(EventInfoActivity.this, CreateEvent.class);
							startActivity(i);
							finish();
							
						} else {
							showToast("You have to be the owner of the event in order to edit it");
						}
					}
				});
			}
		});
		
		Button deleteButton = (Button) findViewById(R.id.eventinfo_delete_button);
		deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Bundle params = new Bundle();
				mAsyncRunner.request("/" + event.id, params, "DELETE", new RequestListener() {
					
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
						Log.i("Delete response", response);
						finish();
					}
				}, null);
			}
		});

		TextView name = (TextView) findViewById(R.id.eventinfo_name_textView);
		TextView locationName = (TextView) findViewById(R.id.eventinfo_location_name_textview);
		TextView address = (TextView) findViewById(R.id.eventinfo_address_textview);
		TextView time = (TextView) findViewById(R.id.eventinfo_time_textview);
		TextView description = (TextView) findViewById(R.id.eventinfo_description);
		friendsListView = (ListView) findViewById(R.id.eventinfo_friend_listview);

		invitedNames = new ArrayList<String>();
		arrayAdapter = new ArrayAdapter<String>(EventInfoActivity.this,
				android.R.layout.simple_list_item_1, invitedNames);
		friendsListView.setAdapter(arrayAdapter);

		event = Utility.getEvent();

		name.setText(event.name);
		locationName.setText(event.locationName);
		address.setText(event.address);
		time.setText(event.startTime.toLocaleString());
		description.setText(event.description);

		mAsyncRunner = Utility.getAsyncRunner();
		mAsyncRunner.request(event.id + "/invited", new RequestListener() {

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
				try {
					// process the response here: executed in background thread
					Log.d("Facebook-Example-Friends Request",
							"response.length(): " + response.length());
					Log.d("Facebook-Example-Friends Request", "Response: "
							+ response);

					final JSONObject json = new JSONObject(response);
					JSONArray jsonAr = json.getJSONArray("data");
					int len = (jsonAr != null ? jsonAr.length() : 0);
					Log.d("Facebook-Example-Friends Request", "d.length(): "
							+ len);

					event.invitedList = new ArrayList<Friend>();
					for (int i = 0; i < len; i++) {
						JSONObject jsonObj = jsonAr.getJSONObject(i);
						String name = jsonObj.getString("name");
						String id = jsonObj.getString("id");
						Friend f = new Friend();
						f.id = id;
						f.name = name;
						event.invitedList.add(f);
					}
				} catch (JSONException e) {
					Log.w("get2gether FB", "JSON Error in response");
				}

				for (int i = 0; i < event.invitedList.size(); i++) {
					invitedNames.add(event.invitedList.get(i).name);
				}

				EventInfoActivity.this.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						arrayAdapter.notifyDataSetChanged();
					}
				});
				// friendsListView.setAdapter(new ArrayAdapter<String>(
				// EventInfoActivity.this,
				// android.R.layout.simple_list_item_1, invitedNames));
			}
		});

	}
	
	private void showToast(final String text) {
		if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
			Toast toast = Toast.makeText(EventInfoActivity.this, text, Toast.LENGTH_LONG);
			toast.show();
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast toast = Toast.makeText(EventInfoActivity.this, text,
							Toast.LENGTH_LONG);
					toast.show();
				}
			});
		}
	}

}
