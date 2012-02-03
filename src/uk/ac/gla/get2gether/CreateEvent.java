package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.idansof.otp.client.Location;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.gla.get2gether.map.OTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class CreateEvent extends Activity {

	private String eventID;
	private AsyncFacebookRunner mAsyncRunner;
	private Intent intentForInviteFriends;
	private PopupWindow popup;
	private EditText locationName;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Utility.INVITE_FRIENDS_CODE) {
			Log.i("CreateEvent Activity", "InviteFriends Activity returned");
			Log.i("CreateEvent Activity", "Finished");
			finish();
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("CreateEvent Activity", "Started");
		setContentView(R.layout.create_event);

		mAsyncRunner = Utility.getAsyncRunner();
		intentForInviteFriends = new Intent();

		final EditText name = (EditText) findViewById(R.id.meetup_name);
		
		locationName = (EditText) findViewById(R.id.meetup_location);
		Button searchLocationButton = (Button) findViewById(R.id.search_location_button);
		searchLocationButton.setOnClickListener(new SearchButtonListener());
		
		final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
		Button arrange = (Button) findViewById(R.id.arrange_button);
		arrange.setOnClickListener(new OnClickListener() {
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

//									Intent i = new Intent();
									intentForInviteFriends.putExtra("eventid", eventID);
//									setResult(Activity.RESULT_OK, i);

									mAsyncRunner.request("me/friends",
											new FriendsRequestListener());
//									Log.i("CreateEvent Activity", "Finished");
//									finish();
								} catch (JSONException e) {
									e.printStackTrace();
								}
							}
						}, new Object());
			}
		});

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
	
	private class SearchButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			LayoutInflater inflater = (LayoutInflater) CreateEvent.this.getSystemService(LAYOUT_INFLATER_SERVICE);
			ArrayList<Location> locationList = new ArrayList<Location>(OTP.geocode(locationName.getEditableText().toString()));
			Log.i("Search button clicked", locationList.toString());
			LocationArrayAdapter locationArrayAdapter = new LocationArrayAdapter(CreateEvent.this, R.layout.rowlayout, locationList);
			View popupView = inflater.inflate(R.layout.location_search, null, false);
			ListView locationsView = (ListView) popupView.findViewById(R.id.location_list);
			locationsView.setAdapter(locationArrayAdapter);
			locationArrayAdapter.notifyDataSetChanged();
			popup = new PopupWindow(popupView, 100, 100, true);
			popup.setFocusable(true);
			popup.showAtLocation(CreateEvent.this.findViewById(R.id.create_event_layout), Gravity.CENTER, 0, 0);
		}
		
	}
	
	private class LocationArrayAdapter extends ArrayAdapter<Location> {
		private final Activity context;
	    private final ArrayList<Location> locationList;
	    private int resourceId;	
	    
		public LocationArrayAdapter(Activity context, int textViewResourceId,
				ArrayList<Location> objects) {
			super(context, textViewResourceId, objects);
			this.context = context;
			this.locationList = objects;
			this.resourceId = textViewResourceId;
		}
	    
		@Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View rowView = convertView;
	        if (rowView == null) {
	            LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	            rowView = vi.inflate(resourceId, null);
	        }
	        Location l = locationList.get(position);
	        TextView rowTxt = (TextView) rowView.findViewById(R.id.rowtext_top);
	        rowTxt.setText(l.getAddress());
	        return rowView;
	    }
	    
	}

	private class FriendsRequestListener implements
			com.facebook.android.AsyncFacebookRunner.RequestListener {

		/**
		 * Called when the request to get friends has been completed. Retrieve
		 * and parse and display the JSON stream.
		 */
		@Override
		public void onComplete(final String response, Object state) {

//			Intent intent = new Intent();
			intentForInviteFriends.putExtra("friendsresponse", response);
			intentForInviteFriends.setClass(CreateEvent.this, InviteFriendsActivity.class);
			startActivityForResult(intentForInviteFriends, Utility.INVITE_FRIENDS_CODE);
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

}
