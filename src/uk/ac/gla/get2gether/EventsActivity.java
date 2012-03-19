package uk.ac.gla.get2gether;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import uk.ac.gla.get2gether.R;


public class EventsActivity extends TabActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_main);
		
		Resources res = getResources();
		final TabHost tabHost = getTabHost();
		TabHost.TabSpec spec; //reusable Tab spec for each tab;
		Intent intent; //reusable intent for each tab
		
		
		//create an intent to launch an activity for the tab (to be reused)
		intent = new Intent().setClass(this, CreateNewEventActivity.class);
		
		//initialize tab spec
		spec = tabHost.newTabSpec("").setIndicator("", res.getDrawable(R.drawable.tab_createnew)).setContent(intent);
		tabHost.addTab(spec);
		
		//other tab
		intent = new Intent().setClass(this, GetEvents.class);
		
		//initialize tab spec
		spec = tabHost.newTabSpec("").setIndicator("", res.getDrawable(R.drawable.tab_myevents)).setContent(intent);
		tabHost.addTab(spec);
		
		tabHost.setCurrentTab(2);
		setTabColor(tabHost);
		
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			//@Override
			public void onTabChanged(String tabId){
				
			}
		});
	}
	
	public static void setTabColor(TabHost tabHost){
		for (int i = 0; i<tabHost.getTabWidget().getChildCount(); i++)
			tabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_default_alternative);
	}
	
}