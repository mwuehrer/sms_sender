package net.wuehrer.sms_sender.fragment;

import android.os.Bundle;
import android.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.app.Dialog;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.Context;
import android.text.Html;

import net.wuehrer.sms_sender.R;

public class AboutDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View aboutView = inflater.inflate(R.layout.about, null);

        TextView tv = (TextView) aboutView.findViewById(R.id.text_view_version);
        tv.setText(String.format(tv.getText().toString(), getVersionNumber()));

        tv = (TextView) aboutView.findViewById(R.id.text_view_url);
        tv.setText(Html.fromHtml(String.format(tv.getText().toString(), getResources().getString(R.string.app_url))));

        tv = (TextView) aboutView.findViewById(R.id.text_view_author);
        tv.setText(String.format(tv.getText().toString(), getResources().getString(R.string.app_author)));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(aboutView);

        // Set Title
        builder.setTitle(String.format(getResources().getString(R.string.dialog_about_title),
                getResources().getString(R.string.app_name)));

        // Add action buttons
        builder.setPositiveButton(R.string.button_ok,null);

        return builder.create();
    }

    private String getVersionNumber() {
        Context context = getActivity().getApplicationContext();
        try{
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "n.a.";
        }
    }
}
