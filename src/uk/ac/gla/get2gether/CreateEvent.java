package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class CreateEvent extends Activity {

	private String eventID;
	private AsyncFacebookRunner mAsyncRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("CreateEvent Activity", "Started");
		setContentView(R.layout.create_event);
		
		mAsyncRunner = Utility.getAsyncRunner();
		
		final EditText name = (EditText) findViewById(R.id.meetup_name);
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
									
									Intent i = new Intent();
									i.putExtra("eventid", eventID);
									setResult(Activity.RESULT_OK, i);
									Log.i("CreateEvent Activity", "Finished");
									finish();
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

}
