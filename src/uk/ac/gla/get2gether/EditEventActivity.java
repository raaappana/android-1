package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.idansof.otp.client.Location;
import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.gla.get2gether.map.OTP;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class EditEventActivity extends Activity {

	private String eventID;
	private AsyncFacebookRunner mAsyncRunner;
	private Intent intentForInviteFriends;
	private EditText locationName;
	private Location selectedLocation;
	private ArrayList<Location> locationList;
	private TextView address;
	private final int ADDRESS_DIALOG = 33;

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

		SharedPreferences settings = getSharedPreferences("get2gether", 0);
		final boolean onEditMode = settings.getBoolean("onEditMode", false);

		mAsyncRunner = Utility.getAsyncRunner();
		intentForInviteFriends = new Intent();

		final EditText name = (EditText) findViewById(R.id.meetup_name);

		locationName = (EditText) findViewById(R.id.meetup_location);
		Button searchLocationButton = (Button) findViewById(R.id.search_location_button);
		searchLocationButton.setOnClickListener(new SearchButtonListener());
		address = (TextView) findViewById(R.id.address_old);

		final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
		final TimePicker timePicker = (TimePicker) findViewById(R.id.timePicker);
		Button arrange = (Button) findViewById(R.id.arrange_button);

		if (onEditMode) {
			Event e = Utility.getEvent();
			if (e == null) {
				Log.e("EditEvent", "Utility.getEventToEdit() was null");
				finish();
			}
			name.setText(e.name);
			locationName.setText(e.locationName);
			address.setText(e.address);
			Log.i("Date/TimePicker values",
					"" + e.startTime.getYear() + ", " + e.startTime.getMonth()
							+ ", " + e.startTime.getDate() + ", "
							+ e.startTime.getHours() + ", "
							+ e.startTime.getMinutes());
			datePicker.init(e.startTime.getYear() + 1900, e.startTime.getMonth(),
					e.startTime.getDate(), null);
			timePicker.setCurrentHour(e.startTime.getHours());
			timePicker.setCurrentMinute(e.startTime.getMinutes());
			selectedLocation = new Location(e.address, e.latitude, e.longitude);
		} 

		arrange.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedLocation == null) {
					Toast.makeText(EditEventActivity.this,
							"You haven't selected any address, sorry..",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Bundle eventParams = new Bundle();
				eventParams.putString("name", "G2G - "
						+ name.getEditableText().toString());
				eventParams.putString(
						"start_time",
						getDateTimeString(datePicker.getYear(),
								datePicker.getMonth() + 1,
								datePicker.getDayOfMonth(),
								timePicker.getCurrentHour(),
								timePicker.getCurrentMinute()));
				eventParams.putString("location", selectedLocation.getAddress()
						+ " @ " + selectedLocation.getLatitude() + ", "
						+ selectedLocation.getLongitude());
				eventParams.putString("description",
						getResources().getString(R.string.event_desc) + " - "
								+ locationName.getEditableText().toString());

				// 1st way
				Bundle locationParam = new Bundle();
				locationParam.putString("latitude",
						Double.toString(selectedLocation.getLatitude()));
				locationParam.putString("longitude ",
						Double.toString(selectedLocation.getLongitude()));
				eventParams.putBundle("venue", locationParam);

				// 2nd way
				eventParams.putString("latitude",
						Double.toString(selectedLocation.getLatitude()));
				eventParams.putString("longitude ",
						Double.toString(selectedLocation.getLongitude()));

				Log.i("Event longitude-latitude",
						Double.toString(selectedLocation.getLatitude())
								+ ", "
								+ Double.toString(selectedLocation
										.getLongitude()));
				String graphString;
				if (onEditMode)
					graphString = "/" + Utility.getEvent().id;
				else
					graphString = "me/events";
				mAsyncRunner.request(graphString, eventParams, "POST",
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
									if (onEditMode)
										intentForInviteFriends.putExtra("eventid",
												Utility.getEvent().id);
									else {
										Log.i("Create Event", response);
										JSONObject jo = new JSONObject(response);
										eventID = jo.getString("id");
										Log.i("EVENT ID", eventID);

									// putting the eventid in the intent to pass
									// to inviteFriends
										intentForInviteFriends.putExtra("eventid",
											eventID);	
									}
									
									mAsyncRunner.request("me/friends",
											new FriendsRequestListener());
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

	protected Dialog onCreateDialog(int id) {
		if (id == ADDRESS_DIALOG) {
			AlertDialog.Builder builder = new AlertDialog.Builder(
					EditEventActivity.this);
			builder.setTitle("Select Address");
			locationList = new ArrayList<Location>(OTP.geocode(locationName
					.getEditableText().toString()));
			Log.i("Search button clicked", locationList.toString());
			LocationArrayAdapter locationArrayAdapter = new LocationArrayAdapter(
					EditEventActivity.this, R.layout.addressrowlayout, locationList);
			builder.setSingleChoiceItems(locationArrayAdapter, -1,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.i("Selected address", Integer.toString(which));
							selectedLocation = locationList.get(which);
							address.setText(selectedLocation.getAddress()
									.toString());
							dismissDialog(ADDRESS_DIALOG);
							removeDialog(ADDRESS_DIALOG);
						}
					});
			return builder.create();
		}
		return null;
	}

	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == ADDRESS_DIALOG) {
			// dialog.
		}
	}

	private class SearchButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(locationName.getWindowToken(), 0);

			// remove the previous dialog instance in case it exists
			removeDialog(ADDRESS_DIALOG);
			showDialog(ADDRESS_DIALOG);
		}

	}

	private class LocationArrayAdapter extends ArrayAdapter<Location> {
		private Activity context;
		private ArrayList<Location> locationList;
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
				LayoutInflater vi = (LayoutInflater) context
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			// putting the friends JSON object in the intent to pass to
			// inviteFriends
			intentForInviteFriends.putExtra("friendsresponse", response);
			intentForInviteFriends.setClass(EditEventActivity.this,
					InviteFriendsActivity.class);
			startActivityForResult(intentForInviteFriends,
					Utility.INVITE_FRIENDS_CODE);

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
