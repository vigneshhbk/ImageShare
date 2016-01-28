package com.example.finalproject;

import android.app.Application;
import com.facebook.FacebookSdk;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseTwitterUtils;

/**
 * Created by Vignesh on 11/22/2015.
 */
public class FinalProject extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FacebookSdk.sdkInitialize(getApplicationContext());
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "qqdR1rPUt0C01vpgFIH6JrLbqRJgL8BAcpfrqwRg", "zNAm0Oh81qnw54kV3TAnhjlYowQV98ENSUmQVpvq");
        ParseInstallation.getCurrentInstallation().saveInBackground();
        ParseFacebookUtils.initialize(this);
        ParseTwitterUtils.initialize("bz1IRCJBbfR6uiDpywLieVJAV", "0LvcIdvn4dawl3pS224c6GaXbpnbjl0iwqZ0Y8H7ZrSFzYUsYQ");
    }
}
