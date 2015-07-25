package ianwong.test;

//Essential
import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

//Debugging
import android.util.Log;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

//UI Components
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends Activity {

    //UI components
    private TextView textBox;
    private Button writeButton;
    private Spinner readingsFiles;

    //Backend data variables
    ArrayAdapter<String> readingsListElements;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Insert elements into the readings dropdown list
        updateReadingsDropdown();

        //Initialise UI components
        //  For printing text
        textBox = (TextView) findViewById(R.id.textBox);

        //Writes data from a vector to a file
        writeButton = (Button) findViewById(R.id.writeButton);
        writeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Prepare a sample Vector
                Vector<String> readings = new Vector<String>();
                readings.add("14");
                readings.add("10");
                readings.add("5");
                readings.add("13");

                //Prepare for writing to file
                String fileName = getCurrDate() + ".readings";

                //Write the readings to a file in internal storage
                writeReadingsToFile(fileName, readings);

                //Refresh the spinner dropdown list
                updateReadingsDropdown();
                readingsFiles.setAdapter(readingsListElements);
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
                displayReadingsData((String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing
            }
        }

        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ## HELPER FUNCTIONS

    //Write to a given file, UV readings with corresponding timestamps
    // (1 line in file = 1 reading in vector)
    public void writeReadingsToFile(String fileName, Vector<String> readings) {
        String timestamp = "";

        //Create and write the readings array to file stored in internal storage
        try {
            File readingsFile = new File(this.getApplicationContext().getFilesDir(), fileName);

            if (!readingsFile.exists()) {
                //First sync: time = current time
                readingsFile.createNewFile();
                timestamp = getCurrTime();
            } else {
                //A sync has already been  made this day: time = most recently recorded timestamp + 1 min
                Vector<String> readingsFileData = readFile(fileName);
                String[] extractedData = readingsFileData.get(readingsFileData.size()-1).split(",");
                timestamp = incrementTime(extractedData[1],1);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(readingsFile, true)); //append
            for (String reading : readings) {
                writer.write(reading + "," + timestamp);
                writer.newLine();
                timestamp = incrementTime(timestamp,1);
            }
            writer.close();
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

    //Returns the current date as a string in the format: "yy-mm-dd"
    String getCurrDate() {
        final Calendar cal = Calendar.getInstance();
        int dd = cal.get(Calendar.DAY_OF_MONTH);
        int mm = cal.get(Calendar.MONTH);
        int yy = cal.get(Calendar.YEAR);
        StringBuilder dateStr = new StringBuilder().append(yy).append("-").append(mm + 1).append("-").append(dd);
        return dateStr.toString();
    }

    //Returns the current time as a string in the format: "HH:MM"
    String getCurrTime() {
        final Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);

        //Format hour and minute
        DecimalFormat formatter = new DecimalFormat("00");
        StringBuilder timeStr = new StringBuilder()
                .append(formatter.format(hour)).append(":")
                .append(formatter.format(min));
        return timeStr.toString();
    }

    //Given a time, increase it by specific hours and minutes.
    // NOTE: Does NOT consider overnight cases, ie when hours exceed 24
    String incrementTime(String currTime, int min) {
        //Process currTime into hours and minutes, and calculate individually
        String[] timeUnits = currTime.split(":");
        int newMin = Integer.parseInt(timeUnits[1]);
        int newHour = Integer.parseInt(timeUnits[0]);

        //Add the minutes first BEFORE hours (carries over to hour)
        newMin = Integer.parseInt(timeUnits[1]) + min;
        if (newMin >= 60) {
            newMin = newMin % 60;
            newHour = newHour + 1;
        }

        //Format hour and minute
        DecimalFormat formatter = new DecimalFormat("00");
        StringBuilder newTimeStr = new StringBuilder()
                .append(formatter.format(newHour)).append(":")
                .append(formatter.format(newMin));
        return newTimeStr.toString();
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

    //DEBUGGING FUNCTIONS

    //Display the contents of a particular file in a TextView (named "textBox")
    public void displayReadingsData(String fileName) {
        //Ensure textBox is empty
        textBox.setText("");

        //Read and display the contents of file
        Vector<String> readData = readFile(fileName);
        for (String line : readData) {
            textBox.append(line);
            textBox.append(System.getProperty("line.separator"));
            //Log.e("test", line);
        }
    }
}
