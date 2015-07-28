package ianwong.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

//UI Components
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.Spinner;

//Charting
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

//Data Representation
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

//Reading Files
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadingsActivity extends Activity {

    //UI components
    private Button backHome;
    private Button prevWeek;
    private Button nextWeek;
    private ToggleButton graphHighToggle;
    private ToggleButton graphModToggle;
    private ToggleButton graphLowToggle;
    private Spinner graphViewOptions;
    private TextView modeName; //for telling user the current view of the graph
    private LineChart chart;

    //Data attributes
    //For Spinner
    private ArrayList<String> dates;

    //Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //Retrieve all the dates that have recordings, and insert filler dates
        dates = new ArrayList<String>();

        //Initialise UI components and buttons
        backHome = (Button) findViewById(R.id.backHome);
        backHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ReadingsActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        modeName = (TextView) findViewById(R.id.modeName);

        //Initialise Graph Viewing Options with following modes for visualisation:
        // - Daily, Weekly
        graphViewOptions = (Spinner) findViewById(R.id.graphViewOption);
        String[] viewModesList = {"Daily", "Weekly"};
        ArrayAdapter<String> viewModes = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, viewModesList
        );
        graphViewOptions.setAdapter(viewModes);
        //Default the selection to "Daily"
        graphViewOptions.setSelection(viewModes.getPosition("Daily"));
        //Set listeners for each selection
        graphViewOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                graphViewOptions.setSelection(position); //change selected to the one clicked

                //ToDo: Make graph display what is selected
                //Change the graph data according to selection
                String currModeSelected = (String) parent.getItemAtPosition(position);
                if (currModeSelected.equals("Daily")) {
                    //Daily presentation

                    //TODO: Currently only graphs the most recent day

                    //Prepare variables for graph
                    ArrayList<Entry> dayReadings = new ArrayList<Entry>();
                    ArrayList<String> timestamps = new ArrayList<String>();

                    //Process each line in the file by extracting reading and corr timestamp
                    //Read the file
                    Vector<String> dataRead = readFile(getDateList().get(getDateList().size()-1));
                    for (int i = 0; i < dataRead.size(); i++) {
                        String[] processedLine = dataRead.get(i).split(",");
                        dayReadings.add(new Entry(Float.parseFloat(processedLine[0]),i));
                        timestamps.add(processedLine[1]);
                    }

                    //Finalise the graph data
                    LineDataSet graphData = new LineDataSet(dayReadings, "");

                    // Graphing
                    LineChart chart = (LineChart) findViewById(R.id.chart);
                    LineData data = new LineData(timestamps, graphData);
                    chart.setData(data);

                    graphData.setColors(new int[]{R.color.line_color}, ReadingsActivity.this);
                    graphData.setLineWidth(3); // min = 0.2f, max = 10f*
                    graphData.setCircleSize(3); // Datapoint size
                    graphData.setCircleColor(getResources().getColor(R.color.line_color));
                    graphData.setValueTextSize(13); // Datapoint text sizes
                    chart.setScaleYEnabled(false); // Don't scroll in y direction
//                chart.setDrawGridBackground(false);
                    chart.setDescription(""); // Descrip in bot right
                    chart.animateXY(2000, 2000);
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Put axis on bot
                    chart.getAxisRight().setEnabled(false); //  Disable right yaxis
                    chart.getLegend().setEnabled(false); // Disable legend
                    chart.invalidate(); // Refresh graph

                } else if (currModeSelected.equals("Weekly")) {
                    //Weekly presentation
                    updateDatesList("Weekly");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
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


    //## HELPER FUNCTIONS

    //Read from a given file and return read data as a vector of strings
    // (1 file line = 1 vector)
    public Vector<String> readFile(String fileName) {
        Vector<String> dataRead = new Vector<String>();

        //Open and read file
        try {

            //File is read using InputStreamReader. Buffered reader stores bytes read by
            // InputStreamReader into the buffer -> more efficient
            FileInputStream fileIn = this.openFileInput(fileName);
            InputStreamReader fileReader = new InputStreamReader(fileIn);
            BufferedReader reader = new BufferedReader(fileReader);

            String currLine = reader.readLine();
            while (currLine != null) {
                dataRead.add(currLine);
                currLine = reader.readLine();
            }
        } catch (IOException e) {
            Log.e("ianwong.test", "Unable to read file");
        }

        return dataRead;
    }

    //Updates the list of dates to display based on what mode the user has chosen to view
    public void updateDatesList(String modeChosen) {

    }

    //Retrieve a list of all the dates, as well as filler dates
    public ArrayList<String> getDateList() {
        //Retrieve all files in the internal storage, each representing 1 day
        ArrayList<String> dates = new ArrayList<String>(Arrays.asList(getApplicationContext().fileList()));

        return dates;
    }

    //Incremement a given date by a given number of days/months/years
    public String incrementDate(String currDate, int days, int months, int years) {
        String newDate = "";

        return newDate;
    }
}
