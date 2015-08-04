package com.example.android.navigationdrawerexample;


/*
    Contains the graph for readings for each day
 */

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.ScatterChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

public class TrendsFragment extends BaseFragment {

    // ## ATTRIBUTES
    //Parent Main Activity
    public MainActivity main;

    //UI Components
    private Button prevDate;
    private Button nextDate;
    private Spinner datePicker;
    private TextView datesLabel; //for telling user the current view of the graph

    //Data attributes
    private ArrayList<String> dates; //for viewing individual day readings
    private int currDateIndex;

    //Other attributes
    private ViewPager pager;

    public TrendsFragment() {
        BaseLayout = R.layout.fragment_trends;
    }

    public static TrendsFragment newInstance() {
        return new TrendsFragment();
    }

    @Override
    public void init() {

        //Bind attributes to appropriate UI components
        prevDate = (Button) findViewById(R.id.prevDate);
        nextDate = (Button) findViewById(R.id.nextDate);
        datePicker = (Spinner) findViewById(R.id.datePicker);
        datesLabel = (TextView) findViewById(R.id.datesLabel);

        //Retrieve all the dates that have recordings, and insert filler dates
        dates = getReadingsList();

        //Prepare the dropdown list that will contain all the dates
        String[] datePickerContentArr = new String[dates.size()];
        datePickerContentArr = dates.toArray(datePickerContentArr);
        ArrayAdapter<String> datePickerContent = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, datePickerContentArr
        );
        datePicker.setAdapter(datePickerContent);
        //Default the selection to the most recent date
        if (!datePickerContent.isEmpty()) {
            datePicker.setSelection(datePickerContent.getPosition(datePickerContent.getItem(datePickerContent.getCount() - 1)));
        }
        //Set listeners for each selection
        datePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                datePicker.setSelection(position); //change selected to the one clicked

                //Update the graph components depending on what was selected
                currDateIndex = position;
                updateDailyGraphComponents();
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
        updateDailyGraphComponents();

    }

    // ## HELPER FUNCTIONS

    //Updates the following components based on what date was selected:
    // - Sets listeners to the prevdate and nextdate buttons
    // - Updates the label to display the current date
    public void updateDailyGraphComponents() {
        if (dates.size() == 0 || dates.size() == 1) {
            //When no ".readings" file are on the device, disable all buttons
            // and graph nothing.
            prevDate.setEnabled(false);
            nextDate.setEnabled(false);

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
                    //Move to previous date
                    currDateIndex = currDateIndex - 1;

                    //Set spinner selected to be the one currently being displayed
                    datePicker.setSelection(currDateIndex);

                    //Update the label
                    datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                    //UpdateGraph
                    drawDailyReadingsGraph(dates.get(currDateIndex));

                    //enable/disable the navigation buttons as necessary
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

                    //Set spinner selected to be the one currently being displayed
                    datePicker.setSelection(currDateIndex);

                    //Update the label
                    datesLabel.setText(dates.get(currDateIndex).replace(".readings", ""));

                    //UpdateGraph
                    drawDailyReadingsGraph(dates.get(currDateIndex));

                    //enable/disable the navigation buttons as necessary
                    if (currDateIndex == dates.size() - 1) {
                        nextDate.setEnabled(false);
                    }
                    if (currDateIndex > 0) {
                        prevDate.setEnabled(true);
                    }
                }
            });

            //display the readings of the chosen current day
            drawDailyReadingsGraph(dates.get(currDateIndex));

            //enable/disable the navigation buttons as necessary
            prevDate.setEnabled(true);
            nextDate.setEnabled(true);
            if (currDateIndex == dates.size()-1) {
                nextDate.setEnabled(false);
            }
            if (currDateIndex == 0) {
                prevDate.setEnabled(false);
            }
        }
    }

    //Draw a line graph from a specified day / file ".summary" file
    public void drawDailyReadingsGraph(String filename) {
        //Prepare variables for graph
        ArrayList<Entry> dayReadings = new ArrayList<Entry>();
        ArrayList<String> timestamps = new ArrayList<String>();

        //Format the reading into 2dp
        DecimalFormat formatter = new DecimalFormat("#.##");

        //Process each line in the file by extracting reading and corr timestamp
        //Read the file
        Vector<String> dataRead = readFile(filename);
        for (int i = 0; i < dataRead.size(); i++) {
            String[] processedLine = dataRead.get(i).split(",");
            Double reading = Double.parseDouble(processedLine[0]);
            dayReadings.add(new Entry(Float.parseFloat(formatter.format(reading)),i));
            timestamps.add(processedLine[1]);
        }

        //Finalise the graph data
        LineDataSet graphData = new LineDataSet(dayReadings, "");

        // Graphing
        LineChart chart = (LineChart) findViewById(R.id.chart);
        LineData data = new LineData(timestamps, graphData);
        chart.setData(data);

        graphData.setColors(new int[]{R.color.line_color}, getActivity().getApplicationContext());
        graphData.setLineWidth(3); // min = 0.2f, max = 10f*
        graphData.setCircleSize(3); // Datapoint size
        graphData.setCircleColor(getResources().getColor(R.color.line_color));
        graphData.setValueTextSize(13); // Datapoint text sizes
        chart.setScaleYEnabled(false); // Don't scroll in y direction
        chart.getXAxis().setDrawGridLines(false); //disable grid lines for y-axis

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
        chart.getAxisLeft().setAxisMaxValue((float) 15);
        //format the values on the y-axis
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            private DecimalFormat format = new DecimalFormat("#.##");

            @Override
            public String getFormattedValue(float value) {
                return format.format(value);
            }
        });
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
            FileInputStream fileIn = getActivity().openFileInput(fileName);
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
        ArrayList<String> allFiles = new ArrayList<String>(Arrays.asList(getActivity().getApplicationContext().fileList()));
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
        Collections.sort(dates, dateComparator);
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