package com.example.android.navigationdrawerexample;


/*
    Contains the fragments that contain "Daily Views" and "Weekly Views"
 */

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

public class TrendsFragment extends BaseFragment {

    public TrendsFragment() {
        BaseLayout = R.layout.fragment_trends;
    }

    private ScatterChart mChart;

    public static TrendsFragment newInstance() {
        return new TrendsFragment();
    }

    private ViewPager pager;

    @Override
    public void init() {
        pager = (ViewPager) findViewById(R.id.viewPager);

    }

    // ## HELPER FUNCTIONS


}