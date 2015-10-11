package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Scheduler Android App
 * Created by Angga on 10/8/2015.
 */
public class NoteViewActivity extends Activity {

    public static final String TAG = NoteViewActivity.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_TOKEN = "token";
    private final String KEY_TITLE = "title";
    private final String KEY_LABEL = "label";
    private final String KEY_NOTE = "note";

    private final String DATA_STATUS = "status";
    private final String DATA_NOTE = "note";

    private String noteId;
    protected JSONObject noteData;

    private Button buttonBack;
    private Button buttonEdit;
    private Button buttonDelete;
    private TextView labelTitle;
    private TextView labelLabel;
    private TextView labelNote;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_note_view);

        alert = new AlertDialogManager();
        connectionDetector = new ConnectionDetector(getApplicationContext());
        session = new SessionManager(getApplicationContext());

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonEdit = (Button) findViewById(R.id.buttonEdit);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        labelTitle = (TextView) findViewById(R.id.title);
        labelLabel = (TextView) findViewById(R.id.label);
        labelNote = (TextView) findViewById(R.id.note);

        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        buttonBack.setOnClickListener(new BackHandler());
        buttonEdit.setOnClickListener(new EditHandler());
        buttonDelete.setOnClickListener(new DeleteHandler());

        Intent i = getIntent();
        noteId = i.getStringExtra(KEY_ID);

        updateNoteDetail();
    }

    /**
     * Update note detail from database
     */
    public void updateNoteDetail(){
        if (connectionDetector.isNetworkAvailable()) {
            new RetrieveNoteHandler().execute();
        }
        else {
            Toast.makeText(NoteViewActivity.this, getString(R.string.disconnect), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Async task to retrieve note detail related tapped list
     * this method will passing JSON object as [status] and [note]
     */
    private class RetrieveNoteHandler extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(NoteViewActivity.this);
            progress.setMessage(getString(R.string.loading_note_retrieve));
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try{
                URL noteUrl = new URL(Constant.URL_NOTE_EDIT);
                HttpURLConnection connection = (HttpURLConnection) noteUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter(KEY_ID, noteId);

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
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            noteData = result;
            progress.dismiss();
            populateNoteResponse();
        }
    }

    /**
     * Handle result note from database
     * get string from json object and pass to text view
     */
    public void populateNoteResponse(){
        if(noteData == null){
            alert.showAlertDialog(NoteViewActivity.this, getString(R.string.error_title), getString(R.string.error_message));
        }
        else{
            try {
                if(noteData.getString("status").equals("success")){
                    JSONObject setting = new JSONObject(noteData.getString(DATA_NOTE));
                    labelTitle.setText(setting.getString(KEY_TITLE));
                    labelLabel.setText(setting.getString(KEY_LABEL));
                    labelNote.setText(setting.getString(KEY_NOTE));
                }
                else{
                    alert.showAlertDialog(NoteViewActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message));
                    finish();
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * Listener for back to list button
     */
    private class BackHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            finish();
        }
    }

    /**
     * Listener for call edit activity and passing data
     */
    private class EditHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(NoteViewActivity.this, NoteEditActivity.class);
            intent.putExtra(KEY_ID, noteId);
            intent.putExtra(KEY_TITLE, labelTitle.getText());
            intent.putExtra(KEY_LABEL, labelLabel.getText());
            intent.putExtra(KEY_NOTE, labelNote.getText());
            startActivityForResult(intent, 100);
        }
    }

    /**
     * receive signal for result when call edit note activity has closed
     * so note detail will be updated on the fly
     *
     * @param requestCode identifier request from this activity
     * @param resultCode  result type from called activity
     * @param data        return data from called activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == Activity.RESULT_CANCELED){
                updateNoteDetail();
            }
        }
    }

    /**
     * Listener for delete note button
     * confirm to delete the note record
     */
    private class DeleteHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            (new AlertDialog.Builder(NoteViewActivity.this))
                    .setTitle(R.string.action_delete_confirm)
                    .setMessage(R.string.message_note_delete)
                    .setCancelable(false)
                    .setPositiveButton("YES",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            new DeleteNoteHandler().execute();
                        }
                    })
                    .setNegativeButton("NO",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    /**
     * Async task to make delete request to server
     * this method will return transaction delete status
     */
    private class DeleteNoteHandler extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(NoteViewActivity.this);
            progress.setMessage(getString(R.string.loading_note_delete));
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try{
                URL noteUrl = new URL(Constant.URL_NOTE_DELETE);
                HttpURLConnection connection = (HttpURLConnection) noteUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter(KEY_ID, noteId);

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
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            progress.dismiss();
            handleDeleteNoteResponse();
        }
    }

    /**
     * Handle result delete note from database
     * check delete status
     */
    public void handleDeleteNoteResponse(){
        if(noteData == null){
            alert.showAlertDialog(NoteViewActivity.this, getString(R.string.error_title), getString(R.string.error_message));
        }
        else{
            try {
                String status = noteData.getString(DATA_STATUS);
                if(status.equals(Constant.STATUS_RESTRICT)){
                    alert.showAlertDialog(NoteViewActivity.this, getString(R.string.restrict_title), getString(R.string.restrict_message));
                    finish();
                }
                else if(status.equals(Constant.STATUS_SUCCESS)){
                    Intent returnIntent = new Intent();
                    setResult(RESULT_CANCELED, returnIntent);
                    finish();
                }
                else if(status.equals(Constant.STATUS_FAILED)){
                    alert.showAlertDialog(NoteViewActivity.this, getString(R.string.action_failed), getString(R.string.message_note_delete_failed));
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
