package net.wuehrer.sms_sender.plugin;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import android.net.Uri;
import android.util.Log;

import android.content.Context;
import android.widget.Toast;

import android.preference.PreferenceActivity;


import net.wuehrer.sms_sender.R;


import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStreamReader;
import java.io.Reader;

public class SmsTrade implements SenderPlugin{
    private static final String KEY_PREF_DEBUG = "pref_sms_trade_debug";
    private static final String KEY_PREF_SMS_KEY = "pref_sms_trade_sms_key";
    private static final String DEBUG_TAG = "SmsTrade";

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences_sms_trade from an XML resource
            addPreferencesFromResource(R.xml.preferences_sms_trade);
        }
    }

    public static class SettingsActivity extends PreferenceActivity{
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    private String message;
    private String recipient;
    private String sender;
    private Boolean debug;
    private String smsKey;
    private Activity callingActivity;

    public SmsTrade(String message, String recipient, String sender, Activity callingActivity) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(callingActivity);
        this.debug = sharedPref.getBoolean(KEY_PREF_DEBUG, false);
        this.smsKey = sharedPref.getString(KEY_PREF_SMS_KEY, null);
        this.callingActivity = callingActivity;
    }

    private String genUrl() {
        HashMap<String,String> params = new HashMap<String,String>();
        if(smsKey != null) {
            params.put("key", smsKey);
        }

        if(debug) {
            params.put("debug", "1");
        }
        params.put("concat", "1");
        params.put("route", "gold");
        params.put("message", message);
        params.put("to", recipient);
        params.put("from", sender);

        String url = "https://gateway.smstrade.de?";

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = Uri.encode(entry.getValue());
            url += key + "=" + value + "&";
        }

        Log.d(DEBUG_TAG, "Send Url: " + url);

        return url;
    }

    public void sendMessage() {
        String url = genUrl();

        ConnectivityManager connMgr = (ConnectivityManager)
                callingActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            Log.i(DEBUG_TAG, "Trying to start the HTTP-Request.");
            new DownloadWebpageTask().execute(url);
        } else {
            Toast.makeText(callingActivity, R.string.no_network, Toast.LENGTH_SHORT).show();
            Log.w(DEBUG_TAG, "Could not start HTTP-Request, because no network.");
        }
    }

    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
           // params comes from the execute() call: params[0] is the url.
           try {
               return callingActivity.getString(downloadUrl(urls[0]));
           } catch (IOException e) {
               Log.i(DEBUG_TAG, "Error while trying to send the Request!");
               Log.i(DEBUG_TAG, "Caught Exception: " + e.toString()  + "\n" + e.getMessage());
               return callingActivity.getString(R.string.network_error) + e.getMessage();
           }
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(callingActivity, result, Toast.LENGTH_SHORT).show();
            Log.d(DEBUG_TAG, "result" + result);
        }
    }

    // Given an URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private static int downloadUrl(String urlString) throws IOException {
        InputStream is = null;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            Log.i(DEBUG_TAG, "Before sending the Request:");

            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response is: " + response);
            is = conn.getInputStream();

            // Convert the InputStream into a string

            return readIt(is, 500);// Only display the first 500 characters of the retrieved web page content.

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    // Reads an InputStream and converts it to a String.
    private static int readIt(InputStream stream, int len) throws IOException {
        Reader reader;
        try{
            reader = new InputStreamReader(stream, "UTF-8");
            char[] buffer = new char[len];
            reader.read(buffer);
            String result = new String(buffer);
            result = result.replaceAll("[^0-9]","");
            int code = Integer.parseInt(result);
            Log.d(DEBUG_TAG, "Got code " + code + getMessageFromCode(code));
            return getMessageFromCode(code);
        } catch (java.lang.NumberFormatException e) {
            Log.w(DEBUG_TAG,"Caught NumberFormatException: " + e.getMessage());
            return R.string.service_code_invalid;
        } catch (Throwable e) {
            Log.w(DEBUG_TAG,"Caught unknown exception: " + e.getMessage());
            return R.string.service_code_unknown;
        }
    }

    private static int getMessageFromCode(int code) {
        switch(code) {
            case 10:
                return R.string.service_code_no_valid_recipient;
            case 20:
                return R.string.service_code_no_valid_sender;
            case 30:
                return R.string.service_code_no_valid_text;
            case 31:
                return R.string.service_code_no_valid_messagetype;
            case 40:
                return R.string.service_code_no_valid_route;
            case 50:
                return R.string.service_code_identification_failed;
            case 60:
                return R.string.service_code_no_credit;
            case 70:
                return R.string.service_code_receiver_network_not_reachable;
            case 71:
                return R.string.service_code_feature_not_available;
            case 80:
                return R.string.service_code_message_could_not_be_delivered;
            case 100:
                return R.string.service_code_message_sent;
            default:
                return R.string.service_code_unknown;
        }
    }
}
