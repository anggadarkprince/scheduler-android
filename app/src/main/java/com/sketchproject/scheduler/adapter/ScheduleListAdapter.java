package com.sketchproject.scheduler.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.model.ScheduleItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by Angga on 10/8/2015.
 */
public class ScheduleListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ScheduleItem> scheduleItem;

    public ScheduleListAdapter(Context context, ArrayList<ScheduleItem> item){
        this.context = context;
        this.scheduleItem = item;
    }

    @Override
    public int getCount() {
        return scheduleItem.size();
    }

    @Override
    public Object getItem(int position) {
        return scheduleItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_schedule_item, null);
        }

        TextView txtId = (TextView) convertView.findViewById(R.id.listScheduleId);
        TextView txtEvent = (TextView) convertView.findViewById(R.id.listScheduleTitle);
        TextView txtDescription = (TextView) convertView.findViewById(R.id.listScheduleDescription);
        TextView txtDay = (TextView) convertView.findViewById(R.id.listScheduleDay);
        TextView txtDate = (TextView) convertView.findViewById(R.id.listScheduleDate);
        TextView txtLeftDate = (TextView) convertView.findViewById(R.id.leftDate);
        TextView txtLeftMonth = (TextView) convertView.findViewById(R.id.leftMonth);
        ImageView timelineTop = (ImageView) convertView.findViewById(R.id.timelineTop);
        ImageView timelineBottom = (ImageView) convertView.findViewById(R.id.timeline);

        if(position == getCount() - 1){
            timelineBottom.setVisibility(View.INVISIBLE);
        }
        else if(position == 0){
            timelineTop.setVisibility(View.INVISIBLE);
        }

        Log.i("list", scheduleItem.get(position).getDescription());

        txtId.setText(String.valueOf(scheduleItem.get(position).getId()));
        txtEvent.setText(scheduleItem.get(position).getEvent());

        if(scheduleItem.get(position).getDescription().length() < 200){
            txtDescription.setText(scheduleItem.get(position).getDescription() + "...");
        }
        else{
            txtDescription.setText(scheduleItem.get(position).getDescription().substring(0, 200).toCharArray().toString() + "...");
        }

        txtDate.setText(scheduleItem.get(position).getDate());
        txtDay.setText(scheduleItem.get(position).getDay());
        txtLeftDate.setText(scheduleItem.get(position).getLeftDate());
        txtLeftMonth.setText(scheduleItem.get(position).getLeftMonth());

        return convertView;
    }
}
