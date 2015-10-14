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
import java.util.Date;

/**
 * Scheduler Android App
 * Created by Angga on 10/8/2015.
 */
public class ScheduleListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<ScheduleItem> scheduleItem;
    private String lastDate;
    private String lastMonth;
    private int dataLength;
    private ArrayList<Integer> dateVisibility;
    private SimpleDateFormat simpleDateFormat;

    public ScheduleListAdapter(Context context, ArrayList<ScheduleItem> item) {
        this.context = context;
        this.scheduleItem = item;
        this.lastDate = "";
        this.lastMonth = "";

        dateVisibility = new ArrayList<>();
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

        dataLength = scheduleItem.size();

        dateVisibility.clear();

        for (int i = 0; i < scheduleItem.size(); i++){
            String date = scheduleItem.get(i).getLeftDate();
            String month = scheduleItem.get(i).getLeftMonth();
            if(lastDate.equals(date) && lastMonth.equals(month) && i > 0){
                dateVisibility.add(View.INVISIBLE);
            }
            else{
                dateVisibility.add(View.VISIBLE);
                lastDate = date;
                lastMonth = month;
            }
        }


        TextView txtId = (TextView) convertView.findViewById(R.id.listScheduleId);
        TextView txtEvent = (TextView) convertView.findViewById(R.id.listScheduleTitle);
        TextView txtDescription = (TextView) convertView.findViewById(R.id.listScheduleDescription);
        TextView txtDay = (TextView) convertView.findViewById(R.id.listScheduleDay);
        TextView txtDate = (TextView) convertView.findViewById(R.id.listScheduleDate);
        TextView txtTime = (TextView) convertView.findViewById(R.id.listScheduleTime);
        TextView txtLeftDate = (TextView) convertView.findViewById(R.id.leftDate);
        TextView txtLeftMonth = (TextView) convertView.findViewById(R.id.leftMonth);
        ImageView timelineTop = (ImageView) convertView.findViewById(R.id.timelineTop);
        ImageView timelineBottom = (ImageView) convertView.findViewById(R.id.timeline);


        if (position == dataLength - 1 && dataLength > 1) {
            timelineBottom.setVisibility(View.INVISIBLE);
        } else if (position == 0) {
            timelineTop.setVisibility(View.INVISIBLE);
        }
        else{
            timelineBottom.setVisibility(View.VISIBLE);
            timelineTop.setVisibility(View.VISIBLE);
        }

        timelineBottom.setVisibility(View.VISIBLE);
        timelineTop.setVisibility(View.VISIBLE);

        txtId.setText(String.valueOf(scheduleItem.get(position).getId()));
        txtEvent.setText(scheduleItem.get(position).getEvent());

        if (scheduleItem.get(position).getDescription().length() < 100) {
            txtDescription.setText(scheduleItem.get(position).getDescription().replaceAll("\r\n", " ") + "...");
        } else {
            txtDescription.setText(scheduleItem.get(position).getDescription().replaceAll("\r\n", " ").substring(0, 100) + "...");
        }

        txtDate.setText(scheduleItem.get(position).getDate());
        txtDay.setText(scheduleItem.get(position).getDay());
        txtTime.setText(scheduleItem.get(position).getTime().substring(0, 5));

        String date = scheduleItem.get(position).getLeftDate();
        String month = scheduleItem.get(position).getLeftMonth();

        txtLeftDate.setText(date);
        txtLeftMonth.setText(month);

        if(dateVisibility.get(position) == View.VISIBLE){
            txtLeftDate.setVisibility(View.VISIBLE);
            txtLeftMonth.setVisibility(View.VISIBLE);
        }
        else{
            txtLeftDate.setVisibility(View.INVISIBLE);
            txtLeftMonth.setVisibility(View.INVISIBLE);
        }


        Date scheduleDate;
        try {
            scheduleDate = simpleDateFormat.parse(scheduleItem.get(position).getDate()+" "+scheduleItem.get(position).getTime());
            Date currentDate = new Date();
            currentDate = simpleDateFormat.parse(simpleDateFormat.format(currentDate));

            if(scheduleDate.after(currentDate)){
                Log.e("DATE", scheduleItem.get(position).getDate()+" lebih dari hari ini");
            }
            if(scheduleDate.equals(currentDate)){
                Log.e("DATE", scheduleItem.get(position).getDate()+" hari ini");
            }
            if (scheduleDate.before(currentDate)){
                Log.e("DATE", scheduleItem.get(position).getDate()+" sebelum dari hari ini");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }


        return convertView;
    }
}
