package com.example.android.navigationdrawerexample;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ValueFormatter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

/*
    Contains the graph for viewing each week's percentage exposure, across each day
 */
public class WeeklyTrendsFragment extends BaseFragment{

    //UI components
    private Button prevWeek;
    private Button nextWeek;
    private Spinner weekPicker;
    private TextView weekLabel; //for telling user the current view of the graph

    //Data attributes
    //for viewing weekly readings
    private ArrayList<String> dates;
    private ArrayList<ArrayList<String>> weeks;
    private int currWeekIndex;

    //Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    public WeeklyTrendsFragment() {
        BaseLayout = R.layout.fragment_weekly_trends;
    }


    @Override
    public void init() {
        //Retrieve all the dates that have recordings, and insert filler dates
        dates = getReadingsList();
        weeks = groupIntoWeeks(getReadingsList());

        //Create the ".summary" files, that will be used to graph the weekly exposures
        createSummaryFiles();

        //Graphing component 1 - navigation and label
        prevWeek = (Button) findViewById(R.id.prevWeek);
        nextWeek = (Button) findViewById(R.id.nextWeek);
        weekLabel = (TextView) findViewById(R.id.weekLabel);

        //Initialise spinner
        weekPicker = (Spinner) findViewById(R.id.weekPicker);
        //Construct the weeks list to display in the spinner
        ArrayList<String> weeksList = new ArrayList<String>();
        for (int i = 0; i < weeks.size(); i++) {
            weeksList.add("Week " + Integer.toString(i+1));
        }
        String[] weeksListContent = new String[weeksList.size()];
        weeksListContent = weeksList.toArray(weeksListContent);
        ArrayAdapter<String> weekPickerContent = new ArrayAdapter<String>(
                getActivity(), android.R.layout.simple_spinner_dropdown_item, weeksListContent
        );
        weekPicker.setAdapter(weekPickerContent);
        //Default the selection to most recent week
        weekPicker.setSelection(weekPickerContent.getPosition(weekPickerContent.getItem(weekPickerContent.getCount()-1)));
        //Set listeners for each selection
        weekPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                weekPicker.setSelection(position); //change selected to the one clicked

                //Update the graph components depending on what was selected
                currWeekIndex = position;
                updateWeeklyGraphComponents();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing when nothing selected
            }
        });

        //Set default week to be the most recent
        if (weeks.size() == 0) {
            //do nothing
        } else {
            currWeekIndex = weeks.size()-1;
        }

        //Default graph to present Daily readings
        updateWeeklyGraphComponents();
    }

    // ## HELPER FUNCTIONS

    //Updates the following components based on what date was selected:
    // - Sets listeners to the prevWeek and nextWeek buttons
    // - Updates the label to display the current week
    public void updateWeeklyGraphComponents() {
        if (weeks.size() == 0 || weeks.size() == 1) {
            //When no ".summary" file are on the device, disable all buttons
            // and graph nothing.
            prevWeek.setEnabled(false);
            //set nextDate button to be disabled
            nextWeek.setEnabled(false);

            //when there is exactly 1 ".summary" file is on the device, set
            // the text label and graph the date file
            if (weeks.size() == 1) {
                //Update the label - should be defaulted to most recent day logged
                if (dates.size() == 0) {
                    //Don't set the label
                } else {
                    weekLabel.setText(dates.get(currWeekIndex).replace(".readings", ""));
                }

                //Default the graph to display the current day
                drawWeeklyGraph(weeks.get(currWeekIndex));
            }
        } else {
            weekLabel.setText("Week " + Integer.toString(currWeekIndex + 1));

            //Update the navigation buttons
            prevWeek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Move to previous date (ie older date, so go UP the dates array
                    currWeekIndex = currWeekIndex - 1;

                    //Update the label
                    weekLabel.setText("Week " + Integer.toString(currWeekIndex + 1));

                    //UpdateGraph
                    drawWeeklyGraph(weeks.get(currWeekIndex));

                    //Check for end limits of the date
                    if (currWeekIndex == 0) {
                        prevWeek.setEnabled(false);
                    }
                    if (currWeekIndex < weeks.size() - 1) {
                        nextWeek.setEnabled(true);
                    }
                }
            });

            nextWeek.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Move to next date ie newer date
                    currWeekIndex = currWeekIndex + 1;

                    //Update the label
                    weekLabel.setText("Week " + Integer.toString(currWeekIndex + 1));

                    //UpdateGraph
                    drawWeeklyGraph(weeks.get(currWeekIndex));


                    //Check for end limits of the date
                    if (currWeekIndex == weeks.size() - 1) {
                        nextWeek.setEnabled(false);
                    }
                    if (currWeekIndex > 0) {
                        prevWeek.setEnabled(true);
                    }
                }
            });

            //Display the exposures for the current day
            drawWeeklyGraph(weeks.get(currWeekIndex));

            //enable/disable the navigation buttons as necessary
            prevWeek.setEnabled(true);
            nextWeek.setEnabled(true);
            if (currWeekIndex == weeks.size()-1) {
                nextWeek.setEnabled(false);
            }
            if (currWeekIndex == 0) {
                prevWeek.setEnabled(false);
            }
        }
    }

    //Draw a bar chart, given a week of ".summary" files
    // If the summary file corresponds to the current day, reads and processes the
    // corresponding ".readings" file instead
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
            Double totalExposure = Double.parseDouble("0");

            //if file corresponds to current day, then read from its ".readings" file
            String currDate = getCurrDate();
            String dateRead = file.replace(".summary", "");
            if (currDate.equals(dateRead)) {
                //Read the .readings file
                Vector<String> readData = readFile(dateRead + ".readings");
                if (readData.size() > 0) {
                    //readings file is not empty - update total exposure
                    for (String line : readData) {
                        String[] processed = line.split(",");
                        totalExposure = totalExposure + Double.parseDouble(processed[0]);
                    }
                }
            } else {
                //Proceed with reading the .summary file
                Vector<String> readData = readFile(file);
                if (readData.size() > 0) {
                    //file is not empty - update total exposure
                    for (String line : readData) {
                        String[] processed = line.split(",");
                        totalExposure = totalExposure + Double.parseDouble(processed[1]);
                    }
                }
            }
            Double percExposure = totalExposure / RECOMMENDED_EXPOSURE * 100;
            entries.add(new BarEntry(Float.parseFloat(Double.toString(percExposure)),i)); //something was weird casting a double to float
            i = i + 1;
        }
        BarDataSet dataSet = new BarDataSet(entries,dataSetName);

        //Prepare Bar Data - dates as x-values, data-set as individual
        BarData barData = new BarData(dates,dataSet);

        //Attach the data to the chart
        BarChart chart = (BarChart) findViewById(R.id.chart);
        chart.setData(barData);

        //Extra Chart settings
        chart.animateY(1500);
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM); // Put axis on bot
        chart.getAxisRight().setEnabled(false); //  Disable right yaxis
        chart.getLegend().setEnabled(false); // Disable legend
        chart.setDescription(""); //remove description

        //format the values on the y-axis
        chart.getAxisLeft().setValueFormatter(new ValueFormatter() {
            private DecimalFormat format = new DecimalFormat("#.#");

            @Override
            public String getFormattedValue(float value) {
                return format.format(value) + " %";
            }
        });

        //Draw the chart
        chart.invalidate();

    }

    // creates a summary file for each ".readings" file
    // EXCEPT the one that corresponds to the current date
    public void createSummaryFiles() {
        //retrieve a list of all ".readings" files, and remove all their extensions
        ArrayList<String> allFiles = getReadingsList();
        ArrayList<String> readingsFiles = new ArrayList<String>();
        for (String filename : allFiles) {
            if (filename.contains(".readings")) {
                String date = filename.replace(".readings","");
                readingsFiles.add(date);
            }
        }

        //Remove the .readings file corresponding to current date
        readingsFiles.remove(new String(getCurrDate()));

        //Construct summary files for each of the remaining dates in "readingsFiles"
        for (String filename : readingsFiles) {
            writeSummaryFile(filename);
        }
    }

    //Creates a summary file corresponding to a given .readings file
    // NOTE: Input fileName is a DATE STRING
    // Format for each line: "[classification],[total exposure],[# readings in this classification]"
    // Does NOT create a summary file if the given file doesn't exist or invalid type
    public void writeSummaryFile(String fileName) {
        try {
            File readingsFile = new File(getActivity().getApplicationContext().getFilesDir(), fileName + ".readings");
            if (!readingsFile.exists()) {
                //No such file for the current ".readings" file - halt the creation
                // of the corresponding summary file
                return;
            }
            //Construct the summary file name by stripping ".readings" extension
            String summaryFileName = fileName.split(".readings")[0] + ".summary";
            File summaryFile = new File(getActivity().getApplicationContext().getFilesDir(), summaryFileName);
            //if (!summaryFile.exists()) {
            //Create the summary file that is currently non-existent
            //Prepare the file and variables for processing into a summary file
            Vector<String> readData = readFile(fileName + ".readings");
            Double totalHighExposure = 0.0;
            Double totalModExposure = 0.0;
            Double totalLowExposure = 0.0;
            int highExposureCounts = 0;
            int modExposureCounts = 0;
            int lowExposureCounts = 0;
            for (String line : readData) {
                //Split and extract the measurement data
                String[] processed = line.split(",");
                Double measurement = Double.parseDouble(processed[0]);

                //Classify the reading, and update the total and count of that classification
                if (measurement < 3) {
                    //low
                    lowExposureCounts = lowExposureCounts + 1;
                    totalLowExposure = totalLowExposure + measurement;
                } else if (measurement < 8) {
                    //moderate
                    modExposureCounts = modExposureCounts + 1;
                    totalModExposure = totalModExposure + measurement;
                } else if (measurement >= 8) {
                    //high
                    highExposureCounts = highExposureCounts + 1;
                    totalHighExposure = totalHighExposure + measurement;
                }
            }

            //Build and write the strings to summary file
            BufferedWriter writer = new BufferedWriter(new FileWriter(summaryFile, false)); //overwrite
            DecimalFormat formatter = new DecimalFormat("0.##");
            writer.write("high," + formatter.format(totalHighExposure) + "," + Integer.toString(highExposureCounts));
            writer.newLine();
            writer.write("moderate," + formatter.format(totalModExposure) + "," + Integer.toString(modExposureCounts));
            writer.newLine();
            writer.write("low," + formatter.format(totalLowExposure) + "," + Integer.toString(lowExposureCounts));
            writer.newLine();
            writer.close();

            //}
        } catch (IOException e) {
            Log.e("ianwong.test", "Unable to write to file");
        }
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
            //Log.e("test", "Unable to read file");
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

    //Returns the current date as a string in the format: "yy-mm-dd"
    String getCurrDate() {
        final Calendar cal = Calendar.getInstance();
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int mm = cal.get(Calendar.MONTH);
        int yy = cal.get(Calendar.YEAR);
        StringBuilder dateStr = new StringBuilder().append(yy).append("-").append(mm + 1).append("-").append(dd);
        return dateStr.toString();
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
