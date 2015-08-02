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
    CircleProgressBar pb = null;

    double MinVal = 0;
    double MaxVal = 100;
    double CurVal = 0;

    int Mode = 0;
    double Mode1 = 0;
    double Mode2 = 0;

    public TextView feedbackText;
    public TextView subFeedbackText;

    //Interfacing with the Activity that contains this fragment
    //private OnFragmentInteractionListener mListener;

    @Override
    public void init() {

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

        //Bind labels
        TextView uvIndexLabel = (TextView) findViewById(R.id.uvIndexLabel);
        //uvIndexLabel.setTypeface(main.appFont);
        TextView recExposureLabel = (TextView) findViewById(R.id.recExposureLabel);
        //recExposureLabel.setTypeface(main.appFont);

        feedbackText = (TextView) findViewById(R.id.feedbackText);
        //feedbackText.setTypeface(main.appFont);
        subFeedbackText = (TextView) findViewById(R.id.subFeedbackText);
        //subFeedbackText.setTypeface(main.appFont);

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
        indicator.setFillColor(Color.rgb(51, 51, 51));
        indicator.setStrokeColor(Color.LTGRAY); // outer color
        indicator.setStrokeWidth(1 * density);
        indicator.setOnPageChangeListener(listener);
        pb = (CircleProgressBar)findViewById(R.id.progressBar);

        pb.main = this.main;

        pb.tx1 = (TextView)findViewById(R.id.text1);
        //pb.tx1.setTypeface(main.appFont);
        pb.tx2 = (TextView)findViewById(R.id.text2);
        //pb.tx2.setTypeface(main.appFont);

        pb.feedbackText = this.feedbackText;
        pb.subFeedbackText = this.subFeedbackText;

        SetMode1(); // init as View1
    }

    // ## HELPER FUNCTIONS
    public void ResetBars() {
        double CurrentPercent = 100 * (pb.getCurVal()) / (pb.getMax());
        pb.mode = Mode;
        pb.setMin(MinVal);
        pb.setMax(MaxVal);
        pb.EndAnimation();

        //Set animation as appropriate to the mode chosen
        if (pb.mode == 1) {
            //Current UV Measurement is being displayed
            pb.setCurVal((float)main.currentUVValue);
            pb.AnimateProgressTo(main.currentUVValue);
        } else if (pb.mode == 2) {
            //Current UV Exposure is being displayed
            pb.setCurVal((float)main.currentExposurePerc);
            pb.AnimateProgressTo(main.currentExposurePerc);
        }
    }

    //For Current UV Readings
    public void SetMode1() {
        MinVal = 0;
        MaxVal = 15;
        CurVal = main.currentUVValue;
        Mode = 1;
        ResetBars();
    }

    //For % Exposure (ie accumulated UV / recommended UV * 100)
    public void SetMode2() {
        MinVal = 0;
        MaxVal = 100;
        CurVal = main.currentExposurePerc;
        Mode = 2;
        ResetBars();
    }

}