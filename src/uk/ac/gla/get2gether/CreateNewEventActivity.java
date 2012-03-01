package uk.ac.gla.get2gether;


//import android.app.Activity;
//import android.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

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
import android.text.AlteredCharSequence;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.*;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;


import com.googlecode.android.widgets.DateSlider.DateSlider;
import com.googlecode.android.widgets.DateSlider.DefaultDateSlider;
import com.googlecode.android.widgets.DateSlider.TimeSlider;

//import android.app.Dialog;
import android.graphics.Typeface;
//import android.os.Bundle;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.widget.Button;
//import android.widget.TextView;

public class CreateNewEventActivity extends G2G_Activity{
	
	private TextView dateText;
	private TextView timeText;
	
	public Typeface barlow;
	public Calendar cal;
	
	static final int DEFAULTDATESELECTOR_ID = 0;
	static final int TIMESELECTOR_ID = 1;
	private final int ADDRESS_DIALOG = 33;
	
	private final int YEAR = 1;
	private final int MONTH = 2;
	private final int DAY_OF_MONTH = 5;
	private final int HOUR = 11;
	private final int MINUTE = 12;
	
	private String eventID;
	private AsyncFacebookRunner mAsyncRunner;
	private Intent intentForInviteFriends;
	private PopupWindow popup;
	private EditText locationName;
	private Location selectedLocation;
	private ArrayList<Location> locationList;
	private TextView address;
	private View v;
	
	private boolean addressSelected;
	
	private boolean dateSelected;
	private boolean timeSelected;
	private EditText details;
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Utility.INVITE_FRIENDS_CODE) {
			Log.i("CreateEvent Activity", "InviteFriends Activity returned");
			Log.i("CreateEvent Activity", "Finished");
			finish();
		}
	}
	
	private void setupTypeface(Typeface tf){
		((TextView)findViewById(R.id.arrange)).setTypeface(tf);
		((TextView)findViewById(R.id.meetup)).setTypeface(tf);
		((TextView)findViewById(R.id.enter_name)).setTypeface(tf);
		((TextView)findViewById(R.id.enter_location)).setTypeface(tf);
		((TextView)findViewById(R.id.enter_time)).setTypeface(tf);
		((TextView)findViewById(R.id.enter_date)).setTypeface(tf);
		((TextView)findViewById(R.id.enter_description)).setTypeface(tf);
		((TextView)findViewById(R.id.get)).setTypeface(tf);
		((TextView)findViewById(R.id.together)).setTypeface(tf);
	}
	
	private void setupTextViewColor(TextView tv, int color){
		tv.setTextColor(color);
	}
	
	private void changeBackground(int resource){
		v.setBackgroundResource(R.drawable.background);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceBundle){
		super.onCreate(savedInstanceBundle);
		setContentView(R.layout.g2g_create_new);
		
		dateSelected = false;
		timeSelected = false;
		
		
		Typeface green_pillow = Typeface.createFromAsset(getAssets(), "GREENPIL.otf");
		Typeface barlow = Typeface.createFromAsset(getAssets(), "Barlow_solid.ttf");
		
		setupTypeface(green_pillow);
				
		mAsyncRunner = Utility.getAsyncRunner();
		intentForInviteFriends = new Intent();
		
		//Changeable textBoxes
		dateText = (TextView) findViewById(R.id.enter_date);
		timeText = (TextView) findViewById(R.id.enter_time);
		address = (TextView) findViewById(R.id.address);
		
		//Background image
		//View v = findViewById(R.id.create_event_layout);
		
		//EditBoxes
		final EditText name = (EditText) findViewById(R.id.meetup_name);
		locationName = (EditText) findViewById(R.id.meetup_location);
		
		//Calendar
		cal = Calendar.getInstance();
		
		SharedPreferences settings = getSharedPreferences("get2gether", 0);
		final boolean onEditMode = settings.getBoolean("onEditMode", false);
		
		details = (EditText) findViewById(R.id.event_description);
		
		
		//Buttons
		Button searchLocation = (Button) findViewById(R.id.search_location_button);
		searchLocation.setTypeface(green_pillow);
		//searchLocation.setText(Html.fromHtml("Search <br/> location"));
		//searchLocation.getBackground().setColorFilter(Color.rgb(206, 255, 191), PorterDuff.Mode.MULTIPLY);
		
		Button arrangeButton = (Button) findViewById(R.id.arrange_button);
		arrangeButton.setTypeface(green_pillow);
		arrangeButton.setText(Html.fromHtml("Arrange a <br/> <medium>new meetup</medium>"));
		//arrangeButton.getBackground().setColorFilter(Color.rgb(206, 255, 191), PorterDuff.Mode.MULTIPLY);
		
		Button dateButton =(Button) findViewById(R.id.date_button);
		dateButton.setTypeface(green_pillow);
		//dateButton.getBackground().setColorFilter(Color.rgb(206, 255, 191), PorterDuff.Mode.MULTIPLY);
		
		Button timeButton = (Button) findViewById(R.id.time_button);
		timeButton.setTypeface(green_pillow);
		//timeButton.getBackground().setColorFilter(Color.rgb(206, 255, 191), PorterDuff.Mode.MULTIPLY);
		
		//Behavior for EditMode
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
			dateText.setText(String.format("%n%te %tB %tY", e.startTime.getYear() + 1900,
					e.startTime.getMonth(), e.startTime.getDate()));
			dateSelected = true;
			timeText.setText(String.format("%n%tR", e.startTime.getHours(), e.startTime.getMinutes()));
			timeSelected = true;
			selectedLocation = new Location(e.address, e.latitude, e.longitude);
			} 
		
		
		/*
		name.setOnFocusChangeListener(new OnFocusChangeListener() {
			boolean flag = true;
			View v = findViewById(R.id.create_event_layout);
			public void onFocusChange(View arg0, boolean arg1) {
				if(flag){
					v.setBackgroundResource(R.drawable.background);
					flag = false;
				}
				else{
					v.setBackgroundResource(R.drawable.background_alternative);
					flag = true;
				}
				v.getBackground().invalidateSelf();
			}
		});
		*/	
		//Button listeners
		searchLocation.setOnClickListener(new SearchButtonListener());
		
		dateButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// call the internal showDialog method using the predefined ID
				showDialog(DEFAULTDATESELECTOR_ID);
				}        	
        	});
		
		timeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// call the internal showDialog method using the predefined ID
				showDialog(TIMESELECTOR_ID);
			}        	
        });
		
		arrangeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (selectedLocation == null || timeSelected == false ||
						dateSelected == false || 
						name.getEditableText().toString().equals("") ||
						details.getEditableText().toString().equals("")) {
					Toast.makeText(CreateNewEventActivity.this,
							"Please fill in all fields before proceeding",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Bundle eventParams = new Bundle();
				eventParams.putString("name", "G2G - "
						+ name.getEditableText().toString());
				eventParams.putString(
						"start_time",
						getDateTimeString(cal.get(YEAR), //Calendar.getYear();
								cal.get(MONTH) + 1, //Calendar.getMonth();
								cal.get(DAY_OF_MONTH), //getDayOfTheMonth();
								cal.get(HOUR), //
								cal.get(MINUTE)));
				eventParams.putString("location", selectedLocation.getAddress()
						+ " @ " + selectedLocation.getLatitude() + ", "
						+ selectedLocation.getLongitude());
				eventParams.putString("description",
						getResources().getString(R.string.event_desc) + " - "
								+ locationName.getEditableText().toString() + "\n" +
								details.getEditableText().toString());
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
	
	private static void prepareTextView(TextView tv, Typeface tf){
		tv.setTypeface(tf);
		tv.setTextSize(18);
		tv.setPadding(7, 10, 0, 20);
	}
	
	// define the listener which is called once a user selected the date.
	private DateSlider.OnDateSetListener mDateSetListener =
		        new DateSlider.OnDateSetListener() {
		            public void onDateSet(DateSlider view, Calendar selectedDate) {
		            	// update the dateText view with the corresponding date
		            	prepareTextView(dateText, barlow);
		                dateText.setText(String.format("%n%te %tB %tY", selectedDate, selectedDate, selectedDate));
		                if(cal == null)
		                	cal = Calendar.getInstance();
		                //
		                cal.set(selectedDate.get(YEAR), selectedDate.get(MONTH), selectedDate.get(DAY_OF_MONTH));
		                Log.i("Year2", cal.get(1) + "");
						Log.i("Month2", cal.get(2) + 1 + "");
						Log.i("Day2", cal.get(5) + "");
						Log.i("Hour2", cal.get(10) + "");
						Log.i("Minute2", cal.get(12) + "");
		                
						dateSelected = true;
		            }
		    };
		
		    private DateSlider.OnDateSetListener mTimeSetListener =
		            new DateSlider.OnDateSetListener() {
		                public void onDateSet(DateSlider view, Calendar selectedDate) {
		                	// update the dateText view with the corresponding date
		                	prepareTextView(timeText, barlow);
		                	timeText.setText(String.format("%n%tR", selectedDate));
		                	if (cal == null)
		                		cal = Calendar.getInstance();
		                    cal.set(cal.get(YEAR), cal.get(MONTH), cal.get(DAY_OF_MONTH), selectedDate.get(HOUR), selectedDate.get(MINUTE));
		                    timeSelected = true;
		                }
		        };
	
	    @Override
	    protected Dialog onCreateDialog(int id) {
	    	// this method is called after invoking 'showDialog' for the first time
	    	// here we initiate the corresponding DateSlideSelector and return the dialog to its caller

	    	// get todays date and the time
	        final Calendar c = Calendar.getInstance();
	        switch (id) {
	        case DEFAULTDATESELECTOR_ID:
	            return new DefaultDateSlider(this,mDateSetListener,c);
	        case TIMESELECTOR_ID:
	            return new TimeSlider(this,mTimeSetListener,c);
	        case ADDRESS_DIALOG:{
	        	AlertDialog.Builder builder = new AlertDialog.Builder(
						CreateNewEventActivity.this);
				builder.setTitle("Select Address");
				locationList = new ArrayList<Location>(OTP.geocode(locationName
						.getEditableText().toString()));
				Log.i("Search button clicked", locationList.toString());
				LocationArrayAdapter locationArrayAdapter = new LocationArrayAdapter(
						CreateNewEventActivity.this, R.layout.addressrowlayout, locationList);
				builder.setSingleChoiceItems(locationArrayAdapter, -1,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								Log.i("Selected address", Integer.toString(which));
								selectedLocation = locationList.get(which);
								address.setText(selectedLocation.getAddress()
										.toString());
								addressSelected = true;
								dismissDialog(ADDRESS_DIALOG);
								removeDialog(ADDRESS_DIALOG);
							}
						});
				return builder.create();
	        	}
	        }
	        return null;
	    }
	    
	    private class SearchButtonListener implements OnClickListener {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(locationName.getWindowToken(), 0);
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
				intentForInviteFriends.setClass(CreateNewEventActivity.this,
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


