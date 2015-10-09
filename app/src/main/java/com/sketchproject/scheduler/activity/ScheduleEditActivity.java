package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sketchproject.scheduler.R;

/**
 * Created by Angga on 10/7/2015.
 */
public class ScheduleEditActivity extends Activity {
    private Button buttonUpdate;
    private EditText textEvent;
    private EditText textDate;
    private EditText textTime;
    private EditText textLocation;
    private EditText textDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_edit);

        buttonUpdate = (Button) findViewById(R.id.updateSchedule);
        textEvent = (EditText) findViewById(R.id.event);
        textDate = (EditText) findViewById(R.id.date);
        textTime = (EditText) findViewById(R.id.time);
        textLocation = (EditText) findViewById(R.id.location);
        textDescription = (EditText) findViewById(R.id.description);

        buttonUpdate.setOnClickListener(new UpdateSchedule());
    }

    private class UpdateSchedule implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}
