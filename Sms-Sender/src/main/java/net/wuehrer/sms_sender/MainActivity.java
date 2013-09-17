package net.wuehrer.sms_sender;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.content.Intent;
import android.widget.EditText;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.net.Uri;
import android.view.MenuItem;

import net.wuehrer.sms_sender.fragment.ListDialog;
import net.wuehrer.sms_sender.fragment.AboutDialog;
import net.wuehrer.sms_sender.plugin.SenderPlugin;
import net.wuehrer.sms_sender.plugin.SmsTrade;

import org.apache.http.impl.entity.StrictContentLengthStrategy;

import java.util.Vector;

public class MainActivity extends Activity implements ListDialog.ListDialogListener{
    private static final int CONTACT_PICKER_SENDER = 1001;
    private static final int CONTACT_PICKER_RECIPIENT = 1002;

    private static final String KEY_PREF_SENDER = "pref_sender";

    private static final String DEBUG_TAG = "MainActivity";

    private EditText editSender;
    private EditText editRecipient;
    private EditText editMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editSender = (EditText) findViewById(R.id.edit_sender);
        editRecipient = (EditText) findViewById(R.id.edit_recipient);
        editMessage = (EditText) findViewById(R.id.edit_message);

        //try to get the phones number, to prefill the sender-textbox
        try{
            String number = ((TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
            if(number!=null) {
                setPhoneNumber(number, CONTACT_PICKER_SENDER);
            }
        } catch (java.lang.NullPointerException e){
            Log.d(DEBUG_TAG, "Caught null-Pointer Exception, but it's not critical...");
        } catch (Throwable e) {
            Log.d(DEBUG_TAG, "Caught unknown Exception, but it's not critical...");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_about:
                showAbout();
                return true;
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_sender_settings:
                showSenderSettings();
                return true;
            case R.id.action_send:
                sendMessage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showAbout() {
        AboutDialog dialog = new AboutDialog();
        dialog.show(getFragmentManager(), "About");
    }

    private void showSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void showSenderSettings() {
        Class<?> pluginSettings = SenderHandler.getSenderSettingsClass(this);
        Intent intent = new Intent(this, pluginSettings);
        startActivity(intent);
    }

    public void selectSender(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
            Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_SENDER);
    }

    public void selectRecipient(View view) {
        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
            Contacts.CONTENT_URI);
        startActivityForResult(contactPickerIntent, CONTACT_PICKER_RECIPIENT);
    }

    /** Called when the user clicks the Send button */
    public void sendMessage() {
        SenderPlugin plugin = SenderHandler.getSenderInstance(this);
        plugin.sendMessage(editMessage.getText().toString(),
                editRecipient.getText().toString(), editSender.getText().toString());
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CONTACT_PICKER_SENDER:
                case CONTACT_PICKER_RECIPIENT:
                    readContact(data.getData(), requestCode);
                    break;
            }
        } else {
            // gracefully handle failure
            Log.w(DEBUG_TAG, "Warning: activity result not ok");
        }
    }

    private void setPhoneNumber(String text, int requestCode) {
        switch(requestCode) {
            case CONTACT_PICKER_RECIPIENT:
                editRecipient.setText(text);
                break;
            case CONTACT_PICKER_SENDER:
                editSender.setText(text);
                break;
        }
    }

    private void readContact(Uri contactUri, int requestCode) {
        Log.d(DEBUG_TAG, "Got a result: "
            + contactUri.toString());

        Cursor cursor = getContentResolver()
                        .query(contactUri, new String[]{Contacts._ID,Contacts.HAS_PHONE_NUMBER}, null, null, null);

        if(cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(Contacts._ID));
            Log.d(DEBUG_TAG, "Got ID: " + id);

            //if contact has more than one phone-numbers
            if (cursor.getInt(cursor.getColumnIndex(Contacts.HAS_PHONE_NUMBER))==1)
            {
                readPhoneNumber(id, requestCode);
            } else {
                setPhoneNumber("", requestCode);
            }
        }
        cursor.close();
    }

    private void  readPhoneNumber(int id, int requestCode) {
        Cursor phones = getContentResolver()
                .query( ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{
                                /*ContactsContract.CommonDataKinds.Phone.TYPE,*/
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER
                        },
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, null, null);
        Log.d(DEBUG_TAG, "Recieved " + phones.getCount() + " Phone-Numbers.");
        Vector<String> phoneNumbers = new Vector<String>();

        while (phones.moveToNext()) {
            //prefer the normalized number, but if it is not available, use the "normal" number
            String number = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER));
            if(number == null) {
                number = phones.getString(phones.getColumnIndex( ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            Log.d(DEBUG_TAG, "  Phone"+number);
            phoneNumbers.add(number);
        }

        phones.close();

        if(phoneNumbers.size() == 1) {
            String number = phoneNumbers.firstElement();
            setPhoneNumber(number, requestCode);
            Log.d(DEBUG_TAG, "Selected " + number);
        } else {
            selectPhoneNumber(phoneNumbers, requestCode);
        }
    }

    private void selectPhoneNumber(Vector<String> phoneNumbers, int requestCode) {
        ListDialog listFragment= new ListDialog(phoneNumbers.toArray(new String[phoneNumbers.size()]),
                getString(R.string.main_choose_phone), requestCode);
        listFragment.show(getFragmentManager(), "Contact_Selector");
    }

    public void onFinishListDialog(String selection, int requestCode){
        Log.d(DEBUG_TAG, "Selected " + selection);
        setPhoneNumber(selection, requestCode);
    }

}
