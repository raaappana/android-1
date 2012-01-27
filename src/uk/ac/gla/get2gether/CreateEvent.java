package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.FacebookError;
import com.facebook.android.AsyncFacebookRunner.RequestListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CreateEvent extends Activity {

	private String eventID;
	private AsyncFacebookRunner mAsyncRunner;
	private ArrayList<Friend> friends;
	private FriendsArrayAdapter friendsArrayAdapter;
	private ListView listView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_event);
		
		mAsyncRunner = Utility.getAsyncRunner();
		friends = new ArrayList<Friend>();
		listView = (ListView) findViewById(R.id.friendsview);
		friendsArrayAdapter = new FriendsArrayAdapter(this, R.layout.rowlayout,
				friends);
//		listView.setAdapter(friendsArrayAdapter);
		
		final EditText name = (EditText) findViewById(R.id.meetup_name);
		final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
		Button arrange = (Button) findViewById(R.id.arrange_button);
		arrange.setOnClickListener(new OnClickListener() {

			/**
			 * @return the ISO-8601 formated date/time, used in FB events
			 */
			private String getDateTimeString(int year, int month, int day,
					int hour, int minute) {
				String s = Integer.toString(year) + "-"
						+ addLeadingZeros(month) + "-" + addLeadingZeros(day)
						+ "T" + addLeadingZeros(hour) + ":"
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

			@Override
			public void onClick(View v) {
				Bundle eventParams = new Bundle();
				eventParams
						.putString("name", name.getEditableText().toString());
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
								Toast.makeText(getApplicationContext(),
										"Facebook Error: " + e.getMessage(),
										Toast.LENGTH_SHORT).show();
							}

							@Override
							public void onComplete(String response, Object state) {

								try {
									JSONObject jo = new JSONObject(response);
									eventID = jo.getString("id");
									Log.i("LAST EVENT ID", eventID);
									
									Intent i = new Intent();
									i.putExtra("eventid", eventID);
									setResult(Activity.RESULT_OK, i);
									finish();
//									 mAsyncRunner.request("me/friends", new FriendsRequestListener());
								} catch (JSONException e) {
									e.printStackTrace();
								}
								// Toast toast =
								// Toast.makeText(getApplicationContext(),
								// "Event created", Toast.LENGTH_SHORT);
								// toast.show();
							}
						}, new Object());
			}
		});
		// setContentView(R.layout.main);
		// mSpinner.show();

	}

//	public class FriendsRequestListener implements
//			com.facebook.android.AsyncFacebookRunner.RequestListener {
//
//		/**
//		 * Called when the request to get friends has been completed. Retrieve
//		 * and parse and display the JSON stream.
//		 */
//		@Override
//		public void onComplete(final String response, Object state) {
//			// mSpinner.dismiss();
//			try {
//				// process the response here: executed in background thread
//				Log.d("Facebook-Example-Friends Request", "response.length(): "
//						+ response.length());
//				Log.d("Facebook-Example-Friends Request", "Response: "
//						+ response);
//
//				final JSONObject json = new JSONObject(response);
//				JSONArray jsonAr = json.getJSONArray("data");
//				int len = (jsonAr != null ? jsonAr.length() : 0);
//				Log.d("Facebook-Example-Friends Request", "d.length(): " + len);
//
//				for (int i = 0; i < len; i++) {
//					JSONObject jsonObj = jsonAr.getJSONObject(i);
//					String name = jsonObj.getString("name");
//					String id = jsonObj.getString("id");
//					Friend f = new Friend();
//					f.id = id;
//					f.name = name;
//					friends.add(f);
//				}
//
//				// setContentView(R.layout.main);
//
//				CreateEvent.this.runOnUiThread(new Runnable() {
//					public void run() {
//						Log.i("FriendsRequestListener", "run() is running");
//						friendsArrayAdapter = new FriendsArrayAdapter(
//								CreateEvent.this, R.layout.rowlayout, friends);
//						listView.setAdapter(friendsArrayAdapter);
//						friendsArrayAdapter.notifyDataSetChanged();
//						listView.setOnItemClickListener(new OnItemClickListener() {
//
//							@Override
//							public void onItemClick(AdapterView<?> arg0,
//									View v, int position, long arg3) {
//								Bundle params = new Bundle();
//								mAsyncRunner.request("/" + lastEventID
//										+ "/invited/"
//										+ friends.get(position).id, params,
//										"POST", new RequestListener() {
//
//											@Override
//											public void onMalformedURLException(
//													MalformedURLException e,
//													Object state) {
//												// TODO Auto-generated method
//												// stub
//
//											}
//
//											@Override
//											public void onIOException(
//													IOException e, Object state) {
//												// TODO Auto-generated method
//												// stub
//
//											}
//
//											@Override
//											public void onFileNotFoundException(
//													FileNotFoundException e,
//													Object state) {
//												// TODO Auto-generated method
//												// stub
//
//											}
//
//											@Override
//											public void onFacebookError(
//													FacebookError e,
//													Object state) {
//												// TODO Auto-generated method
//												// stub
//
//											}
//
//											@Override
//											public void onComplete(
//													String response,
//													Object state) {
//												// TODO Auto-generated method
//												// stub
//
//											}
//										}, new Object());
//
//							}
//
//						});
//					}
//				});
//			} catch (JSONException e) {
//				Log.w("get2gether FB", "JSON Error in response");
//			}
//		}
//
//		@Override
//		public void onFacebookError(FacebookError arg0, Object arg1) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onFileNotFoundException(FileNotFoundException arg0,
//				Object arg1) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onIOException(IOException arg0, Object arg1) {
//			// TODO Auto-generated method stub
//
//		}
//
//		@Override
//		public void onMalformedURLException(MalformedURLException arg0,
//				Object arg1) {
//			// TODO Auto-generated method stub
//
//		}
//
//}
}
