package uk.ac.gla.get2gether;

import android.app.Activity;
import android.content.res.Resources;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;

public class Utility extends Activity{
	
	private static Resources res;
	private static Facebook mFacebook;
	private static AsyncFacebookRunner mAsyncRunner;
	
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
