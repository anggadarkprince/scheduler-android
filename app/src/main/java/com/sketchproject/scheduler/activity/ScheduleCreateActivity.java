package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.library.ConnectionDetector;
import com.sketchproject.scheduler.library.SessionManager;
import com.sketchproject.scheduler.util.AlertDialogManager;
import com.sketchproject.scheduler.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Angga on 10/7/2015.
 */
public class ScheduleCreateActivity extends Activity {
    public static final String TAG = ScheduleCreateActivity.class.getSimpleName();

    private final String KEY_ID = "user_id";
    private final String KEY_TOKEN = "token";
    private final String KEY_EVENT = "event";
    private final String KEY_DATE = "date";
    private final String KEY_TIME = "time";
    private final String KEY_LOCATION = "location";
    private final String KEY_DESCRIPTION = "description";

    protected JSONObject scheduleData;

    private Button buttonSave;
    private EditText textEvent;
    private EditText textDate;
    private EditText textTime;
    private EditText textLocation;
    private EditText textDescription;

    private TextView infoEvent;
    private TextView infoDate;
    private TextView infoTime;
    private TextView infoLocation;
    private TextView infoDescription;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    private String event;
    private String date;
    private String time;
    private String location;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_add);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(ScheduleCreateActivity.this);

        session = new SessionManager(ScheduleCreateActivity.this);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        textEvent = (EditText) findViewById(R.id.event);
        textDate = (EditText) findViewById(R.id.date);
        textTime = (EditText) findViewById(R.id.time);
        textLocation = (EditText) findViewById(R.id.location);
        textDescription = (EditText) findViewById(R.id.description);

        infoEvent = (TextView) findViewById(R.id.infoEvent);
        infoDate = (TextView) findViewById(R.id.infoDate);
        infoTime = (TextView) findViewById(R.id.infoTime);
        infoLocation = (TextView) findViewById(R.id.infoLocation);
        infoDescription = (TextView) findViewById(R.id.infoDescription);

        buttonSave.setOnClickListener(new SaveScheduleHandler());

        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        loadingScreen.setMinimumHeight(size.y);
    }

    private boolean isValidated(){
        boolean checkEvent = true;
        boolean checkDate = true;
        boolean checkTime = true;
        boolean checkLocation = true;
        boolean checkDescription = true;

        event = textEvent.getText().toString();
        if(event.trim().isEmpty()){
            infoEvent.setText("Field Event can't be empty");
            infoEvent.setVisibility(View.VISIBLE);
            checkEvent = false;
        }
        else if(event.length() > 200){
            infoEvent.setText("Field Event allow max length 200 characters");
            infoEvent.setVisibility(View.VISIBLE);
            checkEvent = false;
        }

        date = textDate.getText().toString();
        if(date.trim().isEmpty()){
            infoDate.setText("Field Date can't be empty");
            infoDate.setVisibility(View.VISIBLE);
            checkDate = false;
        }
        else if(date.length() > 10){
            infoDate.setText("Please input date like format eg. 2015-08-23");
            infoDate.setVisibility(View.VISIBLE);
            checkDate = false;
        }

        time = textTime.getText().toString();
        if(time.trim().isEmpty()){
            infoTime.setText("Field Time can't be empty");
            infoTime.setVisibility(View.VISIBLE);
            checkTime = false;
        }
        else if(time.length() > 8){
            infoTime.setText("Please input time like format eg. 17:30");
            infoTime.setVisibility(View.VISIBLE);
            checkTime = false;
        }

        location = textLocation.getText().toString();
        if(location.trim().isEmpty()){
            infoLocation.setText("Field Location can't be empty");
            infoLocation.setVisibility(View.VISIBLE);
            checkLocation = false;
        }
        else if(location.length() > 200){
            infoLocation.setText("Field Event allow max length 200 characters");
            infoLocation.setVisibility(View.VISIBLE);
            checkLocation = false;
        }

        description = textDescription.getText().toString();
        if(description.trim().isEmpty()){
            infoDescription.setText("Field Description can't be empty");
            infoDescription.setVisibility(View.VISIBLE);
            checkDescription = false;
        }

        return checkEvent && checkDate && checkTime && checkLocation && checkDescription;
    }

    private class SaveScheduleHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (connectionDetector.isNetworkAvailable()) {
                infoEvent.setVisibility(View.GONE);
                infoDate.setVisibility(View.GONE);
                infoTime.setVisibility(View.GONE);
                infoLocation.setVisibility(View.GONE);
                infoDescription.setVisibility(View.GONE);
                if(isValidated()){
                    if (connectionDetector.isNetworkAvailable()) {
                        new SaveScheduleAsync().execute(event, date, time, location, description);
                    }
                    else {
                        Toast.makeText(ScheduleCreateActivity.this, "Network is unavailable!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    alert.showAlertDialog(ScheduleCreateActivity.this, "Validation", "Please complete the form", true);
                }
            } else {
                alert.showAlertDialog(ScheduleCreateActivity.this, "Save Failed", "No Internet Connection", false);
            }
        }
    }

    private class SaveScheduleAsync extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ScheduleCreateActivity.this);
            progress.setMessage("Saving schedule data ...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonResponse = null;

            try{
                URL accountUrl = new URL(Constant.URL_SCHEDULE_INSERT);
                HttpURLConnection connection = (HttpURLConnection) accountUrl.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_EVENT, params[0])
                        .appendQueryParameter(KEY_DATE, params[1])
                        .appendQueryParameter(KEY_TIME, params[2])
                        .appendQueryParameter(KEY_LOCATION, params[3])
                        .appendQueryParameter(KEY_DESCRIPTION, params[4])
                        .appendQueryParameter(KEY_ID, session.getUserDetails().get(SessionManager.KEY_ID))
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN));

                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                connection.connect();

                int responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);

                    String responseData = new String(charArray);
                    Log.e(TAG, responseData);
                    jsonResponse = new JSONObject(responseData);
                }
                else{
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            }
            catch(MalformedURLException e){
                Log.e(TAG, "Exception caught: " + e);
            }
            catch(IOException e){
                Log.e(TAG, "Exception caught: " + e);
            }
            catch (Exception e){
                Log.e(TAG, "Exception caught: " + e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            scheduleData = result;
            progress.dismiss();
            handleSaveScheduleResponse();
        }
    }

    public void handleSaveScheduleResponse(){
        if(scheduleData == null){
            alert.showAlertDialog(ScheduleCreateActivity.this, getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(scheduleData.getString("status").equals("restrict")){
                    alert.showAlertDialog(ScheduleCreateActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message), true);
                    finish();
                }
                else if(scheduleData.getString("status").equals("success")){
                    //alert.showAlertDialog(ScheduleCreateActivity.this, "Created", "Schedule successfully created", false);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                }
                else if(scheduleData.getString("status").equals("failed")){
                    alert.showAlertDialog(ScheduleCreateActivity.this, "Failed", "Save schedule failed", false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
