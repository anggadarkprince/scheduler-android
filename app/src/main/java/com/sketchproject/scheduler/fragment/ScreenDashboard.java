package com.sketchproject.scheduler.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.activity.ScheduleCreateActivity;
import com.sketchproject.scheduler.adapter.DashboardPagerAdapter;
import com.sketchproject.scheduler.library.ConnectionDetector;
import com.sketchproject.scheduler.library.SessionManager;
import com.sketchproject.scheduler.util.AlertDialogManager;
import com.sketchproject.scheduler.util.Constant;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
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
public class ScreenDashboard extends Fragment {

    public static final String TAG = ScreenDashboard.class.getSimpleName();

    private final String KEY_USER_ID = "user_id";
    private final String KEY_TOKEN = "token";

    private final String DATA_STATUS = "status";
    private final String DATA_TOTAL_INCOMING = "total_incoming";
    private final String DATA_TOTAL_SCHEDULE = "total_schedule";
    private final String DATA_TOTAL_NOTE = "total_note";
    private final String DATA_INCOMING = "incoming";
    private final String DATA_TODAY = "today";
    private final String DATA_TOMORROW = "tomorrow";

    private JSONObject dashboardData;

    private TextView labelName;
    private TextView labelAbout;
    private TextView labelSchedule;
    private TextView labelIncoming;
    private TextView labelNote;

    private Button buttonIncoming;
    private Button buttonToday;
    private Button buttonTomorrow;

    private Button buttonCreateSchedule;

    private ViewPager viewPager;
    private DashboardPagerAdapter pagerAdapter;

    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_menu_dashboard, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alert = new AlertDialogManager();
        connectionDetector = new ConnectionDetector(getActivity());
        session = new SessionManager(getActivity());

        labelName = (TextView) getActivity().findViewById(R.id.dashboardName);
        labelAbout = (TextView) getActivity().findViewById(R.id.dashboardAbout);
        labelSchedule = (TextView) getActivity().findViewById(R.id.valueSchedule);
        labelIncoming = (TextView) getActivity().findViewById(R.id.valueIncoming);
        labelNote = (TextView) getActivity().findViewById(R.id.valueNote);

        buttonCreateSchedule = (Button) getActivity().findViewById(R.id.buttonCreate);
        buttonCreateSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newSchedule = new Intent(getActivity(), ScheduleCreateActivity.class);
                startActivityForResult(newSchedule, 200);
            }
        });

        labelName.setText(session.getUserDetails().get(SessionManager.KEY_NAME));
        labelAbout.setText(session.getUserDetails().get(SessionManager.KEY_ABOUT));

        updateDashboard();
    }

    /**
     * Update dashboard data from database
     */
    public void updateDashboard() {
        if (connectionDetector.isNetworkAvailable()) {
            new RetrieveDashboardHandler().execute();
        } else {
            Toast.makeText(getActivity(), getString(R.string.disconnect), Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Async task to retrieve dashboard related by logged in user
     * this method will passing JSON object as [status][incoming total][schedule total][note total]
     * and JSON array as [incoming][today][tomorrow]
     */
    private class RetrieveDashboardHandler extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getActivity());
            progress.setMessage(getString(R.string.loading_dashboard_retrieve));
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {

            JSONObject jsonResponse = null;

            try {
                URL dashboardUrl = new URL(Constant.URL_DASHBOARD);
                HttpURLConnection connection = (HttpURLConnection) dashboardUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter(KEY_TOKEN, session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter(KEY_USER_ID, session.getUserDetails().get(SessionManager.KEY_ID));

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

                    responseData = responseData.replace("\\r\\n", " ");
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
            progress.dismiss();
            dashboardData = result;
            handleScheduleResponse();
        }
    }

    /**
     * Handle result summary schedule from database
     * put statistic total in text view
     * distribute schedules in each page
     */
    public void handleScheduleResponse() {
        if (dashboardData == null) {
            alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
        } else {
            try {
                if (dashboardData.getString(DATA_STATUS).equals(Constant.STATUS_SUCCESS)) {
                    labelSchedule.setText(dashboardData.getString(DATA_TOTAL_SCHEDULE));
                    labelIncoming.setText(dashboardData.getString(DATA_TOTAL_INCOMING));
                    labelNote.setText(dashboardData.getString(DATA_TOTAL_NOTE));

                } else {
                    alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception caught: " + e);
            }
        }
    }

}
