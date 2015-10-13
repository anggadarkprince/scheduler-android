package com.sketchproject.scheduler.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.activity.ScheduleCreateActivity;
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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class ScreenIncoming extends Fragment {

    public static final String TAG = ScreenIncoming.class.getSimpleName();

    private final String KEY_USER_ID = "user_id";
    private final String KEY_TOKEN = "token";
    private final String KEY_ID = "id";
    private final String KEY_EVENT = "event";
    private final String KEY_DESCRIPTION = "description";
    private final String KEY_DATE = "date";
    private final String KEY_TIME = "time";

    private final String DATA_STATUS = "status";
    private final String DATA_SCHEDULE = "incoming";

    private JSONObject scheduleData;

    private TextView emptyMessage;
    private ListView scheduleList;
    private ScheduleListAdapter scheduleAdapter;
    private ArrayList<ScheduleItem> scheduleItems;

    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;
    private SessionManager session;

    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu_incoming, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alert = new AlertDialogManager();
        connectionDetector = new ConnectionDetector(getActivity());
        session = new SessionManager(getActivity());

        loadingScreen = (LinearLayout) getActivity().findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) getActivity().findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        emptyMessage = (TextView) getActivity().findViewById(R.id.emptyMessage);
        scheduleList = (ListView) getActivity().findViewById(R.id.listSchedule);
        scheduleList.setDivider(null);

        scheduleItems = new ArrayList<>();
        scheduleAdapter = new ScheduleListAdapter(getActivity(), scheduleItems);

        scheduleList.setOnItemClickListener(new ListScheduleListener());

        updateScheduleList();
    }

    /**
     * Update schedule list from database
     */
    public void updateScheduleList() {
        if (connectionDetector.isNetworkAvailable()) {
            RetrieveScheduleHandler scheduleTask = new RetrieveScheduleHandler();
            scheduleTask.execute();
        } else {
            Toast.makeText(getActivity(), getString(R.string.disconnect), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Listener each list item to call detail activity and show complete schedule by id
     */
    private class ListScheduleListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String scheduleId = ((TextView) view.findViewById(R.id.listScheduleId)).getText().toString();
            Intent intent = new Intent(getActivity(), ScheduleViewActivity.class);
            intent.putExtra(KEY_ID, scheduleId);
            startActivityForResult(intent, 200);
        }
    }

    /**
     * Listener to call form new schedule
     */
    private class CreateScheduleListener implements View.OnClickListener {
        public void onClick(View v) {
            Intent newSchedule = new Intent(getActivity(), ScheduleCreateActivity.class);
            startActivityForResult(newSchedule, 200);
        }
    }

    /**
     * receive signal for result when call new schedule activity has closed
     * so schedule list will be update on the fly
     *
     * @param requestCode identifier request from this activity
     * @param resultCode  result type from called activity
     * @param data        return data from called activity
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_CANCELED) {
                updateScheduleList();
            }
        }
    }


    /**
     * Async task to retrieve schedule related by logged in user
     * this method will passing JSON object as [status] and [schedules]
     */
    private class RetrieveScheduleHandler extends AsyncTask<Object, Void, JSONObject> {

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
                URL scheduleUrl = new URL(Constant.URL_SCHEDULE_INCOMING);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

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
                    Reader reader = new InputStreamReader(inputStream);
                    int contentLength = connection.getContentLength();
                    char[] charArray = new char[contentLength];
                    reader.read(charArray);

                    String responseData = new String(charArray);
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
            loadingScreen.setVisibility(View.GONE);
            loadingAnimation.stop();
            scheduleData = result;
            handleScheduleResponse();
        }

    }

    /**
     * Handle result schedule from database
     * loop through and populate JSON array to list adapter
     */
    public void handleScheduleResponse() {
        if (scheduleData == null) {
            alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
        } else {
            try {
                if (scheduleData.getString(DATA_STATUS).equals(Constant.STATUS_SUCCESS)) {
                    JSONArray jsonSchedules = scheduleData.getJSONArray(DATA_SCHEDULE);

                    if(jsonSchedules.length() == 0){
                        emptyMessage.setVisibility(View.VISIBLE);
                    }
                    else{
                        emptyMessage.setVisibility(View.GONE);
                    }

                    scheduleItems.clear();
                    for (int i = 0; i < jsonSchedules.length(); i++) {
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
                } else {
                    alert.showAlertDialog(getActivity(), getString(R.string.error_title), getString(R.string.error_message));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Exception caught: " + e);
            }
        }
    }
}
