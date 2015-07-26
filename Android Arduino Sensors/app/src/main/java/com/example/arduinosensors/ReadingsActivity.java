package com.example.arduinosensors;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

//Reading and writing files
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.IOException;

//Time and Dates
import java.text.DecimalFormat;
import java.util.Calendar;

//Data Representations
import java.util.Vector;

public class ReadingsActivity extends Activity {

    //UI Components
    private Button backHome;
    private Spinner readingsFiles;

    //Debugging
    private Button writeButton;

    //Backend data variables
    ArrayAdapter<String> readingsListElements;

    // String for MAC address
    private static String address;
    // EXTRA string to send on to page for readings viewing
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //Prepare the dropdown list
        updateReadingsDropdown();

        //Initialise UI components and buttons
        backHome = (Button) findViewById(R.id.backHome);
        backHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ReadingsActivity.this, MainActivity.class);
                i.putExtra(EXTRA_DEVICE_ADDRESS, getIntent().getStringExtra(ReadingsActivity.EXTRA_DEVICE_ADDRESS)); //pass the currently connected device
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });

        //  Displays to the user the list of dates / files in the internal storage
        readingsFiles = (Spinner) findViewById(R.id.readingsFiles);
        readingsFiles.setAdapter(readingsListElements);
        // Set on-item-click listeners to item in the dropdown list
        readingsFiles.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                readingsFiles.setSelection(position); //change selected to the one clicked

                //Prepare variables for graph
                ArrayList<Entry> dayReadings = new ArrayList<Entry>();
                ArrayList<String> timestamps = new ArrayList<String>();

                //Process each line in the file by extracting reading and corr timestamp
                //Read the file
                Vector<String> dataRead = readFile((String) parent.getItemAtPosition(position));
                for (int i = 0; i < dataRead.size(); i++) {
                    String[] processedLine = dataRead.get(i).split(",");
                    dayReadings.add(new Entry(Float.parseFloat(processedLine[0]), i));
                    timestamps.add(processedLine[1]);
                }

                //Finalise the graph data
                LineDataSet graphData = new LineDataSet(dayReadings, "");

                // Graphing
                LineChart chart = (LineChart) findViewById(R.id.chart);
                LineData data = new LineData(timestamps, graphData);
                chart.setData(data);

                graphData.setColors(new int[]{R.color.line_color}, ReadingsActivity.this);
                graphData.setLineWidth(3); // min = 0.2f, max = 10f
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
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        });
    };

    // ## HELPER FUNCTIONS

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

    //Determine the elements to insert into the dropdown
    // that allows the user to select a readings file to view
    public void updateReadingsDropdown() {

        //Construct a list of ONLY UV readings files in internal storage
        List<String> files = new ArrayList<String>(Arrays.asList(getApplicationContext().fileList()));
        //Filter the list of any files that are NOT of type ".readings"
        for (int i = 0; i < files.size(); i++) {
            String fileName = files.get(i);
            if (!fileName.contains(".readings")) {
                //File is not a readings file - remove from list
                files.remove(i);
                i--; //adjust the counter
            } else {
                //strip the extension
                fileName = fileName.replace(".readings","");
            }
        }

        //Sort the array in DESCENDING order (ie most recent date to oldest)
        Collections.sort(files);
        Collections.reverse(files);
        String[] readingsFilesList = files.toArray(new String[files.size()]); //assign list to variable

        //Prepare adapter that contains elements
        readingsListElements = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, readingsFilesList
        );
    }


}
