/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.navigationdrawerexample;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.UUID;
import java.util.Vector;

/**
 * This example illustrates a common usage of the DrawerLayout widget
 * in the Android support library.
 * <p/>
 * <p>When a navigation (left) drawer is present, the host activity should detect presses of
 * the action bar's Up affordance as a signal to open and close the navigation drawer. The
 * ActionBarDrawerToggle facilitates this behavior.
 * Items within the drawer should fall into one of two categories:</p>
 * <p/>
 * <ul>
 * <li><strong>View switches</strong>. A view switch follows the same basic policies as
 * list or tab navigation in that a view switch does not create navigation history.
 * This pattern should only be used at the root activity of a task, leaving some form
 * of Up navigation active for activities further down the navigation hierarchy.</li>
 * <li><strong>Selective Up</strong>. The drawer allows the user to choose an alternate
 * parent for Up navigation. This allows a user to jump across an app's navigation
 * hierarchy at will. The application should treat this as it treats Up navigation from
 * a different task, replacing the current task stack using TaskStackBuilder or similar.
 * This is the only form of navigation drawer that should be used outside of the root
 * activity of a task.</li>
 * </ul>
 * <p/>
 * <p>Right side drawers should be used for actions, not navigation. This follows the pattern
 * established by the Action Bar that navigation should be to the left and actions to the right.
 * An action should be an operation performed on the current contents of the window,
 * for example enabling or disabling a data overlay on top of the current content.</p>
 */
public class MainActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mDrawerTitles;

    //BT attributes
    Handler bluetoothIn;
    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // EXTRA string to send on to page for readings viewing
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // String for MAC address
    private static String address;

    //Arduino Command Constants
    private static final String REQUEST_LOGS = "0";
    private static final String REQUEST_CURRENT_UV = "1";


    //Other Constants
    public static final int RECOMMENDED_EXPOSURE = 150;

    //Variables for activity
    public double currentUVValue = 0;
    public double currentExposureLevel = 0;
    public double currentExposurePerc = 0;

    public Thread syncThread;
    public Runnable syncRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTitle = mDrawerTitle = getTitle();
        mDrawerTitles = getResources().getStringArray(R.array.drawer_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        mDrawerList.setAdapter(new ArrayAdapter<String>(
                this,
                R.layout.drawer_list_item,
                mDrawerTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ) {
            public void onDrawerClosed(View view) {
                //getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //getActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        if (savedInstanceState == null) {
            selectItem(0); // opens the first one - summary
        }

        //Initialise Bluetooth handlers and connections
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);

                    //Determine the type of message received
                    int EOLIndexMulti = recDataString.indexOf("~");                    // determine the end-of-line
                    int EOLIndexSingle = recDataString.indexOf("$");
                    int sunscreenNotification = recDataString.indexOf("&");

                    if (EOLIndexMulti > 0) {
                        //Multiple values requested
                        String dataInPrint = recDataString.substring(0, EOLIndexMulti);    // extract string
                        int dataLength = dataInPrint.length();
                        if (recDataString.charAt(0) == '#')	{
                            //A valid message - extract each UV reading from string
                            String readingsString = recDataString.substring(1, dataLength);
                            Vector<String> readings = new Vector<String>(Arrays.asList(
                                    readingsString.split("\\+"))
                            );

                            //Construct file name based on current date
                            String fileName = getCurrDate() + ".readings";

                            //Write the readings to a file in internal storage
                            writeReadingsToFile(fileName, readings);

                            //Calculate and update the exposure level
                            updateExposureLevel();

                            //Display to the user that successful synced has been done with arduino
                            ShortToast("Synced");
                        }
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        dataInPrint = " ";
                    } else if(EOLIndexSingle > 0) { // single measurement was received
                        String dataInPrint = recDataString.substring(0, EOLIndexSingle);
                        int dataLength = dataInPrint.length();
                        if (recDataString.charAt(0) == '#') {
                            String currUVMeasurement = recDataString.substring(1, 5);
                            //Update current UV Measurement
                            currentUVValue = Double.valueOf(currUVMeasurement);
                            Log.e("test", "Current UV level: " + currentUVValue);

                            //Update the current UV reading on the UI
                            updateCurrentUVLevel();

                            //Display to the user that successful reading of current UV has been done
                            // with arduino
                            ShortToast("Measured UV");
                        }
                        recDataString.delete(0, recDataString.length());                    //clear all string data
                        dataInPrint = " ";
                    } else if (sunscreenNotification > 0) {
                        String dataInPrint = recDataString.substring(0, EOLIndexMulti);    // extract string
                        int dataLength = dataInPrint.length();
                        if (recDataString.charAt(0) == '#') {

                            //Extract and confirm the notification message is valid
                            String readingsString = recDataString.substring(1, dataLength);
                            if (readingsString.equals("note")) {
                                //Sunscreen notification message received
                                // - Create dialog box
                                // - Turn on vibration
                                //Turn on vibration if one exists
                                final Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                                if (vibrator.hasVibrator()) {
                                    vibrator.vibrate(getVibratorPattern(), 1); //put vibration on repeat
                                }
                                //Build and display a dialog box
                                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
                                dlgAlert.setMessage("Please apply some sunscreen now!");
                                dlgAlert.setTitle("Sunscreen Notification");
                                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Close dialog box
                                        //By default, vibrator will close when dialog closes
                                        vibrator.cancel();
                                    }
                                });
                                dlgAlert.setCancelable(true);
                                dlgAlert.create().show();
                            }
                        }
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
        syncRunnable = new Runnable() {
            public void run() {
                /*
                while(true) {

                    synchronized (this) {
                        try {
                            Log.e("test","sync thread is running");
                            if (mConnectedThread != null && mConnectedThread.isAlive()) {
                                mConnectedThread.write("1"); //send reqeuest for curr uv measurement
                            }
                            wait(1000);
                        } catch (Exception e) {}
                    }
                }
               */
            }
        };

        //Update the current uv levels and percentage
        updateCurrentUVLevel();
        updateExposureLevel();
    }


    @Override
    public void onResume() {
        super.onResume();

        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

        //create device and set the MAC address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("x");

        //Send a request to arduino for an initial reading of the current UV level
        mConnectedThread.write(REQUEST_CURRENT_UV);

        syncThread = new Thread(syncRunnable);
        syncThread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (syncThread != null) {
            try {
                syncThread.join();
                syncThread = null;
            } catch (Exception e) {
                Log.e("test", "Can't join thread");
            }
        }
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_measure_uv).setVisible(!drawerOpen);
        menu.findItem(R.id.action_sync).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         // The action bar home/up action should open or close the drawer.
         // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId()) {
        case R.id.action_sync:
            //Send to the arduino a request for its logs
            mConnectedThread.write(REQUEST_LOGS);
            ShortToast("Syncing...");
            return true;

        case R.id.action_measure_uv:
            //Send to the arduino a request for it's current UV measurement
            mConnectedThread.write(REQUEST_CURRENT_UV);
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    /* The click listner for ListView in the navigation drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    public int CurrentPosition = -1;

    //Set selection options from menu bar
    private void selectItem(int NewPosition) {

        if (NewPosition == CurrentPosition) { return; }
        CurrentPosition = NewPosition;

        Fragment fragment = null;
        switch(NewPosition) {
            case 0:
                fragment = new SummaryFragment();
                ((SummaryFragment) fragment).main = this;
                break;

            case 1:
                fragment = new TrendsFragment();
                break;

            case 2:
                fragment = new WeeklyTrendsFragment();
                break;

            case 3:
                fragment = new SettingsFragment();
                break;

            default:
                fragment = new SummaryFragment();
                ((SummaryFragment) fragment).main = this;
                break;
        }
        Bundle args = new Bundle();

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(NewPosition, true);
        setTitle(mDrawerTitles[NewPosition]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    // ## BLUETOOTH CLASSES
    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }

    // ## BT HELPER FUNCTIONS
    //creates secure outgoing connecetion with BT device using UUID
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {
        if(btAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //## FILE HANDLING FUNCTIONS

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
            //do nothing
        }
        return dataRead;
    }


    //## GENERAL HELPER FUNCTIONS

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

    //Based on a given UV value, provide more feedback to the user
    public String getFeedback(String sensorReading) {
        //Ensure there is a reading
        if (sensorReading == null) {
            return "No reading is currently supplied.";
        }
        String feedbackString = "";

        //Convert sensorReading into a numerical value
        Float sensorValue = Float.parseFloat(sensorReading);

        //Analyse the value and provide feedback
        if (sensorValue < 1) {
            feedbackString = "This is a safe level, you are fine!";
        } else if (sensorValue < 2) {
            feedbackString = "You are in a dangerous condition. Consider applying sunscreen.";
        } else {
            //Some weird reading occurred
            feedbackString = "A strange reading has occurred.";
        }

        return feedbackString;
    }

    //Return a particular vibration pattern
    public long[] getVibratorPattern() {
        long pattern[] = {0,100,100};
        return pattern;
    }

    // ## UI Updating Functions

    //updates what's displayed on the "Current Exposure Level" within the circle progress bar
    public void updateCurrentUVLevel() {
        //Refresh the circle progress bar only for the current UV reading
        Fragment f = getFragmentManager().findFragmentById(R.id.content_frame);
        if (f instanceof SummaryFragment) {
            ((SummaryFragment) f).pb.invalidate();
            ((SummaryFragment) f).ResetBars();
        }
    }

    //updates what's displayed on the "Current Exposure Level" within the circle progress bar
    // ie the percentage of exposurelevel
    public void updateExposureLevel() {
        //Calculate the new exposure level by reading the .readings file corresponding
        // to the CURRENT day
        String todayReadings = getCurrDate() + ".readings";
        Vector<String> readData = readFile(todayReadings);
        if (readData.size() > 0) {
            for (String line : readData) {
                String[] processed = line.split(",");
                currentExposureLevel = currentExposureLevel + Double.parseDouble(processed[0]);
            }
        }

        currentExposurePerc = currentExposureLevel / RECOMMENDED_EXPOSURE * 100;

        Log.e("test","Percentage exposure: " + Double.toString(currentExposurePerc)+"%");

        //Refresh the circle progress bar
        Fragment f = getFragmentManager().findFragmentById(R.id.content_frame);
        if (f instanceof SummaryFragment) {
            ((SummaryFragment) f).pb.invalidate();
            ((SummaryFragment) f).ResetBars();
        }
    }

    //## DEBUGGING HELPER FUNCTIONS
    //Display toast message
    public void ShortToast(String keepo) {
        Toast.makeText(this, keepo, Toast.LENGTH_SHORT).show();
    }

}