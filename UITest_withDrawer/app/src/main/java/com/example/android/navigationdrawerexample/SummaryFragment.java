package com.example.android.navigationdrawerexample;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import java.util.Locale;

public class SummaryFragment extends BaseFragment {

    //Attributes
    public MainActivity main;

    //Constructor
    public SummaryFragment() {
        BaseLayout = R.layout.fragment_summary;
    }

    // Summary Code

    SeekBar sb = null;
    CircleProgressBar pb = null;

    double MinVal = 0;
    double MaxVal = 100;
    double CurVal = 0;

    int Mode = 0;
    double Mode1 = 0;
    double Mode2 = 0;

    public TextView tvTopLeft;
    public TextView tvTopRight;
    public TextView tvBotLeft;
    public TextView tvBotRight;

    //Interfacing with the Activity that contains this fragment
    //private OnFragmentInteractionListener mListener;

    @Override
    public void init() {

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

        tvTopLeft = (TextView) findViewById(R.id.TV1);
        tvTopRight = (TextView) findViewById(R.id.TV2);
        tvBotLeft = (TextView) findViewById(R.id.TV3);
        tvBotRight = (TextView) findViewById(R.id.TV4);

        ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    SetMode1();
                }
                if (position == 1) {
                    SetMode2();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        };

        pager.setOnPageChangeListener(listener);

        ViewPagerAdapter adapter = new ViewPagerAdapter();
        pager.setAdapter(adapter);

        CirclePageIndicator indicator = (CirclePageIndicator)findViewById(R.id.titles1);
        final float density = getResources().getDisplayMetrics().density;
        indicator.setViewPager(pager);
        indicator.setBackgroundColor(0x000000);
        indicator.setRadius(5 * density);

//        indicator.setPageColor(0x880000FF);
//        indicator.setFillColor(0xFF888888);
//        indicator.setStrokeColor(0xFF000000);
//        indicator.setStrokeWidth(2 * density);

        indicator.setPageColor(Color.WHITE); // inside the stroke
        indicator.setFillColor(Color.BLACK); // current
        indicator.setStrokeColor(Color.LTGRAY); // outer color
        indicator.setStrokeWidth(1 * density);
        indicator.setOnPageChangeListener(listener);


        sb = (SeekBar) findViewById(R.id.seekBar);
        pb = (CircleProgressBar)findViewById(R.id.progressBar);

        pb.main = this.main;

        pb.tx1 = (TextView)findViewById(R.id.text1);
        pb.tx2 = (TextView)findViewById(R.id.text2);

        pb.tvTopLeft = this.tvTopLeft;
        pb.tvTopRight = this.tvTopRight;
        pb.tvBotLeft = this.tvBotLeft;
        pb.tvBotRight = this.tvBotRight;

        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                // this sends a PERCENTAGE only ...
                double SendValue = MinVal + ((MaxVal - MinVal) * i / 100);

                if (b) {
                    pb.AnimateProgressTo(SendValue);
                } else {
                    pb.setCurVal(SendValue);
                }

                if (Mode == 1) {
                    Mode1 = SendValue;
                }
                if (Mode == 2) {
                    Mode2 = SendValue;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        SetMode1(); // init as View1
    }


/*
    //Detect the activity that this fragment is used in
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //mListener =
                    (SummaryFragment.this)  activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    //Detect that the activity has abandoned this fragment
    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }
*/

    // ## HELPER FUNCTIONS
    public void ResetBars() {
        double CurrentPercent = 100 * (pb.getCurVal()) / (pb.getMax());
        double NewPercent = (int)(100 * (float)CurVal/(float)MaxVal);
        sb.setProgress((int)NewPercent);
        pb.mode = Mode;
        pb.setMin(MinVal);
        pb.setMax(MaxVal);
        pb.EndAnimation();

        double NewVal = (CurrentPercent * MaxVal) / 100;
        pb.setCurVal(NewVal);
        pb.AnimateProgressTo(CurVal);
    }

    //For Current UV Readings
    public void SetMode1() {
        MinVal = 0;
        MaxVal = 15;
        CurVal = Mode1;
        Mode = 1;
        ResetBars();
    }

    //For % Exposure (ie accumulated UV / recommended UV * 100)
    public void SetMode2() {
        MinVal = 0;
        MaxVal = 100;
        CurVal = Mode2;
        Mode = 2;
        ResetBars();
    }

}