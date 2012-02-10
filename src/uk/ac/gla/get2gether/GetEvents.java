package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class GetEvents extends Activity {

	private EventsArrayAdapter eventsArrayAdapter;
	private ListView listView;
	private ArrayList<Event> events;
	private AsyncFacebookRunner mAsyncRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("GetEvents Activity", "Started");

//		setContentView(R.layout.getevents);
		mAsyncRunner = Utility.getAsyncRunner();
//		events = new ArrayList<Event>();
//		listView = (ListView) findViewById(R.id.geteventsview);

		mAsyncRunner.request("me/events", new RequestListener() {

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
//						mAsyncRunner = Utility.getAsyncRunner();
						events = new ArrayList<Event>();
						listView = (ListView) findViewById(R.id.geteventsview);
						
						try {
							JSONObject json = new JSONObject(response);

							JSONArray jsonAr = json.getJSONArray("data");
							int len = (jsonAr != null ? jsonAr.length() : 0);
							Log.d("Facebook Events Request", "d.length(): " + len);

							for (int i = 0; i < len; i++) {
								Event e = new Event();
								JSONObject jsonObj = jsonAr.getJSONObject(i);
								e.id = jsonObj.getString("id");
								// It's not sure that an event has a location name
								if (jsonObj.has("location")) 
									e.locationName = jsonObj.getString("location");
								e.name = jsonObj.getString("name");
								e.start_time = jsonObj.getString("start_time");
								events.add(e);
							}
							
							Log.i("Events length", Integer.toString(events.size()));

							eventsArrayAdapter = new EventsArrayAdapter(GetEvents.this,
									R.layout.eventrowlayout, events);
							listView.setAdapter(eventsArrayAdapter);
							eventsArrayAdapter.notifyDataSetChanged();
							// listView.setOnItemClickListener
						} catch (JSONException e) {
							Log.w("GetEvents", "JSON Error in response. " + e.getLocalizedMessage());
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
			View rowView = convertView;
			if (rowView == null) {
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = vi.inflate(resourceId, null);
			}
			Event e = events.get(position);
			TextView dateTxt = (TextView) rowView.findViewById(R.id.daterow);
			dateTxt.setText(e.start_time);
			TextView nameTxt = (TextView) rowView.findViewById(R.id.namerow);
			nameTxt.setText(e.name);
			return rowView;
		}

	}

	private class Event {

		public String id;
		public String locationName;
		public String name;
		public String start_time;

		public String ownerID;
		public String description;
		public String locationCoordinates;

		// Map of invited people <id, name> who haven't responded
		// yet to their invitation to this event
		public HashMap<String, String> invitedMap;
		public HashMap<String, String> confirmedMap; // likewise for confirmed

	}

}
