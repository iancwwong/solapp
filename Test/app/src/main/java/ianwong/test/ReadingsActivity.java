package ianwong.test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
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
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

//Data Representation
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    private Spinner graphViewOptions;
    private TextView datesLabel; //for telling user the current view of the graph

    //Data attributes
    //For Spinner
    private ArrayList<String> dates; //for viewing individual day readings
    private int currDateIndex;
    private ArrayList<ArrayList<String>> weeks; //for viewing weekly readings
    private int currWeekIndex;

    //Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    //Fragments
    ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //Retrieve all the dates that have recordings, and insert filler dates
        dates = getReadingsList();
        weeks = groupIntoWeeks(getReadingsList());

        for (int i = 0; i < weeks.size(); i++) {
            Log.e("test", "Week " + i);
            for (String date : weeks.get(i)) {
                Log.e("test",date);
            }
        }

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

        //Set default date to be current
        if (dates.size() == 0) {
            //do nothing
        } else {
            currDateIndex = dates.size()-1;
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
                        currDateIndex = currDateIndex - 1;

                        //Update the label
                        datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                        //UpdateGraph
                        drawDailyReadingsGraph(dates.get(currDateIndex));

                        //Check for end limits of the date
                        if (currDateIndex == 0) {
                            prevDate.setEnabled(false);
                        }
                        if (currDateIndex < dates.size()-1) {
                            nextDate.setEnabled(true);
                        }
                    }
                });

                nextDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Move to next date ie newer date
                        currDateIndex = currDateIndex + 1;

                        //Update the label
                        datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                        //UpdateGraph
                        drawDailyReadingsGraph(dates.get(currDateIndex));

                        //Check for end limits of the date
                        if (currDateIndex == dates.size()-1) {
                            nextDate.setEnabled(false);
                        }
                        if (currDateIndex > 0) {
                            prevDate.setEnabled(true);
                        }
                    }
                });

                //Default the graph to display the current day
                drawDailyReadingsGraph(dates.get(currDateIndex));
            }

            //Deafault the nextdate button to be disabled, since the most recent date is being displayed
            nextDate.setEnabled(false);

        } else if (mode.equals("Weekly")) {
            //ToDo: Group dates into weeks, and update the dates list.
            //Todo: Draw the graph for each week

        }
    }

    //Draw a line graph from a specified day / file ".summary" file
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

    //Draw a bar chart, given a week of ".summary" files
    //NOTE: this list MUST be in ascending order
    public void drawWeeklyGraph(ArrayList<String> summaryFiles) {

        //Prepare x-axis by extracting the dates from the summaryFiles names
        ArrayList<String> dates = new ArrayList<String>();
        for (String string : summaryFiles) {
            dates.add(new String(string.replace(".summary","")));
        }

        //Prepare the data set for the chart
        String dataSetName = "Exposures for the week";
        int i = 0;
        ArrayList<BarEntry> entries = new ArrayList<BarEntry>();
        for (String file : summaryFiles) {
            Vector<String> readData = readFile(file);

            //Calculate the total exposure
            Double totalExposure = Double.parseDouble("0");
            if (readData.size() > 0) {
                //file is not empty - update total exposure
                for (String line : readData) {
                    String[] processed = line.split(",");
                    totalExposure = totalExposure + Double.parseDouble(processed[1]);
                }
            }
            Double percExposure = totalExposure / RECOMMENDED_EXPOSURE;
            entries.add(new BarEntry(Float.parseFloat(Double.toString(percExposure)),i)); //something was weird casting a double to float
            i = i + 1;
        }
        BarDataSet dataSet = new BarDataSet(entries,dataSetName);

        //Prepare Bar Data - dates as x-values, data-set as individual
        BarData barData = new BarData(dates,dataSet);

        //Draw and customise Graph
        BarChart chart = (BarChart) findViewById(R.id.chart);
        chart.setData(barData);

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

    //Retrieve a list of all files with extension of type ".readings"
    // Returns the list in ASCENDING order
    public ArrayList<String> getReadingsList() {

        //Used to compare dates in string form
        Comparator dateComparator = new Comparator<String>() {
            public int compare(String date1, String date2) {

                //strip extension
                String date1Str = date1.replace(".readings","");
                String processed[] = date1Str.split("-");
                String year = processed[0];
                String month = String.format("%02d",Integer.parseInt(processed[1]));
                String day = String.format("%02d",Integer.parseInt(processed[2]));
                String date1FinalStr = year + month + day;
                long date1Value = Long.parseLong(date1FinalStr, 10); //to base 10

                String date2Str = date2.replace(".readings","");
                String processed2[] = date2Str.split("-");
                String year2 = processed2[0];
                String month2 = String.format("%02d",Integer.parseInt(processed2[1]));
                String day2 = String.format("%02d", Integer.parseInt(processed2[2]));
                String date2FinalStr = year2 + month2 + day2;
                long date2Value = Long.parseLong(date2FinalStr,10); //to base 10

                //Determine whether one is greater than the other
                if (date1Value > date2Value) {
                    return 1;
                } else if (date1Value == date2Value) {
                    return 0;
                }
                return -1;
            }
        };

        //Retrieve all files in the internal storage, each representing 1 day

        //Get original list of files, and sort it in desc order
        ArrayList<String> allFiles = new ArrayList<String>(Arrays.asList(getApplicationContext().fileList()));
        if (allFiles.size() == 0) {
            //There are no readings on the phone
            return new ArrayList<String>();
        }

        //Filter out ONLY ".readings" files
        ArrayList<String> dates = new ArrayList<String>();
        for (String filename : allFiles) {
            if (filename.contains(".readings")) {
                dates.add(filename);
            }
        }
        Collections.sort(dates,dateComparator);
        Collections.reverse(dates);

        if (dates.size() == 1) {
            //There is only 1 ".readings" file - immediately return
            return dates;
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

        //Sort everything in order of date
        Collections.sort(dates, dateComparator);

        return dates;
    }

    //Group a specified list of dates into weeks, with each week containing 7 dates
    // with FILLER dates completed (ie dates that don't have corresponding ".readings" file)
    // NOTE: Each string will represent a ".summary" file
    public ArrayList<ArrayList<String>> groupIntoWeeks(ArrayList<String> dates) {
        ArrayList<ArrayList<String>> weeks = new ArrayList<ArrayList<String>>();

        //Add collections of 7 days in "dates" to "weeks"
        int counter = 0;
        ArrayList<String> aWeek = new ArrayList<String>();
        for (String date : dates) {
            String currDateSummary = date.replace(".readings",".summary");
            if (counter == 7) {
                counter = 0;
                weeks.add(aWeek);
                aWeek = new ArrayList<String>(); //empty out aWeeks
            }
            aWeek.add(currDateSummary);
            counter = counter + 1;
        }
        //Add the remaining "aWeek" to weeks
        weeks.add(aWeek);
        return weeks;
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
