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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;

//Reading Files
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ReadingsActivity extends Activity {

    //UI components
    private Button backHome;
    private Button prevDate;
    private Button nextDate;
    private ToggleButton graphHighToggle;
    private ToggleButton graphModToggle;
    private ToggleButton graphLowToggle;
    private Spinner graphViewOptions;
    private TextView datesLabel; //for telling user the current view of the graph
    private LineChart chart;

    //Data attributes
    //For Spinner
    private ArrayList<String> dates; //for viewing individual day readings
    private int currDateIndex;
    private ArrayList<ArrayList<String>> weeks; //for viewing weekly readings
    private int currWeekIndex;


    //Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //Retrieve all the dates that have recordings, and insert filler dates
        dates = getDateList();
        weeks = getWeekList();

        //Bind graph attribute to graph on UI
        //LineChart chart = (LineChart) findViewById(R.id.chart);

        //Initialise UI components and buttons
        backHome = (Button) findViewById(R.id.backHome);
        backHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ReadingsActivity.this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        //Graphing component 1 - navigation and label
        prevDate = (Button) findViewById(R.id.prevDate);
        nextDate = (Button) findViewById(R.id.nextDate);
        datesLabel = (TextView) findViewById(R.id.datesLabel);

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

                //Update the graph components depending on what was selected
                String currModeSelected = (String) parent.getItemAtPosition(position);
                updateGraphVisualisation(currModeSelected);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing when nothing selected
            }
        });

        //Bind graph UI filtering buttons
        graphHighToggle = (ToggleButton) findViewById(R.id.graphHighToggle);
        graphModToggle = (ToggleButton) findViewById(R.id.graphModToggle);
        graphLowToggle = (ToggleButton) findViewById(R.id.graphLowToggle);

        //Set default date to be current
        if (dates.size() == 0) {
            //do nothing
        } else {
            currDateIndex = 0;
        }

        //Default graph to present Daily readings
        updateGraphVisualisation("Daily");
    }


    //## HELPER FUNCTIONS

    //Refreshes the following components relating to displaying the data:
    // - Dates List
    public void updateGraphVisualisation(String mode) {
        if (mode.equals("Daily")) {
            if (dates.size() == 0 || dates.size() == 1) {
                //When no ".readings" file are on the device, disable all buttons
                // and graph nothing.
                prevDate.setEnabled(false);

                //when there is exactly 1 ".readings" file is on the device, set
                // the text label and graph the date file
                if (dates.size() == 1) {
                    //Update the label - should be defaulted to most recent day logged
                    datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));
                    //Default the graph to display the current day
                    drawDailyReadingsGraph(dates.get(currDateIndex));
                }
            } else {
                //Update the label - should be defaulted to most recent day logged
                datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                //Update the navigation buttons
                prevDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Move to previous date (ie older date, so go UP the dates array
                        currDateIndex = currDateIndex + 1;

                        //Update the label
                        datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                        //UpdateGraph
                        drawDailyReadingsGraph(dates.get(currDateIndex));

                        //Check for end limits of the date
                        if (currDateIndex == dates.size() - 1) {
                            prevDate.setEnabled(false);
                        }
                        if (currDateIndex > 0) {
                            nextDate.setEnabled(true);
                        }
                    }
                });

                nextDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Move to next date ie newer date
                        currDateIndex = currDateIndex - 1;

                        //Update the label
                        datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                        //UpdateGraph
                        drawDailyReadingsGraph(dates.get(currDateIndex));

                        //Check for end limits of the date
                        if (currDateIndex == 0) {
                            nextDate.setEnabled(false);
                        }
                        if (currDateIndex < dates.size() - 1) {
                            prevDate.setEnabled(true);
                        }
                    }
                });

                //Default the graph to display the current day
                drawDailyReadingsGraph(dates.get(currDateIndex));
            }

            //Deafault the nextdate button to be disabled, since the most recent date is being displayed
            nextDate.setEnabled(false);

            //Disable the toggle buttons near the bottom, as by default there's nothing to toggle
            // on the chart
            graphHighToggle.setEnabled(false);
            graphModToggle.setEnabled(false);
            graphLowToggle.setEnabled(false);

        } else if (mode.equals("Weekly")) {
            //ToDo: Group dates into weeks, and update the dates list.
            //Todo: Draw the graph for each week

            //Enable toggle buttons, and add onClick functions to update graph
            graphHighToggle.setEnabled(true);
            graphHighToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Enable or disable the high exposure display on the graph
                }
            });
            graphModToggle.setEnabled(true);
            graphModToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Enable or disable the moderate exposure display on the graph
                }
            });
            graphLowToggle.setEnabled(true);
            graphLowToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Enable or disable the low exposure display on the graph
                }
            });
        }
    }

    //Draw a line graph from a specified day / file
    public void drawDailyReadingsGraph(String filename) {
        //Prepare variables for graph
        ArrayList<Entry> dayReadings = new ArrayList<Entry>();
        ArrayList<String> timestamps = new ArrayList<String>();

        //Process each line in the file by extracting reading and corr timestamp
        //Read the file
        Vector<String> dataRead = readFile(filename);
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

        //If file has no readings, set description to indicate this
        if (dataRead.size() == 0) {
            chart.setDescription("No readings for this day");
        } else if (dataRead.size() > 0) {
            chart.setDescription("");
        }

        chart.animateXY(2000, 2000);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Put axis on bot
        chart.getAxisRight().setEnabled(false); //  Disable right yaxis
        chart.getLegend().setEnabled(false); // Disable legend
        chart.invalidate(); // Refresh graph
    }

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
            //File not found - do nothing
            //Log.e("ianwong.test", "Unable to read file");
        }

        return dataRead;
    }

    //Retrieve a list of all the dates, as well as filler dates
    // in DESCENDING order (ie recent dates first)
    // each string containing a ".readings" extension
    public ArrayList<String> getDateList() {
        //Retrieve all files in the internal storage, each representing 1 day

        //Get original list of files, and sort it in desc order
        ArrayList<String> dates = new ArrayList<String>(Arrays.asList(getApplicationContext().fileList()));
        //ToDo: Filter list to contain ONLY .readings files
        if (dates.size() == 0) {
            //There are no readings on the phone
            return new ArrayList<String>();
        } else if (dates.size() == 1) {
            //There is only 1 .readings file
            return dates;

        }
        int newDatesSize = dates.size();
        for (int i = 0; i < newDatesSize; i++) {
            //Filter for appropriate files
            if (!dates.get(i).contains(".readings")) {
                //file is not of '.readings' type - remove
                dates.remove(i);
                i = i - 1;
            }
            Collections.sort(dates);
            Collections.reverse(dates);
        }

        //Add in filler dates - start at the most recent date, work towards oldest date
        // and re-sort the list
        String latestDate = dates.get(0);
        String oldestDate = dates.get(dates.size() - 1);
        String theoreticalFile = new String(oldestDate);
        theoreticalFile = incrementDate(theoreticalFile) + ".readings";
        while (!theoreticalFile.equals(latestDate)) {
            //Check to see if corr date/file exists
            if (!dates.contains(theoreticalFile)) {
                dates.add(theoreticalFile);
            }
            theoreticalFile = incrementDate(theoreticalFile) + ".readings";
        }
        Collections.sort(dates);
        Collections.reverse(dates);

        return dates;
    }

    //Retrieve a collection of weeks, with each week containing a series of dates
    // with FILLER dates completed (ie dates that don't have corresponding ".readings" file)
    public ArrayList<ArrayList<String>> getWeekList() {
        //ToDo: Implement this
        return null;
    }

    //increment a date by 1
    // date is given and returned in the format: yyyy-mm-dd
    // NOTE: Does NOT take into account leap year
    public String incrementDate(String currDate) {
        String newDate = "";

        //Extract the data
        String[] processed = currDate.split("-");
        int year = Integer.parseInt(processed[0]);
        int month = Integer.parseInt(processed[1]);
        String str = processed[2].replaceAll("\\D+", "");
        int day = Integer.parseInt(str) + 1;

        //Determine total days in month
        int daysInMonth = 31;
        if ((month == 4) || (month == 6 || (month == 9) || (month == 11))) {
            daysInMonth = 30;
        } else if (month == 2) {
            //feb
            daysInMonth = 28;
        }

        //Calculate the new day with carries
        if (day > daysInMonth) {
            month = month + 1;
            day = day % daysInMonth;
        }
        if (month > 12) {
            year = year + 1;
            month = month % 12;
        }

        newDate = Integer.toString(year) + "-" + Integer.toString(month) + "-" + Integer.toString(day);
        return newDate;
    }
}
