package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.library.ConnectionDetector;
import com.sketchproject.scheduler.library.SessionManager;
import com.sketchproject.scheduler.util.AlertDialogManager;
import com.sketchproject.scheduler.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class ScheduleEditActivity extends Activity {
    public static final String TAG = ScheduleEditActivity.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_TOKEN = "token";
    private final String KEY_EVENT = "event";
    private final String KEY_DATE = "date";
    private final String KEY_TIME = "time";
    private final String KEY_LOCATION = "location";
    private final String KEY_DESCRIPTION = "description";

    private final String DATA_STATUS = "status";

    private String scheduleId;
    private JSONObject scheduleData;

    private Button buttonUpdate;
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

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    private String event;
    private String date;
    private String time;
    private String location;
    private String description;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener(){

        @Override
        public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            year = selectedYear;
            month = selectedMonth;
            day = selectedDay;

            textDate.setText(new StringBuilder().append(year).append("-").append(month + 1).append("-").append(day));
        }
    };

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
            hour = selectedHour;
            minute = selectedMinute;

            textTime.setText(new StringBuilder().append(pad(hour)).append(":").append(pad(minute)));
        }
    };

    private static String pad(int c){
        if(c >= 10){
            return String.valueOf(c);
        }
        return "0"+String.valueOf(c);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_edit);

        alert = new AlertDialogManager();
        connectionDetector = new ConnectionDetector(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        buttonUpdate = (Button) findViewById(R.id.buttonUpdate);
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

        textDate.setOnClickListener(new DateClickHandler());
        textDate.setOnFocusChangeListener(new DateFocusHandler());
        textTime.setOnClickListener(new TimeClickHandler());
        textTime.setOnFocusChangeListener(new TimeFocusHandler());
        buttonUpdate.setOnClickListener(new UpdateSchedule());

        Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        Intent i = getIntent();
        scheduleId = i.getStringExtra(KEY_ID);
        textEvent.setText(i.getStringExtra(KEY_EVENT));
        textDate.setText(i.getStringExtra(KEY_DATE));
        textTime.setText(i.getStringExtra(KEY_TIME));
        textLocation.setText(i.getStringExtra(KEY_LOCATION));
        textDescription.setText(i.getStringExtra(KEY_DESCRIPTION));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == 1){
            return new DatePickerDialog(this, datePickerListener, year, month, day);
        }
        else if(id == 2){
            return new TimePickerDialog(this, timePickerListener, hour, minute, true);
        }
        return null;
    }

    private class DateFocusHandler implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                showDialog(1);
            }
        }
    }

    private class DateClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDialog(1);
        }
    }

    private class TimeFocusHandler implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus){
                showDialog(2);
            }
        }
    }

    private class TimeClickHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDialog(2);
        }
    }

    /**
     * Listener for update button
     */
    private class UpdateSchedule implements View.OnClickListener {
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
                        new UpdateScheduleHandler().execute(event, date, time, location, description);
                    }
                    else {
                        Toast.makeText(ScheduleEditActivity.this, getString(R.string.disconnect), Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    alert.showAlertDialog(ScheduleEditActivity.this, getString(R.string.validation), getString(R.string.validation_message));
                }
            } else {
                Toast.makeText(ScheduleEditActivity.this, getString(R.string.disconnect), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Async task to make update request to server
     * this method will return transaction update status
     */
    private class UpdateScheduleHandler extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ScheduleEditActivity.this);
            progress.setMessage(getString(R.string.loading_schedule_update));
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonResponse = null;

            try{
                URL accountUrl = new URL(Constant.URL_SCHEDULE_UPDATE);
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
                        .appendQueryParameter(KEY_ID, scheduleId)
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
            } catch (Exception e){
                Log.e(TAG, "Exception caught: " + e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            scheduleData = result;
            progress.dismiss();
            handleUpdateScheduleResponse();
        }
    }

    /**
     * Handle result update schedule from database
     * check update status
     */
    public void handleUpdateScheduleResponse(){
        if(scheduleData == null){
            alert.showAlertDialog(ScheduleEditActivity.this, getString(R.string.error_title), getString(R.string.error_message));
        }
        else{
            try {
                String status = scheduleData.getString(DATA_STATUS);
                switch (status) {
                    case Constant.STATUS_RESTRICT:
                        alert.showAlertDialog(ScheduleEditActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message));
                        finish();
                        break;
                    case Constant.STATUS_SUCCESS:
                        Intent returnIntent = new Intent();
                        setResult(RESULT_CANCELED, returnIntent);
                        finish();
                        break;
                    case Constant.STATUS_FAILED:
                        alert.showAlertDialog(ScheduleEditActivity.this, getString(R.string.action_failed), getString(R.string.message_schedule_update));
                        break;
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Check validation
     *
     * @return boolean
     */
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
}
