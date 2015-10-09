package com.sketchproject.scheduler.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.sketchproject.scheduler.fragment.FeaturedCalendarFragment;
import com.sketchproject.scheduler.fragment.FeaturedNoteFragment;
import com.sketchproject.scheduler.fragment.FeaturedScheduleFragment;

/**
 * Created by Angga on 10/7/2015.
 */
public class FeaturedPagerAdapter extends FragmentPagerAdapter {

    public FeaturedPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        switch (index) {
            case 0:
                return new FeaturedCalendarFragment();
            case 1:
                return new FeaturedNoteFragment();
            case 2:
                return new FeaturedScheduleFragment();
            default:
                return new FeaturedCalendarFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
