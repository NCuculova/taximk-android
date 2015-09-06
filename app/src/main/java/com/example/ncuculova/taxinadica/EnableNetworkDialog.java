package com.example.ncuculova.taxinadica;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;


public class EnableNetworkDialog extends DialogFragment {

    public DialogInterface.OnClickListener mOnClickListener;

    public void setOnDialogClickListener(DialogInterface.OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.network_error)
                .setMessage(getString(R.string.check_internet))
                .setPositiveButton(R.string.action_check, mOnClickListener)
                .setNegativeButton(R.string.action_cancel, mOnClickListener);
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
