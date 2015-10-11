package com.sketchproject.scheduler;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.sketchproject.scheduler.adapter.FeaturedPagerAdapter;

/**
 * Scheduler Android App
 * Created by Angga on 10/7/2015.
 */
public class FeaturedActivity extends FragmentActivity {
    private ViewPager viewPager;
    private FeaturedPagerAdapter pagerAdapter;
    private LinearLayout layoutTap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_featured);

        pagerAdapter = new FeaturedPagerAdapter(getSupportFragmentManager());

        layoutTap = (LinearLayout) findViewById(R.id.labelTap);
        viewPager = (ViewPager) findViewById(R.id.featuredPager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                changePage(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int position) {
            }
        });
    }


    /**
     * @param position index position of pager view
     */
    private void changePage(int position){
        ImageView view;

        view = (ImageView) findViewById(R.id.page1);
        view.setImageResource(R.drawable.circle_featured_normal);

        view = (ImageView) findViewById(R.id.page2);
        view.setImageResource(R.drawable.circle_featured_normal);

        view = (ImageView) findViewById(R.id.page3);
        view.setImageResource(R.drawable.circle_featured_normal);

        layoutTap.setVisibility(View.GONE);

        switch(position){
            case 0:
                view = (ImageView) findViewById(R.id.page1);
                view.setImageResource(R.drawable.circle_featured_active);
                break;
            case 1:
                view = (ImageView) findViewById(R.id.page2);
                view.setImageResource(R.drawable.circle_featured_active);
                break;
            case 2:
                layoutTap.setVisibility(View.VISIBLE);
                view = (ImageView) findViewById(R.id.page3);
                view.setImageResource(R.drawable.circle_featured_active);
                break;
        }
    }
}
