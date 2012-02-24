package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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

		// setContentView(R.layout.getevents);
		mAsyncRunner = Utility.getAsyncRunner();
		// events = new ArrayList<Event>();
		// listView = (ListView) findViewById(R.id.geteventsview);

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
				Log.i("GetEvents response", response);

				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						setContentView(R.layout.getevents);
						// mAsyncRunner = Utility.getAsyncRunner();
						events = new ArrayList<Event>();
						listView = (ListView) findViewById(R.id.geteventsview);

						listView.setOnItemClickListener(new OnItemClickListener() {

							@Override
							public void onItemClick(AdapterView<?> parent,
									View view, int position, long id) {

								SharedPreferences settings = getSharedPreferences(
										"get2gether", 0);
								SharedPreferences.Editor editor = settings
										.edit();
								editor.putString("eventID",
										events.get(position).id);
								editor.commit();

								Log.i("GetEvents",
										"Event clicked: "
												+ events.get(position).id
												+ ", "
												+ events.get(position).name);

								finish();
							}
						});

						try {
							JSONObject json = new JSONObject(response);

							JSONArray jsonAr = json.getJSONArray("data");
							int len = (jsonAr != null ? jsonAr.length() : 0);
							Log.d("Facebook Events Request", "d.length(): "
									+ len);

							for (int i = 0; i < len; i++) {
								JSONObject jsonObj = jsonAr.getJSONObject(i);
								if (!jsonObj.has("description")
										|| !jsonObj
												.getString("description")
												.trim()
												.startsWith(
														getResources()
																.getString(
																		R.string.event_desc)
																.trim())
										|| !jsonObj.has("location"))
									continue;
								Event e = new Event(jsonObj.getString("id"),
										jsonObj.getString("location"), jsonObj
												.getString("name"), jsonObj
												.getString("start_time"),
										jsonObj.getString("description"));
								// e.description =
								// jsonObj.getString("description");
								// e.id = jsonObj.getString("id");
								// e.locationName =
								// jsonObj.getString("location");
								// e.name = jsonObj.getString("name");
								// e.start_time =
								// jsonObj.getString("start_time");
								events.add(e);
								Log.i("Event added", e.toString());
							}

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
			Event e = events.get(position);
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = vi.inflate(resourceId, null);
			}
			TextView dateTxt = (TextView) rowView.findViewById(R.id.daterow);
			dateTxt.setText(e.startTime.toLocaleString());
			TextView nameTxt = (TextView) rowView.findViewById(R.id.namerow);
			nameTxt.setText(e.name);
			return rowView;
		}

	}

	private class Event {

		public String id;
		public String locationName;
		public String name;
		// public String start_time;
		public Date startTime;

		public String ownerID;
		public String description;
		public double latitude;
		public double longitude;

		// Map of invited people <id, name> who haven't responded
		// yet to their invitation to this event
		public HashMap<String, String> invitedMap;
		public HashMap<String, String> confirmedMap; // likewise for confirmed

		public Event(String id, String locationName, String name,
				String start_time, String description) {
			this.id = id;

			int atCursor = locationName.lastIndexOf('@');
			this.locationName = locationName.substring(0, atCursor - 1);

			int commaCursor = locationName.lastIndexOf(',');
			this.latitude = Double.valueOf(locationName.substring(atCursor + 2,
					commaCursor));
			this.longitude = Double.valueOf(locationName.substring(
					commaCursor + 2, locationName.length()));

			// this.start_time = start_time;
			this.name = name.substring(6);
			this.description = description;

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			try {
				this.startTime = sdf.parse(start_time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		public String toString() {
			return id + ", " + locationName + ", " + name + ", " + description
					+ ", " + latitude + ", " + longitude + ", "
					+ startTime.toGMTString();
		}

	}

}
