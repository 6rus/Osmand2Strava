package fr.rezvani.osmand2strava;


import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by cyrus on 02/02/17.
 */



public class Settings extends PreferenceActivity {





    @Override
    protected void onCreate(Bundle savedInstanceState) {






        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);



    //  prefs.edit().putString(CLIENT_CODE_KEY, client_code).apply();


    }



}
