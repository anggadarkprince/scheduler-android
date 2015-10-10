package com.sketchproject.scheduler.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.scheduler.ApplicationActivity;
import com.sketchproject.scheduler.FeaturedActivity;
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
 * Created by Angga on 10/7/2015.
 */
public class ScreenSetting extends Fragment {

    public static final String TAG = ScreenSetting.class.getSimpleName();

    protected JSONObject settingData;

    private Button buttonSave;
    private EditText textName;
    private EditText textWork;
    private EditText textAbout;
    private EditText textPassword;
    private EditText textNewPassword;
    private EditText textConfirmPassword;

    private TextView infoName;
    private TextView infoWork;
    private TextView infoAbout;
    private TextView infoPassword;
    private TextView infoConfirmPassword;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    private String name;
    private String work;
    private String about;
    private String password;
    private String newPassword;
    private String confirmPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_setting, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(getContext());

        session = new SessionManager(getActivity().getApplicationContext());

        buttonSave = (Button) getActivity().findViewById(R.id.buttonSave);
        textName = (EditText) getActivity().findViewById(R.id.name);
        textWork = (EditText) getActivity().findViewById(R.id.work);
        textAbout = (EditText) getActivity().findViewById(R.id.about);
        textPassword =  (EditText) getActivity().findViewById(R.id.password);
        textNewPassword =  (EditText) getActivity().findViewById(R.id.newPassword);
        textConfirmPassword =  (EditText) getActivity().findViewById(R.id.confirmPassword);

        infoName = (TextView) getActivity().findViewById(R.id.infoName);
        infoWork = (TextView) getActivity().findViewById(R.id.infoWork);
        infoAbout = (TextView) getActivity().findViewById(R.id.infoAbout);
        infoPassword = (TextView) getActivity().findViewById(R.id.infoPassword);
        infoConfirmPassword = (TextView) getActivity().findViewById(R.id.infoConfirmPassword);

        loadingScreen = (LinearLayout) getActivity().findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) getActivity().findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        loadingScreen.setMinimumHeight(size.y);

        buttonSave.setOnClickListener(new SaveSettingHandler());

        if (connectionDetector.isNetworkAvailable()) {
            GetSettingTask scheduleTask = new GetSettingTask();
            scheduleTask.execute();
        }
        else {
            Toast.makeText(getContext(), "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private class GetSettingTask extends AsyncTask<Object, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingScreen.setVisibility(View.VISIBLE);
            loadingAnimation.start();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try{
                URL scheduleUrl = new URL(Constant.URL_ACCOUNT);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("token", session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter("id", session.getUserDetails().get(SessionManager.KEY_ID));

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
            settingData = result;
            loadingScreen.setVisibility(View.GONE);
            loadingAnimation.stop();
            populateSettingResponse();
        }
    }

    public void populateSettingResponse(){
        if(settingData == null){
            alert.showAlertDialog(getContext(), getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(settingData.getString("status").equals("success")){
                    JSONObject setting = new JSONObject(settingData.getString("user"));
                    textName.setText(setting.getString("name"));
                    textWork.setText(setting.getString("work"));
                    textAbout.setText(setting.getString("about"));
                }
                else{
                    alert.showAlertDialog(getContext(), getString(R.string.restrict_title), getString(R.string.restrict_message), false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * form validation
     * check length and required data
     * @return boolean
     */
    private boolean isValidated(){
        boolean checkName = true;
        boolean checkWork = true;
        boolean checkAbout = true;
        boolean checkPassword = true;
        boolean checkConfirmPassword = true;

        name = textName.getText().toString();
        if(name.trim().isEmpty()){
            infoName.setText("Field Name can't be empty");
            infoName.setVisibility(View.VISIBLE);
            checkName = false;
        }
        else if(name.length() > 100){
            infoName.setText("Field Name allow max length 100 characters");
            infoName.setVisibility(View.VISIBLE);
            checkName = false;
        }

        work = textWork.getText().toString();
        if(work.trim().isEmpty()){
            infoWork.setText("Field Work can't be empty");
            infoWork.setVisibility(View.VISIBLE);
            checkWork = false;
        }
        else if(name.length() > 100){
            infoWork.setText("Field Work allow max length 100 characters");
            infoWork.setVisibility(View.VISIBLE);
            checkWork = false;
        }

        about = textAbout.getText().toString();
        if(about.trim().isEmpty()){
            infoAbout.setText("Field About can't be empty");
            infoAbout.setVisibility(View.VISIBLE);
            checkAbout = false;
        }
        else if(about.length() > 300){
            infoAbout.setText("Field About allow max length 300 characters");
            infoAbout.setVisibility(View.VISIBLE);
            checkAbout = false;
        }

        password = textPassword.getText().toString();
        if(password.trim().isEmpty()){
            infoPassword.setText("Type your current Password to save");
            infoPassword.setVisibility(View.VISIBLE);
            checkPassword = false;
        }

        newPassword = textNewPassword.getText().toString();
        confirmPassword = textConfirmPassword.getText().toString();
        Log.e(TAG, newPassword+"  "+confirmPassword);
        if(!newPassword.trim().equals(confirmPassword.trim())){
            infoConfirmPassword.setText("New password must match");
            infoConfirmPassword.setVisibility(View.VISIBLE);
            checkConfirmPassword = false;
        }

        return checkName && checkWork && checkAbout && checkPassword && checkConfirmPassword;
    }

    private class SaveSettingHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (connectionDetector.isNetworkAvailable()) {
                infoName.setVisibility(View.GONE);
                infoWork.setVisibility(View.GONE);
                infoAbout.setVisibility(View.GONE);
                infoPassword.setVisibility(View.GONE);
                infoConfirmPassword.setVisibility(View.GONE);
                if(isValidated()){
                    new UpdateSettingAsync().execute(name, work, about, password, newPassword);
                }
                else{
                    alert.showAlertDialog(getContext(), "Validation", "Please complete the form", true);
                }
            } else {
                alert.showAlertDialog(getContext(), "Save Failed", "No Internet Connection", false);
            }
        }
    }


    private class UpdateSettingAsync extends AsyncTask<String, String, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingScreen.setVisibility(View.VISIBLE);
            loadingAnimation.start();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonResponse = null;

            try{
                URL accountUrl = new URL(Constant.URL_ACCOUNT_UPDATE);
                HttpURLConnection connection = (HttpURLConnection) accountUrl.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("name", params[0])
                        .appendQueryParameter("work", params[1])
                        .appendQueryParameter("about", params[2])
                        .appendQueryParameter("password", params[3])
                        .appendQueryParameter("password_new", params[4])
                        .appendQueryParameter("id", session.getUserDetails().get(SessionManager.KEY_ID))
                        .appendQueryParameter("username", session.getUserDetails().get(SessionManager.KEY_USERNAME));

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
            settingData = result;
            loadingScreen.setVisibility(View.GONE);
            loadingAnimation.stop();
            handleSaveSettingResponse();
        }
    }

    public void handleSaveSettingResponse(){
        if(settingData == null){
            alert.showAlertDialog(getContext(), getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                textPassword.setText("");
                if(settingData.getString("status").equals("restrict")){
                    infoPassword.setText("Please input current correct password");
                    infoPassword.setVisibility(View.VISIBLE);
                    alert.showAlertDialog(getContext(), getString(R.string.restrict_title), getString(R.string.restrict_message), true);
                }
                else if(settingData.getString("status").equals("mismatch")){
                    alert.showAlertDialog(getContext(), "Mismatched", "Your current password is mismatch", true);
                }
                else if(settingData.getString("status").equals("success")){
                    alert.showAlertDialog(getContext(), "Updated", "Account setting updated", false);
                }
                else if(settingData.getString("status").equals("failed")){
                    alert.showAlertDialog(getContext(), "Failed", "Account update failed", false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
