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
import java.util.Vector;
import java.util.Arrays;

import net.wuehrer.sms_sender.R;

public class ListDialog extends DialogFragment {
    private String[] items;
    private String title;
    private int requestCode;
    private ListDialog(){}

    public ListDialog(String[] _items, String _title, int _requestCode){
        items = _items;
        title = _title;
        requestCode = _requestCode;
    }

    public interface ListDialogListener {
        void onFinishListDialog(String selection, int requestCode);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
            .setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                ListDialogListener activity = (ListDialogListener) getActivity();
                activity.onFinishListDialog(items[which], requestCode);
            }
        });
        return builder.create();
    }
}