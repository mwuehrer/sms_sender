package net.wuehrer.sms_sender;

import android.util.Log;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.widget.EditText;
import net.wuehrer.sms_sender.plugin.SenderPlugin;
import net.wuehrer.sms_sender.plugin.SmsTrade;

public class SenderHandler{
    private static String sender = null;
    private static Class<?> senderSettingsClass = null;
    private static SenderPlugin senderInstance = null;
    private static final String KEY_PREF_SENDER = "pref_sender";

    private static final String DEBUG_TAG = "SenderHandler";

    public static void getInstances(Activity activity) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(activity);
        String senderString = sharedPref.getString(KEY_PREF_SENDER, activity.getString(R.string.pref_sender_default));
        sender = senderString;
        Log.d(DEBUG_TAG,sender);
        if(sender.equals("SmsTrade")) {
            senderSettingsClass = SmsTrade.SettingsActivity.class;
            senderInstance = new SmsTrade(activity);
        } else {
            senderSettingsClass = null;
            senderInstance = null;
        }
    }

    public static Class<?> getSenderSettingsClass(Activity activity) {
        if(sender == null) {
            getInstances(activity);
        }
        return senderSettingsClass;
    }

    //@TODO: maybe we should do this, so, that we set the text and recipients, aso. later, not on the construct
    public static SenderPlugin getSenderInstance(Activity activity) {
        if(sender == null) {
            getInstances(activity);
        }
        return senderInstance;
    }

    public static void reset() {
        sender = null;
    }
}
