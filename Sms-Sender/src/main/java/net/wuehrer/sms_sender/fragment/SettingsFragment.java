package net.wuehrer.sms_sender.fragment;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import net.wuehrer.sms_sender.R;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences_sms_trade from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
