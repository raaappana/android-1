package uk.ac.gla.route2go;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import com.facebook.android.*;
import com.facebook.android.Facebook.*;

public class Main extends Activity {

    Facebook facebook = new Facebook("134601229976419");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        facebook.authorize(this, new String[] { "email", "offline_access", "publish_checkins"},
            new DialogListener() {
            @Override
            public void onComplete(Bundle values) {}

            @Override
            public void onFacebookError(FacebookError error) {}

            @Override
            public void onError(DialogError e) {}

            @Override
            public void onCancel() {}
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        facebook.authorizeCallback(requestCode, resultCode, data);
    }
}
