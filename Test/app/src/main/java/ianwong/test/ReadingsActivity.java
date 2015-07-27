package ianwong.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

//UI Components
import android.widget.Button;
import android.widget.ToggleButton;

//Data representations
import java.util.ArrayList;

public class ReadingsActivity extends Activity {

    //UI components
    private Button backHome;
    private Button prevWeek;
    private Button nextWeek;
    private ToggleButton graphHighToggle;
    private ToggleButton graphModToggle;
    private ToggleButton graphLowToggle;
    //private BarChart chart;

    //Data attributes
    private ArrayList<String> currWeekDates;

    //Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //ToDo: Make current week the most recent set of logs
        currWeekDates = new ArrayList<String>();

        //Initialise UI components and buttons
        backHome = (Button) findViewById(R.id.backHome);
        backHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ReadingsActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        //Graph Weekly Navigation buttons
        prevWeek = (Button) findViewById(R.id.prevWeek);
        prevWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Make graph display the previous week's data
            }
        });

        nextWeek = (Button) findViewById(R.id.nextWeek);
        nextWeek.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Make graph display the next week's data
            }
        });

        //Graph UI filtering buttons
        graphHighToggle = (ToggleButton) findViewById(R.id.graphHighToggle);
        graphHighToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Send data to chart to display high values
            }
        });

        graphModToggle = (ToggleButton) findViewById(R.id.graphModToggle);
        graphModToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Send data to chart to display moderate values
            }
        });

        graphLowToggle = (ToggleButton) findViewById(R.id.graphLowToggle);
        graphLowToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Send data to chart to display low/safe values
            }
        });

    }


}
