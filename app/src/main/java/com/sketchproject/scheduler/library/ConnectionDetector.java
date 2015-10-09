package com.sketchproject.scheduler.library;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class ConnectionDetector {
	private Context _context;
    
    public ConnectionDetector(Context context){
        this._context = context;
    }
 
    public boolean isNetworkAvailable(){
        ConnectivityManager manager = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        boolean isAvailable = false;

        if(info != null && info.isConnected()){
            isAvailable = true;
        }

        return isAvailable;
    }
}
