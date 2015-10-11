package com.sketchproject.scheduler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class LoginActivity extends Activity {

    public static final String TAG = LoginActivity.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_TOKEN = "token";
    private final String KEY_NAME = "name";
    private final String KEY_WORK = "work";
    private final String KEY_ABOUT = "about";
    private final String KEY_USERNAME = "username";
    private final String KEY_PASSWORD = "password";

    private final String DATA_STATUS = "status";

    protected JSONObject loginData;

    private TextView loginMessage;
    private EditText textUsername;
    private EditText textPassword;
    private Button buttonLogin;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    private SessionManager session;
    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = new SessionManager(LoginActivity.this);

        if (session.isLoggedIn()) {
            Intent mainScreen = new Intent(getApplicationContext(), ApplicationActivity.class);
            mainScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainScreen);
            finish();
        }

        setContentView(R.layout.activity_login);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(getApplicationContext());

        loginMessage = (TextView) findViewById(R.id.loginMessage);
        buttonLogin = (Button) findViewById(R.id.signin);
        loadingScreen = (LinearLayout) findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) findViewById(R.id.loadingIcon);
        textUsername = (EditText) findViewById(R.id.username);
        textPassword = (EditText) findViewById(R.id.password);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connectionDetector.isNetworkAvailable()) {
                    String username = textUsername.getText().toString();
                    String password = textPassword.getText().toString();

                    if (username.trim().isEmpty() || password.trim().isEmpty()) {
                        loginMessage.setVisibility(View.VISIBLE);
                        loginMessage.setText(getString(R.string.validation_message));
                        alert.showAlertDialog(LoginActivity.this, getString(R.string.login_failed_title), getString(R.string.validation_login_empty));
                    } else {
                        loginMessage.setVisibility(View.GONE);
                        new LoginChecker().execute(username, password);
                    }
                } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.disconnect), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    /**
     * Handle transaction login
     * save user data in session and call featured activity
     */
    public void handleLoginResponse() {
        if (loginData == null) {
            alert.showAlertDialog(LoginActivity.this, getString(R.string.error_title), getString(R.string.error_message));
        } else {
            try {
                if (loginData.getString(DATA_STATUS).equals(Constant.STATUS_GRANTED)) {
                    session.createLoginSession(
                            loginData.getString(KEY_ID),
                            loginData.getString(KEY_NAME),
                            loginData.getString(KEY_WORK),
                            loginData.getString(KEY_ABOUT),
                            loginData.getString(KEY_USERNAME),
                            loginData.getString(KEY_TOKEN));

                    Intent featuredScreen = new Intent(getBaseContext(), FeaturedActivity.class);
                    featuredScreen.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(featuredScreen);
                    finish();
                } else {
                    alert.showAlertDialog(LoginActivity.this, getString(R.string.login_failed_title), getString(R.string.login_failed_message));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception caught: " + e);
            }
        }
    }

    /**
     * Async task for check credential on server
     */
    private class LoginChecker extends AsyncTask<String, String, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingScreen.setVisibility(View.VISIBLE);
            loadingAnimation.start();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            JSONObject jsonResponse = null;

            try {
                URL loginUrl = new URL(Constant.URL_LOGIN);
                HttpURLConnection connection = (HttpURLConnection) loginUrl.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_USERNAME, args[0])
                        .appendQueryParameter(KEY_PASSWORD, args[1]);

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
                    Log.e(TAG, responseData);
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
            loginData = result;
            loadingScreen.setVisibility(View.GONE);
            loadingAnimation.stop();
            handleLoginResponse();
        }
    }

}
