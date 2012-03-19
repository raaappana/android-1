package uk.ac.gla.get2gether;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Looper;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class Utility extends Activity{
	
	private static Resources res;
	private static Facebook mFacebook;
	private static AsyncFacebookRunner mAsyncRunner;
	private static Event event;

	public static Event getEvent() {
		return event;
	}

	public static void setEvent(Event eventToEdit) {
		Utility.event = eventToEdit;
	}

	// these could be in the xml files??????
	public final static int MAIN_CODE = 12;
	public final static int INVITE_FRIENDS_CODE = 65;
	public final static int CREATE_EVENT_CODE = 99;
	
	public static void setResources(Resources r) {
		res = r;
	}
	
	public static Facebook getFacebook() {
		if (mFacebook == null) 
			mFacebook = new Facebook(res.getString(R.string.fb_appid));
		return mFacebook;
	}
	
	public static AsyncFacebookRunner getAsyncRunner() {
		if (mAsyncRunner == null)
			mAsyncRunner = new AsyncFacebookRunner(getFacebook());
		return mAsyncRunner;
	}

}
