package com.sketchproject.scheduler.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.sketchproject.scheduler.R;


public class AlertDialogManager {
	public AlertDialog alertDialog;
	
	public void showAlertDialog(Context context, String title, String message, Boolean status) {
        alertDialog = new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        if(status != null)
            alertDialog.setIcon((status) ? R.drawable.ic_info : R.drawable.ic_cross);
  
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
  
        alertDialog.show();
    }

	public static void dialogInfo(Context context, String title, String message){
		(new AlertDialog.Builder(context))
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setIcon(R.drawable.ic_cross)
        .setNeutralButton("OK",new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        })       
        .show();
	}
	
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
