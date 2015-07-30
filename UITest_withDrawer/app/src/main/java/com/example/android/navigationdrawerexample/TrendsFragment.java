package com.example.android.navigationdrawerexample;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

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

    @Override
    public void init() {

    }

}