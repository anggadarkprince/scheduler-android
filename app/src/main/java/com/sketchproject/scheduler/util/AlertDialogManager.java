package com.sketchproject.scheduler.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sketchproject.scheduler.R;


public class AlertDialogManager {
	public AlertDialog alertDialog;

    /**
     * General dialog box for multipurpose
     * @param context context activity caller
     * @param title caption of message box
     * @param message content of message box
     */
    public void showAlertDialog(Context context, String title, String message) {
        alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
  
        alertDialog.show();
    }

    /**
     * Info dialog for information
     *
     * @param context context activity caller
     * @param title caption of message box
     * @param message content of message box
     */
	public static void dialogInfo(Context context, String title, String message){
		(new AlertDialog.Builder(context))
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setIcon(R.drawable.ic_info)
        .setNeutralButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })       
        .show();
	}

    /**
     * Confirm dialog for select an option
     *
     * @param context context activity caller
     * @param title caption of message box
     * @param message content of message box
     */
	public static void dialogConfirm(Context context, String title, String message, 
			String positiveLabel,
			DialogInterface.OnClickListener positiveListener, 
			String negativeLabel,
			DialogInterface.OnClickListener negativeListener){
		(new AlertDialog.Builder(context))
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setIcon(R.drawable.ic_cross)
        .setPositiveButton(positiveLabel,positiveListener)
        .setNegativeButton(negativeLabel,negativeListener)		                
        .show();
	}
}
