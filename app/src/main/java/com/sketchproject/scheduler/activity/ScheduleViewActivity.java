package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by Angga on 10/8/2015.
 */
public class ScheduleViewActivity extends Activity {

    public static final String TAG = ScheduleViewActivity.class.getSimpleName();
    private final String KEY_ID = "id";
    private final String KEY_TOKEN = "token";
    private final String KEY_EVENT = "event";
    private final String KEY_DATE = "date";
    private final String KEY_TIME = "time";
    private final String KEY_LOCATION = "location";
    private final String KEY_DESCRIPTION = "description";

    private Button buttonBack;
    private Button buttonEdit;
    private Button buttonDelete;
    private TextView labelEvent;
    private TextView labelDate;
    private TextView labelTime;
    private TextView labelLocation;
    private TextView labelDescription;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    private String scheduleId;
    protected JSONObject scheduleData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_view);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(getApplicationContext());

        session = new SessionManager(getApplicationContext());

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonEdit = (Button) findViewById(R.id.buttonEdit);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        labelEvent = (TextView) findViewById(R.id.event);
        labelDate = (TextView) findViewById(R.id.date);
        labelTime = (TextView) findViewById(R.id.time);
        labelLocation = (TextView) findViewById(R.id.location);
        labelDescription = (TextView) findViewById(R.id.description);

        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        loadingScreen.setMinimumHeight(size.y);

        buttonBack.setOnClickListener(new BackHandler());
        buttonEdit.setOnClickListener(new EditHandler());
        buttonDelete.setOnClickListener(new DeleteHandler());

        Intent i = getIntent();
        scheduleId = i.getStringExtra(KEY_ID);

        updateScheduleDetail();
    }

    private void updateScheduleDetail(){
        if (connectionDetector.isNetworkAvailable()) {
            new GetScheduleViewTask().execute();
        }
        else {
            Toast.makeText(ScheduleViewActivity.this, "Network is unavailable!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private class GetScheduleViewTask extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ScheduleViewActivity.this);
            progress.setMessage("Retrieving schedule data ...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try{
                URL scheduleUrl = new URL(Constant.URL_SCHEDULE_EDIT);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter(KEY_ID, scheduleId);

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
                    jsonResponse = new JSONObject(responseData);
                }
                else{
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            }
            catch(MalformedURLException e){
                Log.e(TAG, "Exception caught: " + e);
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            scheduleData = result;
            progress.dismiss();
            populateScheduleResponse();
        }
    }

    public void populateScheduleResponse(){
        if(scheduleData == null){
            alert.showAlertDialog(ScheduleViewActivity.this, getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(scheduleData.getString("status").equals("success")){
                    JSONObject setting = new JSONObject(scheduleData.getString("schedule"));
                    labelEvent.setText(setting.getString(KEY_EVENT));
                    labelDate.setText(setting.getString(KEY_DATE));
                    labelTime.setText(setting.getString(KEY_TIME));
                    labelLocation.setText(setting.getString(KEY_LOCATION));
                    labelDescription.setText(setting.getString(KEY_DESCRIPTION));
                }
                else{
                    alert.showAlertDialog(ScheduleViewActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message), false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    private class BackHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    private class EditHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(ScheduleViewActivity.this, ScheduleEditActivity.class);
            intent.putExtra(KEY_ID, scheduleId);
            intent.putExtra(KEY_EVENT, labelEvent.getText());
            intent.putExtra(KEY_DATE, labelDate.getText());
            intent.putExtra(KEY_TIME, labelTime.getText());
            intent.putExtra(KEY_LOCATION, labelLocation.getText());
            intent.putExtra(KEY_DESCRIPTION, labelDescription.getText());
            startActivityForResult(intent, 100);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == Activity.RESULT_CANCELED){
                updateScheduleDetail();
            }
        }
    }

    private class DeleteHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            (new AlertDialog.Builder(ScheduleViewActivity.this))
                    .setTitle("CONFIRM DELETE")
                    .setMessage("Do you want to delete this schedule?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            new TaskDeleteScheduleData().execute();
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    private class TaskDeleteScheduleData extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(ScheduleViewActivity.this);
            progress.setMessage("Deleting schedule data ...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try{
                URL scheduleUrl = new URL(Constant.URL_SCHEDULE_DELETE);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("token", session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter("id", scheduleId);

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
                    jsonResponse = new JSONObject(responseData);
                }
                else{
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            }
            catch(MalformedURLException e){
                Log.e(TAG, "Exception caught: " + e);
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            progress.dismiss();
            handleDeleteScheduleResponse();
        }
    }

    public void handleDeleteScheduleResponse(){
        if(scheduleData == null){
            alert.showAlertDialog(ScheduleViewActivity.this, getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(scheduleData.getString("status").equals("restrict")){
                    alert.showAlertDialog(ScheduleViewActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message), true);
                    finish();
                }
                else if(scheduleData.getString("status").equals("success")){
                    //alert.showAlertDialog(ScheduleViewActivity.this, "Deleted", "Schedule successfully deleted", false);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                }
                else if(scheduleData.getString("status").equals("failed")){
                    alert.showAlertDialog(ScheduleViewActivity.this, "Failed", "Delete schedule failed", false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
