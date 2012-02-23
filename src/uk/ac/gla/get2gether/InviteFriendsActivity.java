package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

import android.app.ListActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class InviteFriendsActivity extends ListActivity {

//	private FriendsArrayAdapter friendsArrayAdapter;
	private ListView listView;
	private ArrayList<Friend> friends;
	private Button selectFriendsButton;
	private AsyncFacebookRunner mAsyncRunner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("InviteFriends Activity", "Started");
		setContentView(R.layout.invite_friends);
		
		
		String response = getIntent().getStringExtra("friendsresponse");
		friends = new ArrayList<Friend>();
		mAsyncRunner = Utility.getAsyncRunner();
//		listView = (ListView) findViewById(R.id.addfriendsview);
		listView = getListView();
		listView.setCacheColorHint(0);
		Log.i("InviteFriendsActivity response", response);
		
		selectFriendsButton = (Button) findViewById(R.id.selectfriendsbutton);

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
		} catch (JSONException e) {
			Log.w("get2gether FB", "JSON Error in response");
		}

//			friendsArrayAdapter = new FriendsArrayAdapter(
//					InviteFriendsActivity.this, R.layout.invite_friends_rowlayout, friends);
		
		String friendNames[] = new String[friends.size()];
		for (int i = 0; i < friends.size(); i++) {
			friendNames[i] = friends.get(i).name;
		}

			listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, friendNames));
			listView.setItemsCanFocus(false);
			listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
//			friendsArrayAdapter.notifyDataSetChanged();
			
			selectFriendsButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {					
					String eventID = getIntent().getStringExtra("eventid");
					
//					SparseBooleanArray hits = listView.getCheckedItemPositions();
					String friendIDsString = "";
					int count = listView.getAdapter().getCount();
					for (int i = 0; i < count; i++) {
						if (listView.isItemChecked(i)) {
							if (friendIDsString.equals(""))
								friendIDsString += friends.get(i).id;
							else
								friendIDsString += "," + friends.get(i).id;
						}
					}
					Log.i("InviteFriendsActivity", "friendsIDsString: " + friendIDsString);
//					Log.i("Friends count", "" + count);
					Bundle params = new Bundle();
					Log.i("InviteFriendsActivity", "request: " + "/" + eventID + "/invited?users=" + friendIDsString);
					mAsyncRunner.request("/" + eventID + "/invited?users=" + friendIDsString, params, "POST", new RequestListener(){

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
	}
			


}
