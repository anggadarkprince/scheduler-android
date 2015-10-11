package com.sketchproject.scheduler.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.activity.ScheduleViewActivity;
import com.sketchproject.scheduler.adapter.ScheduleListAdapter;
import com.sketchproject.scheduler.library.ConnectionDetector;
import com.sketchproject.scheduler.library.SessionManager;
import com.sketchproject.scheduler.model.ScheduleItem;
import com.sketchproject.scheduler.util.AlertDialogManager;
import com.sketchproject.scheduler.util.Constant;
import com.sketchproject.scheduler.util.Parser;

import org.json.JSONArray;
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
import java.util.ArrayList;

/**
 * Scheduler Android App
 * Created by Angga on 10/10/2015.
 */
public class DashboardIncomingFragment extends Fragment {
    public static final String TAG = DashboardIncomingFragment.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_EVENT = "event";
    private final String KEY_DESCRIPTION = "description";
    private final String KEY_DATE = "date";
    private final String KEY_TIME = "time";

    protected JSONObject dashboardData;
    private JSONArray schedules;

    private ListView scheduleList;
    private ScheduleListAdapter scheduleAdapter;
    private ArrayList<ScheduleItem> scheduleItems;

    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;
    private SessionManager session;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        try {
            schedules = new JSONArray(getArguments().getString("data"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return inflater.inflate(R.layout.fragment_dashboard_incoming, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alert = new AlertDialogManager();
        connectionDetector = new ConnectionDetector(getActivity());
        session = new SessionManager(getActivity());

        scheduleList = (ListView) getActivity().findViewById(R.id.listSchedule);
        scheduleList.setDivider(null);

        scheduleItems = new ArrayList<>();
        scheduleAdapter = new ScheduleListAdapter(getActivity(), scheduleItems);

        scheduleList.setOnItemClickListener(new ListScheduleListener());

        Log.e(TAG, "UPDATE INCOMING");

        updateSchedule(schedules);

        //updateDashboard();
    }

    private void updateDashboard(){
        if (connectionDetector.isNetworkAvailable()) {
            new GetDashboardTask().execute();
        }
        else {
            Toast.makeText(getActivity(), "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private class ListScheduleListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String scheduleId = ((TextView) view.findViewById(R.id.listScheduleId)).getText().toString();
            Intent intent = new Intent(getActivity(), ScheduleViewActivity.class);
            intent.putExtra(KEY_ID, scheduleId);
            startActivityForResult(intent, 200);
        }
    }

    private class GetDashboardTask extends AsyncTask<Object, Void, JSONObject> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new ProgressDialog(getActivity());
            progress.setMessage("Retrieving incoming schedule ...");
            progress.setIndeterminate(false);
            progress.setCancelable(false);
            progress.show();
        }

        @Override
        protected JSONObject doInBackground(Object[] params) {

            JSONObject jsonResponse = null;

            try{
                URL dashboardUrl = new URL(Constant.URL_DASHBOARD);
                HttpURLConnection connection = (HttpURLConnection) dashboardUrl.openConnection();

                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("token", session.getUserDetails().get(SessionManager.KEY_TOKEN))
                        .appendQueryParameter("user_id", session.getUserDetails().get(SessionManager.KEY_ID));

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

                    responseData = responseData.replace("\\r\\n", " ");
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
            progress.dismiss();
            dashboardData = result;
            handleScheduleResponse();
        }
    }

    public void handleScheduleResponse(){
        if(dashboardData == null){
            alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
        }
        else{
            try {
                if(dashboardData.getString("status").equals("success")){
                    JSONArray schedule = dashboardData.getJSONArray("incoming");
                    updateSchedule(schedule);
                }
                else{
                    alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
                }
            }
            catch(JSONException e){
                Log.e(TAG, "Exception caught: " + e);
            }
        }
    }

    public void updateSchedule(JSONArray jsonSchedules){
        try{
            scheduleItems.clear();
            for(int i = 0; i < jsonSchedules.length(); i++){
                JSONObject schedule = jsonSchedules.getJSONObject(i);

                int id = schedule.getInt(KEY_ID);
                String event = schedule.getString(KEY_EVENT);
                String description = schedule.getString(KEY_DESCRIPTION);
                String date = Parser.formatDate(schedule.getString(KEY_DATE), "dd MM yyyy");
                String day = Parser.getFullDay(schedule.getString(KEY_DATE)) + ",";
                String time = schedule.getString(KEY_TIME);
                String leftMonth = Parser.getShortMonth(Parser.getMonthOfYear(schedule.getString(KEY_DATE)));
                String leftDate = Parser.getDateOfMonth(schedule.getString(KEY_DATE));

                scheduleItems.add(new ScheduleItem(id, event, description, date, day, time, leftMonth, leftDate));
            }
            scheduleList.setAdapter(scheduleAdapter);
        }
        catch(JSONException e){
            Log.e(TAG, "Exception caught: " + e);
        }

    }
}
