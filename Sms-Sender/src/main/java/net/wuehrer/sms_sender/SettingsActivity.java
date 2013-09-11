package net.wuehrer.sms_sender;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.wuehrer.sms_sender.fragment.SettingsFragment;

public class SettingsActivity extends PreferenceActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}