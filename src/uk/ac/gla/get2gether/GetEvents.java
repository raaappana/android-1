package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class GetEvents extends Activity {

	private EventsArrayAdapter eventsArrayAdapter;
	private ListView listView;
	private ArrayList<Event> events;
	private AsyncFacebookRunner mAsyncRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("GetEvents Activity", "Started");

		setContentView(R.layout.getevents);
		mAsyncRunner = Utility.getAsyncRunner();
		// events = new ArrayList<Event>();
		// listView = (ListView) findViewById(R.id.geteventsview);
		
		Typeface green_pillow = Typeface.createFromAsset(getAssets(), "GREENPIL.otf");
		((TextView) findViewById(R.id.get)).setTypeface(green_pillow);
		((TextView) findViewById(R.id.together)).setTypeface(green_pillow);

		Bundle params = new Bundle();
		params.putString("fields", "location,description,name,id,start_time");

		mAsyncRunner.request("me/events", params, new RequestListener() {

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
			public void onComplete(final String response, Object state) {
				Log.i("events response", response);
				
				events = new ArrayList<Event>();
				
				try {
					JSONObject json = new JSONObject(response);

					JSONArray jsonAr = json.getJSONArray("data");
					int len = (jsonAr != null ? jsonAr.length() : 0);
					Log.d("Facebook Events Request", "d.length(): "
							+ len);

					for (int i = 0; i < len; i++) {
						JSONObject jsonObj = jsonAr.getJSONObject(i);
						if (!jsonObj.getString("name")
								.trim()
								.startsWith("G2G - ")
						|| !jsonObj.has("location")) {
					Log.i("JSON not_replied array", "item incompatible");
					continue;
				}
				String description = "";
				if (jsonObj.has("description"))
					description = jsonObj.getString("description");
				Event e = new Event(jsonObj.getString("id"),
						jsonObj.getString("location"), jsonObj
								.getString("name"), jsonObj
								.getString("start_time"),
						description);
						if (e.isOld())
							continue;
						events.add(e);
						Log.i("Event added", e.toString());
					}
				} catch (JSONException e) {
					Log.w("Get notEvents",
							"JSON Error in response. "
									+ e.getLocalizedMessage());
				}


				// We also need the events to which the user hasn't replied
				mAsyncRunner.request("/me/events/not_replied", new RequestListener() {
					
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
					public void onComplete(final String response, Object state) {
						Log.i("not_replied response", response);
						
						runOnUiThread(new Runnable() {

							@SuppressWarnings("unchecked")
							@Override
							public void run() {
								setContentView(R.layout.getevents);
								// mAsyncRunner = Utility.getAsyncRunner();
								listView = (ListView) findViewById(R.id.geteventsview);

								listView.setOnItemClickListener(new OnItemClickListener() {

									@Override
									public void onItemClick(AdapterView<?> parent,
											View view, int position, long id) {
										Utility.setEvent(events.get(position));
										

										Intent i = new Intent(GetEvents.this, EventInfoActivity.class);
										startActivity(i);
										finish();
										
									}
								});

								try {
									JSONObject json = new JSONObject(response);

									JSONArray jsonAr = json.getJSONArray("data");
									int len = (jsonAr != null ? jsonAr.length() : 0);
									Log.d("Facebook not_replied Events Request", "d.length(): "
											+ len);

									for (int i = 0; i < len; i++) {
										JSONObject jsonObj = jsonAr.getJSONObject(i);
										if (!jsonObj.getString("name")
														.trim()
														.startsWith("G2G - ")
												|| !jsonObj.has("location")) {
											Log.i("JSON not_replied array", "item incompatible");
											continue;
										}
										String description = "";
										if (jsonObj.has("description"))
											description = jsonObj.getString("description");
										Event e = new Event(jsonObj.getString("id"),
												jsonObj.getString("location"), jsonObj
														.getString("name"), jsonObj
														.getString("start_time"),
												description);
										if (e.isOld())
											continue;
										events.add(e);
										Log.i("not_replied event added", e.toString());
									}
									
									Collections.sort(events);

									Log.i("Events length",
											Integer.toString(events.size()));

									eventsArrayAdapter = new EventsArrayAdapter(
											GetEvents.this, R.layout.eventrowlayout,
											events);
									listView.setAdapter(eventsArrayAdapter);
									eventsArrayAdapter.notifyDataSetChanged();
									// listView.setOnItemClickListener
								} catch (JSONException e) {
									Log.w("GetEvents",
											"JSON Error in response. "
													+ e.getLocalizedMessage());
								}

							}
						});
					}
				});

			}
		});
	}

	private class EventsArrayAdapter extends ArrayAdapter<Event> {
		private final Activity context;
		private final ArrayList<Event> events;
		private int resourceId;

		/**
		 * Constructor
		 * 
		 * @param context
		 *            the application content
		 * @param resourceId
		 *            the ID of the resource/view
		 * @param events
		 *            the bound ArrayList
		 */
		public EventsArrayAdapter(Activity context, int resourceId,
				ArrayList<Event> events) {
			super(context, resourceId, events);
			this.context = context;
			this.events = events;
			this.resourceId = resourceId;
		}

		/**
		 * Updates the view
		 * 
		 * @param position
		 *            the ArrayList position to update
		 * @param convertView
		 *            the view to update/inflate if needed
		 * @param parent
		 *            the groups parent view
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			Typeface green_pillow = Typeface.createFromAsset(getAssets(), "GREENPIL.otf");
			((TextView) findViewById(R.id.get)).setTypeface(green_pillow);
			((TextView) findViewById(R.id.together)).setTypeface(green_pillow);
			
			Event e = events.get(position);
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = vi.inflate(resourceId, null);
			}
			TextView dateTxt = (TextView) rowView.findViewById(R.id.daterow);
			dateTxt.setText(e.startTime.toLocaleString());
			dateTxt.setTextColor(R.color.black);
			TextView nameTxt = (TextView) rowView.findViewById(R.id.namerow);
			nameTxt.setText(e.name);
			nameTxt.setTextColor(R.color.black);
			nameTxt.setTextSize(20);
			return rowView;
		}

	}

	
}
