package com.sketchproject.scheduler.fragment;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.app.Fragment;
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
 * Created by Angga on 10/7/2015.
 */
public class ScreenSchedule extends Fragment {

    protected JSONObject scheduleData;

    public static final String TAG = ScreenSchedule.class.getSimpleName();

    private final String KEY_ID = "id";
    private final String KEY_EVENT = "event";
    private final String KEY_DESCRIPTION = "description";
    private final String KEY_DATE = "date";

    private ListView scheduleList;
    private ScheduleListAdapter scheduleAdapter;
    private ArrayList<ScheduleItem> scheduleItem;

    private AlertDialogManager alert;
    private ConnectionDetector connectionDetector;
    private SessionManager session;

    private Button createScheduleButton;
    private LinearLayout loadingScreen;
    private ImageView loadingIcon;
    private AnimationDrawable loadingAnimation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_menu_schedule, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alert = new AlertDialogManager();

        connectionDetector = new ConnectionDetector(getActivity().getApplicationContext());

        session = new SessionManager(getActivity().getApplicationContext());

        loadingScreen = (LinearLayout) getActivity().findViewById(R.id.loadingScreen);
        loadingIcon = (ImageView) getActivity().findViewById(R.id.loadingIcon);
        loadingIcon.setBackgroundResource(R.drawable.loading_animation);
        loadingAnimation = (AnimationDrawable) loadingIcon.getBackground();

        createScheduleButton = (Button) getActivity().findViewById(R.id.buttonSave);

        scheduleList = (ListView) getActivity().findViewById(R.id.listSchedule);

        scheduleItem = new ArrayList<ScheduleItem>();

        scheduleAdapter = new ScheduleListAdapter(getActivity(), scheduleItem);

        scheduleList.setOnItemClickListener(new ListScheduleListener());

        createScheduleButton.setOnClickListener(new CreateScheduleListener());

        if (connectionDetector.isNetworkAvailable()) {
            GetScheduleTask scheduleTask = new GetScheduleTask();
            scheduleTask.execute();
        }
        else {
            Toast.makeText(getActivity().getApplicationContext(), "Network is unavailable!", Toast.LENGTH_LONG).show();
        }
    }

    private class ListScheduleListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String noteId = ((TextView) view.findViewById(R.id.listScheduleId)).getText().toString();
            Intent intent = new Intent(getActivity(), ScheduleViewActivity.class);
            intent.putExtra(KEY_ID, noteId);
            startActivityForResult(intent, 100);
        }
    }

    private class CreateScheduleListener implements View.OnClickListener {
        public void onClick(View v) {
            Intent newNote = new Intent(getActivity(), ScheduleCreateActivity.class);
            startActivityForResult(newNote, 200);
        };
    }


    private class GetScheduleTask extends AsyncTask<Object, Void, JSONObject> {

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
                URL scheduleUrl = new URL(Constant.URL_SCHEDULE_VIEW);
                HttpURLConnection connection = (HttpURLConnection) scheduleUrl.openConnection();

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
            loadingScreen.setVisibility(View.GONE);
            loadingAnimation.stop();
            scheduleData = result;
            handleScheduleResponse();
        }

    }

    public void handleScheduleResponse(){

        if(scheduleData == null){
            alert.showAlertDialog(getActivity().getApplicationContext(), getString(R.string.error_title), getString(R.string.error_message), false);
        }
        else{
            try {
                if(scheduleData.getString("status").equals("success")){
                    JSONArray jsonSchedules = scheduleData.getJSONArray("schedules");

                    for(int i = 0; i < jsonSchedules.length(); i++){
                        JSONObject schedule = jsonSchedules.getJSONObject(i);

                        int id = schedule.getInt(KEY_ID);
                        String event = schedule.getString(KEY_EVENT);
                        String description = schedule.getString(KEY_DESCRIPTION);
                        String date = Parser.formatDate(schedule.getString(KEY_DATE), "dd MMMM yyyy");
                        String day = Parser.formatDate(schedule.getString(KEY_DATE), "EEE, MMM d, yy");
                        String leftMonth = Parser.getShortMonth(Integer.parseInt(Parser.formatDate(schedule.getString(KEY_DATE), "M")));
                        String leftDate = Parser.formatDate(schedule.getString(KEY_DATE), "dd");

                        scheduleItem.add(new ScheduleItem(id, event, description, date, day, leftMonth, leftDate));
                    }
                    scheduleList.setAdapter(scheduleAdapter);
                }
                else{
                    alert.showAlertDialog(getActivity().getApplicationContext(), getString(R.string.error_title), getString(R.string.error_message), false);
                }
            }
            catch(JSONException e){
                Log.e(TAG, "Exception caught: " + e);
            }
        }
    }
}
