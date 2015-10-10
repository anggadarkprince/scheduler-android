package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
public class NoteCreateActivity extends Activity{
    public static final String TAG = ScheduleCreateActivity.class.getSimpleName();

    private final String KEY_ID = "user_id";
    private final String KEY_TOKEN = "token";
    private final String KEY_TITLE = "title";
    private final String KEY_LABEL = "label";
    private final String KEY_NOTE = "note";

    protected JSONObject noteData;

    private Button buttonSave;
    private EditText textTitle;
    private EditText textLabel;
    private EditText textNote;

    private TextView infoTitle;
    private TextView infoLabel;
    private TextView infoNote;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    private String title;
    private String label;
    private String note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_add);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(NoteCreateActivity.this);

        session = new SessionManager(NoteCreateActivity.this);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        textTitle = (EditText) findViewById(R.id.title);
        textLabel = (EditText) findViewById(R.id.label);
        textNote = (EditText) findViewById(R.id.note);

        infoTitle = (TextView) findViewById(R.id.infoTitle);
        infoLabel = (TextView) findViewById(R.id.infoLabel);
        infoNote = (TextView) findViewById(R.id.infoNote);

        buttonSave.setOnClickListener(new SaveNoteHandler());
    }

    private boolean isValidated(){
        boolean checkTitle = true;
        boolean checkLabel = true;
        boolean checkNote = true;

        title = textTitle.getText().toString();
        if(title.trim().isEmpty()){
            infoTitle.setText("Field Title can't be empty");
            infoTitle.setVisibility(View.VISIBLE);
            checkTitle = false;
        }
        else if(title.length() > 200){
            infoTitle.setText("Field Title allow max length 200 characters");
            infoTitle.setVisibility(View.VISIBLE);
            checkTitle = false;
        }

        label = textLabel.getText().toString();
        if(label.trim().isEmpty()){
            infoLabel.setText("Field Label can't be empty");
            infoLabel.setVisibility(View.VISIBLE);
            checkLabel = false;
        }
        else if(label.length() > 50){
            infoLabel.setText("Field Label allow max length 50 characters");
            infoLabel.setVisibility(View.VISIBLE);
            checkLabel = false;
        }

        note = textNote.getText().toString();
        if(note.trim().isEmpty()){
            infoNote.setText("Field Note can't be empty");
            infoNote.setVisibility(View.VISIBLE);
            checkNote = false;
        }

        return checkTitle && checkLabel && checkNote;
    }

    private class SaveNoteHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (connectionDetector.isNetworkAvailable()) {
                infoTitle.setVisibility(View.GONE);
                infoLabel.setVisibility(View.GONE);
                infoNote.setVisibility(View.GONE);
                if(isValidated()){
                    if (connectionDetector.isNetworkAvailable()) {
                        new SaveNoteAsync().execute(title, label, note);
                    }
                    else {
                        Toast.makeText(NoteCreateActivity.this, "Network is unavailable!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    alert.showAlertDialog(NoteCreateActivity.this, "Validation", "Please complete the form", true);
                }
            } else {
                alert.showAlertDialog(NoteCreateActivity.this, "Save Failed", "No Internet Connection", false);
            }
        }
    }

    private class SaveNoteAsync extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(NoteCreateActivity.this);
            progress.setMessage("Saving note data ...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonResponse = null;

            try{
                URL accountUrl = new URL(Constant.URL_NOTE_INSERT);
                HttpURLConnection connection = (HttpURLConnection) accountUrl.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TITLE, params[0])
                        .appendQueryParameter(KEY_LABEL, params[1])
                        .appendQueryParameter(KEY_NOTE, params[2])
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
            noteData = result;
            progress.dismiss();
            handleSaveNoteResponse();
        }
    }

    public void handleSaveNoteResponse(){
        if(noteData == null){
            alert.showAlertDialog(NoteCreateActivity.this, getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(noteData.getString("status").equals("restrict")){
                    alert.showAlertDialog(NoteCreateActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message), true);
                    finish();
                }
                else if(noteData.getString("status").equals("success")){
                    //alert.showAlertDialog(ScheduleCreateActivity.this, "Created", "Note successfully created", false);
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                }
                else if(noteData.getString("status").equals("failed")){
                    alert.showAlertDialog(NoteCreateActivity.this, "Failed", "Save note failed", false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
