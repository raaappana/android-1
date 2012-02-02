package uk.ac.gla.get2gether;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("InviteFriends Activity", "Started");
		
		setContentView(R.layout.invite_friends);
		String response = getIntent().getStringExtra("response");
		friends = new ArrayList<Friend>();
		listView = (ListView) findViewById(R.id.addfriendsview);
		Log.i("InviteFriendsActivity response", response);
		
		try {
			// process the response here: executed in background thread
			Log.d("Facebook-Example-Friends Request", "response.length(): "
					+ response.length());
			Log.d("Facebook-Example-Friends Request", "Response: "
					+ response);

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
			public void onItemClick(AdapterView<?> arg0,
					View v, int position, long arg3) {
				Intent i = new Intent();
				i.putExtra("friendid", friends.get(position).id);
				setResult(Activity.RESULT_OK, i);
				Log.i("InviteFriends Activity", "Finished");
				finish();
			}

		});}catch (JSONException e) {
			Log.w("get2gether FB", "JSON Error in response");
		}
	}

}
