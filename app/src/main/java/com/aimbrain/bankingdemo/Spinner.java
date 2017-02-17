package com.aimbrain.bankingdemo;

import android.app.ProgressDialog;
import android.content.Context;


public class Spinner {
    public static ProgressDialog showSpinner(Context context) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        dialog.show();
        return dialog;
    }
}
