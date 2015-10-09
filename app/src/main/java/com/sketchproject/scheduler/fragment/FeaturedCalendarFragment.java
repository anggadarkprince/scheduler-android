package com.sketchproject.scheduler.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sketchproject.scheduler.R;

/**
 * Created by Angga on 10/7/2015.
 */
public class FeaturedCalendarFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_featured_calendar, container, false);

        return rootView;
    }
}
