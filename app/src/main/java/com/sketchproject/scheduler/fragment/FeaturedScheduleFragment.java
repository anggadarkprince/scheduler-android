package com.sketchproject.scheduler.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.sketchproject.scheduler.ApplicationActivity;
import com.sketchproject.scheduler.R;


/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class FeaturedScheduleFragment extends Fragment {
    RelativeLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_featured_schedule, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layout = (RelativeLayout) getActivity().findViewById(R.id.launchArea);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent mainScreen = new Intent(getActivity(), ApplicationActivity.class);
                startActivity(mainScreen);
                getActivity().finish();
            }
        });
    }
}
