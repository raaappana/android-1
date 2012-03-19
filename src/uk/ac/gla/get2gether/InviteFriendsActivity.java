package uk.ac.gla.get2gether;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.FacebookError;

public class InviteFriendsActivity extends ListActivity {

	// private FriendsArrayAdapter friendsArrayAdapter;
	private ListView listView;
	private ArrayList<Friend> friends;
	private Button selectFriendsButton;
	private AsyncFacebookRunner mAsyncRunner;
	private String friendIDsString;
	private Bundle addParams;
	private List<Friend> friendsToAdd;
	private List<Friend> friendsToDelete;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("InviteFriends Activity", "Started");
		setContentView(R.layout.invite_friends);


		String response = getIntent().getStringExtra("friendsresponse");
		friends = new ArrayList<Friend>();
		mAsyncRunner = Utility.getAsyncRunner();
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

		String friendNames[] = new String[friends.size()];
		for (int i = 0; i < friends.size(); i++) {
			friendNames[i] = friends.get(i).name;
		}

		listView.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, friendNames));
		listView.setItemsCanFocus(false);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		// friendsArrayAdapter.notifyDataSetChanged();

		selectFriendsButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				friendsToAdd = new ArrayList<Friend>();
				friendsToDelete = new ArrayList<Friend>();
				List<Friend> friendsChecked = new ArrayList<Friend>();

				int count = listView.getAdapter().getCount();
				for (int i = 0; i < count; i++) {
					if (listView.isItemChecked(i)) {
						friendsChecked.add(friends.get(i));
					}
				}

				final SharedPreferences settings = getSharedPreferences(
						"get2gether", 0);
				boolean onEditMode = settings.getBoolean("onEditMode", false);
				String myID = settings.getString("facebookID", null);

				if (onEditMode) {
					for (int i = 0; i < friendsChecked.size(); i++) {
						if (!Utility.getEvent().invitedList
								.contains(friendsChecked.get(i)))
							friendsToAdd.add(friendsChecked.get(i));
					}

					for (int i = 0; i < Utility.getEvent().invitedList.size(); i++) {
						if (Utility.getEvent().invitedList.get(i).id
								.equals(myID))
							continue;
						if (!friendsChecked.contains(Utility.getEvent().invitedList
								.get(i)))
							friendsToDelete.add(Utility.getEvent().invitedList
									.get(i));
					}
				} else
					friendsToAdd = friendsChecked;
				Log.i("Add/delete friends", "" + friendsToDelete.size()
						+ " friends to be deleted, " + friendsToAdd.size()
						+ " friends to be added");
				final String eventID = getIntent().getStringExtra("eventid");

				if (friendsToDelete.size() > 0) {
					Toast.makeText(InviteFriendsActivity.this,
							"Deleting Friends", Toast.LENGTH_SHORT).show();

					for (int i = 0; i < friendsToDelete.size(); i++) {
						Bundle deleteParams = new Bundle();
						Log.i("Deleting friends", friendsToDelete.get(i).name);

						mAsyncRunner.request("/" + eventID + "/invited/"
								+ friendsToDelete.get(i).id, deleteParams,
								"DELETE", new RequestListener() {

									@Override
									public void onMalformedURLException(
											MalformedURLException e,
											Object state) {
									}

									@Override
									public void onIOException(IOException e,
											Object state) {
									}

									@Override
									public void onFileNotFoundException(
											FileNotFoundException e,
											Object state) {
									}

									@Override
									public void onFacebookError(
											FacebookError e, Object state) {
									}

									@Override
									public void onComplete(String response,
											Object state) {
										Log.i("Friend deletion response",
												response);
									}
								}, null);
					}

				}
				if (friendsToAdd.size() > 0) {
					Toast.makeText(InviteFriendsActivity.this,
							"Adding Friends", Toast.LENGTH_SHORT).show();
					friendIDsString = "";
					friendIDsString += friendsToAdd.get(0).id;
					for (int i = 0; i < friendsToAdd.size(); i++)
						friendIDsString += "," + friendsToAdd.get(i).id;
					Log.i("InviteFriendsActivity", "friendsIDsString: "
							+ friendIDsString);
					addParams = new Bundle();
					Log.i("InviteFriendsActivity", "request: " + "/" + eventID
							+ "/invited?users=" + friendIDsString);
					mAsyncRunner.request("/" + eventID + "/invited?users="
							+ friendIDsString, addParams, "POST",
							new RequestListener() {

								@Override
								public void onComplete(String arg0, Object arg1) {
									Log.i("Inviting response", arg0);

									SharedPreferences.Editor editor = settings
											.edit();
									editor.putBoolean("onEditMode", false);
									editor.commit();

									Log.i("InviteFriends Activity", "Finished");
									finish();
								}

								@Override
								public void onFacebookError(FacebookError arg0,
										Object arg1) {
								}

								@Override
								public void onFileNotFoundException(
										FileNotFoundException arg0, Object arg1) {
								}

								@Override
								public void onIOException(IOException arg0,
										Object arg1) {
								}

								@Override
								public void onMalformedURLException(
										MalformedURLException arg0, Object arg1) {
								}

							}, new Object());

				} else {
					SharedPreferences.Editor editor = settings.edit();
					editor.putBoolean("onEditMode", false);
					editor.commit();

					finish();
				}
			}
		});
	}

}
