package uk.ac.gla.get2gether.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.idansof.otp.client.Leg;
import org.idansof.otp.client.WalkStep;

import uk.ac.gla.get2gether.Map;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

public class WalkSteps extends ListActivity {
	
	ArrayList<WalkStep> wsl = new ArrayList<WalkStep>();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);

	  ArrayList<String> wsd = new ArrayList<String>();
	  
	  
	  List<Leg> legs = Map.itinerary.getLegs();
	  for (Leg l : legs) {
		  for (WalkStep w : l.getWalkSteps()) {
			  String rel;
			  if (w.getRelativeDirection() == null)
				  	rel = "Start";
			  else
				  rel = w.getRelativeDirection().toString();
			  wsd.add(rel+" on "+w.getLocation().getAddress());
			  wsl.add(w);
			  
		  }
	  }
	  
	  /*
      SharedPreferences.Editor editor = prefs.edit();
	 
      prefs.
      editor.putStringSet("walksteps", new HashSet<String>(wsd));
	  
	  */
	  setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, wsd));

	  ListView lv = getListView();
	  lv.setTextFilterEnabled(true);

	  lv.setOnItemClickListener(new OnItemClickListener() {
	    public void onItemClick(AdapterView<?> parent, View view,
	        int position, long id) {
	      // When clicked, show a toast with the TextView text
	      /*Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
	          Toast.LENGTH_SHORT).show(); */
	    	Intent resultIntent = new Intent();
	    	resultIntent.putExtra("walkStepLatitude", wsl.get(position).getLocation().getLatitude());
	    	resultIntent.putExtra("walkStepLongitude", wsl.get(position).getLocation().getLongitude());
	    	resultIntent.putExtra("walkStepDescription", 
	    			wsl.get(position).getRelativeDirection().toString()+" on "
	    					+wsl.get(position).getLocation().getAddress());
	    	setResult(Activity.RESULT_OK, resultIntent);
	    	finish();
	    }
	  });
	}
}
