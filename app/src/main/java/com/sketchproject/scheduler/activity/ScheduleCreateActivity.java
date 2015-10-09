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
public class ScheduleCreateActivity extends Activity {
    private Button buttonSave;
    private EditText textEvent;
    private EditText textDate;
    private EditText textTime;
    private EditText textLocation;
    private EditText textDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_add);

        buttonSave = (Button) findViewById(R.id.buttonSave);
        textEvent = (EditText) findViewById(R.id.event);
        textDate = (EditText) findViewById(R.id.date);
        textTime = (EditText) findViewById(R.id.time);
        textLocation = (EditText) findViewById(R.id.location);
        textDescription = (EditText) findViewById(R.id.description);

        buttonSave.setOnClickListener(new SaveSchedule());
    }

    private class SaveSchedule implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}
