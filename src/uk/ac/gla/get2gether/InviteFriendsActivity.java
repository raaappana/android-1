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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class InviteFriendsActivity extends Activity {

	private FriendsArrayAdapter friendsArrayAdapter;
	private ListView listView;
	private ArrayList<Friend> friends;
	private AsyncFacebookRunner mAsyncRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("InviteFriends Activity", "Started");

		setContentView(R.layout.invite_friends);
		mAsyncRunner = Utility.getAsyncRunner();
		String response = getIntent().getStringExtra("friendsresponse");
		friends = new ArrayList<Friend>();
		listView = (ListView) findViewById(R.id.addfriendsview);
		Log.i("InviteFriendsActivity response", response);

		try {
			// process the response here: executed in background thread
			Log.d("Facebook-Example-Friends Request", "response.length(): "
					+ response.length());
			Log.d("Facebook-Example-Friends Request", "Response: " + response);

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

			friendsArrayAdapter = new FriendsArrayAdapter(
					InviteFriendsActivity.this, R.layout.rowlayout, friends);
			listView.setAdapter(friendsArrayAdapter);
			friendsArrayAdapter.notifyDataSetChanged();
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View v,
						int position, long arg3) {
//					Intent i = new Intent();
//					i.putExtra("friendid", friends.get(position).id);
//					setResult(Activity.RESULT_OK, i);
					
					String eventID = getIntent().getStringExtra("eventid");
					String friendID = friends.get(position).id;
					Bundle params = new Bundle();
					mAsyncRunner.request("/" + eventID + "/invited/" + friendID, params, "POST", new RequestListener(){

						@Override
						public void onComplete(String arg0, Object arg1) {
							Log.i("Inviting response", arg0);
							Log.i("InviteFriends Activity", "Finished");
							finish();
						}

						@Override
						public void onFacebookError(FacebookError arg0, Object arg1) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onFileNotFoundException(FileNotFoundException arg0,
								Object arg1) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onIOException(IOException arg0, Object arg1) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onMalformedURLException(MalformedURLException arg0,
								Object arg1) {
							// TODO Auto-generated method stub
							
						}
						
					}, new Object());
				}

			});
		} catch (JSONException e) {
			Log.w("get2gether FB", "JSON Error in response");
		}
	}



}
