package net.wuehrer.sms_sender;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

import net.wuehrer.sms_sender.fragment.SettingsFragment;
import net.wuehrer.sms_sender.SenderHandler;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String KEY_PREF_SENDER = "pref_sender";

    private static final String DEBUG_TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_SENDER)) {
            SenderHandler.reset();
        }
    }
}
