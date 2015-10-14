package com.sketchproject.scheduler.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sketchproject.scheduler.R;
import com.sketchproject.scheduler.model.NoteItem;

import java.util.ArrayList;

/**
 * Scheduler Android App
 * Created by Angga on 10/8/2015.
 */
public class NoteListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<NoteItem> noteItems;

    public NoteListAdapter(Context context, ArrayList<NoteItem> item) {
        this.context = context;
        this.noteItems = item;
    }

    @Override
    public int getCount() {
        return noteItems.size();
    }

    @Override
    public Object getItem(int position) {
        return noteItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.list_note_item, null);
        }

        TextView txtId = (TextView) convertView.findViewById(R.id.listNoteId);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.listNoteTitle);
        TextView txtLabel = (TextView) convertView.findViewById(R.id.listNoteLabel);
        TextView txtNote = (TextView) convertView.findViewById(R.id.listNoteContent);

        txtId.setText(String.valueOf(noteItems.get(position).getId()));
        txtTitle.setText(noteItems.get(position).getTitle());
        txtLabel.setText(noteItems.get(position).getLabel());

        if (noteItems.get(position).getNote().length() < 200) {
            txtNote.setText(noteItems.get(position).getNote() + "...");
        } else {
            txtNote.setText(noteItems.get(position).getNote().substring(0, 200) + "...");
        }

        return convertView;
    }
}
