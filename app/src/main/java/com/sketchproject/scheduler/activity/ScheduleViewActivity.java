package com.sketchproject.scheduler.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.sketchproject.scheduler.R;

/**
 * Created by Angga on 10/8/2015.
 */
public class ScheduleViewActivity extends Activity {
    private Button buttonBack;
    private Button buttonEdit;
    private Button buttonDelete;
    private TextView labelEvent;
    private TextView labelDate;
    private TextView labelTime;
    private TextView labelLocation;
    private TextView labelDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_schedule_view);

        buttonBack = (Button) findViewById(R.id.buttonBack);
        buttonEdit = (Button) findViewById(R.id.buttonEdit);
        buttonDelete = (Button) findViewById(R.id.buttonDelete);
        labelEvent = (EditText) findViewById(R.id.event);
        labelDate = (EditText) findViewById(R.id.date);
        labelTime = (EditText) findViewById(R.id.time);
        labelLocation = (EditText) findViewById(R.id.location);
        labelDescription = (EditText) findViewById(R.id.description);

        buttonBack.setOnClickListener(new BackHandler());
        buttonEdit.setOnClickListener(new EditkHandler());
        buttonDelete.setOnClickListener(new DeleteHandler());
    }

    private class BackHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class EditkHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

    private class DeleteHandler implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }
}
