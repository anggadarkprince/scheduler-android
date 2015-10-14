package com.sketchproject.scheduler.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.io.BufferedReader;
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
 * Created by Angga on 10/7/2015.
 */
public class ScreenSetting extends Fragment {

    public static final String TAG = ScreenSetting.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_TOKEN = "token";
    private final String KEY_NAME = "name";
    private final String KEY_WORK = "work";
    private final String KEY_ABOUT = "about";
    private final String KEY_USERNAME = "username";
    private final String KEY_PASSWORD = "password";
    private final String KEY_PASSWORD_NEW = "password_new";

    private final String DATA_STATUS = "status";
    private final String DATA_USER = "user";

    private JSONObject settingData;

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
        session = new SessionManager(getActivity());

        buttonSave = (Button) getActivity().findViewById(R.id.buttonSave);
        textName = (EditText) getActivity().findViewById(R.id.name);
        textWork = (EditText) getActivity().findViewById(R.id.work);
        textAbout = (EditText) getActivity().findViewById(R.id.about);
        textPassword = (EditText) getActivity().findViewById(R.id.password);
        textNewPassword = (EditText) getActivity().findViewById(R.id.newPassword);
        textConfirmPassword = (EditText) getActivity().findViewById(R.id.confirmPassword);

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

        retrieveUser();
    }

    /**
     * Retrieving user account data
     */
    private void retrieveUser(){
        if (connectionDetector.isNetworkAvailable()) {
            RetrieveSettingHandler scheduleTask = new RetrieveSettingHandler();
            scheduleTask.execute();
        } else {
            Toast.makeText(getContext(), getString(R.string.disconnect), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Async task to retrieve user by id and token from database
     * this method will passing JSON object as [status] and [user]
     */
    private class RetrieveSettingHandler extends AsyncTask<Object, Void, JSONObject> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingScreen.setVisibility(View.VISIBLE);
            loadingAnimation.start();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {
            JSONObject jsonResponse = null;

            try {
                URL scheduleUrl = new URL(Constant.URL_ACCOUNT);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter(KEY_ID, session.getUserDetails().get(SessionManager.KEY_ID));

                String query = builder.build().getEncodedQuery();

                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                connection.connect();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);

                    String responseData = new String(charArray);
                    jsonResponse = new JSONObject(responseData);
                } else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            } catch (MalformedURLException e) {
                Log.e(TAG, "Exception caught: " + e);
            } catch (JSONException | IOException e) {
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

    /**
     * Populating data from database to input text
     * check if data null or not
     * catch exception if json cannot parsed
     */
    public void populateSettingResponse() {
        if (settingData == null) {
            alert.showAlertDialog(getContext(), getString(R.string.error_title), getString(R.string.error_message));
        } else {
            try {
                if (settingData.getString(DATA_STATUS).equals(Constant.STATUS_SUCCESS)) {
                    JSONObject setting = new JSONObject(settingData.getString(DATA_USER));
                    textName.setText(setting.getString(KEY_NAME));
                    textWork.setText(setting.getString(KEY_WORK));
                    textAbout.setText(setting.getString(KEY_ABOUT));
                } else {
                    alert.showAlertDialog(getContext(), getString(R.string.restrict_title), getString(R.string.restrict_message));
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    /**
     * form validation
     * check length and required data
     *
     * @return boolean
     */
    private boolean isValidated() {
        boolean checkName = true;
        boolean checkWork = true;
        boolean checkAbout = true;
        boolean checkPassword = true;
        boolean checkConfirmPassword = true;

        name = textName.getText().toString();
        if (name.trim().isEmpty()) {
            infoName.setText(getString(R.string.validation_name_empty));
            infoName.setVisibility(View.VISIBLE);
            checkName = false;
        } else if (name.length() > 100) {
            infoName.setText(getString(R.string.validation_name_maxlength));
            infoName.setVisibility(View.VISIBLE);
            checkName = false;
        }

        work = textWork.getText().toString();
        if (work.trim().isEmpty()) {
            infoWork.setText(getString(R.string.validation_work_empty));
            infoWork.setVisibility(View.VISIBLE);
            checkWork = false;
        } else if (name.length() > 100) {
            infoWork.setText(getString(R.string.validation_work_maxlength));
            infoWork.setVisibility(View.VISIBLE);
            checkWork = false;
        }

        about = textAbout.getText().toString();
        if (about.trim().isEmpty()) {
            infoAbout.setText(getString(R.string.validation_about_empty));
            infoAbout.setVisibility(View.VISIBLE);
            checkAbout = false;
        } else if (about.length() > 300) {
            infoAbout.setText(getString(R.string.validation_about_maxlength));
            infoAbout.setVisibility(View.VISIBLE);
            checkAbout = false;
        }

        password = textPassword.getText().toString();
        if (password.trim().isEmpty()) {
            infoPassword.setText(getString(R.string.validation_password_empty));
            infoPassword.setVisibility(View.VISIBLE);
            checkPassword = false;
        }

        newPassword = textNewPassword.getText().toString();
        confirmPassword = textConfirmPassword.getText().toString();
        if (!newPassword.trim().equals(confirmPassword.trim())) {
            infoConfirmPassword.setText(getString(R.string.validation_password_new_mismatch));
            infoConfirmPassword.setVisibility(View.VISIBLE);
            checkConfirmPassword = false;
        }

        return checkName && checkWork && checkAbout && checkPassword && checkConfirmPassword;
    }

    /**
     * Listener for button save
     * check connection, reset validation info and revalidate
     * passing params to Async task
     */
    private class SaveSettingHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (connectionDetector.isNetworkAvailable()) {
                infoName.setVisibility(View.GONE);
                infoWork.setVisibility(View.GONE);
                infoAbout.setVisibility(View.GONE);
                infoPassword.setVisibility(View.GONE);
                infoConfirmPassword.setVisibility(View.GONE);
                if (isValidated()) {
                    new UpdateSettingHandler().execute(name, work, about, password, newPassword);
                } else {
                    alert.showAlertDialog(getContext(), getString(R.string.validation), getString(R.string.validation_message));
                }
            } else {
                Toast.makeText(getContext(), getString(R.string.disconnect), Toast.LENGTH_LONG).show();
            }
        }
    }


    /**
     * Async task to update user by id and token to database
     * this method will receive status transaction from REST API
     */
    private class UpdateSettingHandler extends AsyncTask<String, String, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getString(R.string.loading_setting_update));
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            JSONObject jsonResponse = null;

            try {
                URL accountUrl = new URL(Constant.URL_ACCOUNT_UPDATE);
                HttpURLConnection connection = (HttpURLConnection) accountUrl.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_NAME, params[0])
                        .appendQueryParameter(KEY_WORK, params[1])
                        .appendQueryParameter(KEY_ABOUT, params[2])
                        .appendQueryParameter(KEY_PASSWORD, params[3])
                        .appendQueryParameter(KEY_PASSWORD_NEW, params[4])
                        .appendQueryParameter(KEY_ID, session.getUserDetails().get(SessionManager.KEY_ID))
                        .appendQueryParameter(KEY_USERNAME, session.getUserDetails().get(SessionManager.KEY_USERNAME))
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

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = connection.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader rd = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = rd.readLine()) != null) {
                        sb.append(line);
                    }
                    String responseData = sb.toString();
                    jsonResponse = new JSONObject(responseData);
                } else {
                    Log.i(TAG, "Unsuccessful HTTP Response Code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception caught: " + e);
            }

            return jsonResponse;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            settingData = result;
            progress.dismiss();
            handleSaveSettingResponse();
        }
    }

    /**
     * Handle transaction status from database after update the setting
     * [RESTRICT] the token had sent is not available on server
     * [MISMATCH] the current password is mismatch with database which related with account
     * [SUCCESS] the request update and transaction has been completing successfully
     * [FAILED] something is getting wrong on server or nothing data changes sent
     */
    public void handleSaveSettingResponse() {
        if (settingData == null) {
            alert.showAlertDialog(getContext(), getString(R.string.error_title), getString(R.string.error_message));
        } else {
            try {
                textPassword.setText("");
                String status = settingData.getString(DATA_STATUS);
                switch (status) {
                    case Constant.STATUS_RESTRICT:
                        alert.showAlertDialog(getContext(), getString(R.string.restrict_title), getString(R.string.restrict_message));
                        break;
                    case Constant.STATUS_MISMATCH:
                        infoPassword.setText(getString(R.string.validation_password_mismatch));
                        infoPassword.setVisibility(View.VISIBLE);
                        alert.showAlertDialog(getContext(), getString(R.string.action_failed), getString(R.string.message_setting_password_mismatch));
                        break;
                    case Constant.STATUS_SUCCESS:
                        alert.showAlertDialog(getContext(), getString(R.string.action_updated), getString(R.string.message_setting_update_success));
                        break;
                    case Constant.STATUS_FAILED:
                        alert.showAlertDialog(getContext(), getString(R.string.action_failed), getString(R.string.message_setting_update_failed));
                        break;
                }
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
