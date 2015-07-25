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
import android.widget.Button;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
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
    private Button sync;

    //BT Attributes
    Handler bluetoothIn;
    final int handlerState = 0;        				 //used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;
    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String for MAC address
    private static String address;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings);

        //Set up the Bluetooth handler
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int EOLIndexMulti = recDataString.indexOf("~");                    // determine the end-of-line
                    if (EOLIndexMulti > 0) {

                        //Multiple readings message received
                        String dataInPrint = recDataString.substring(0, EOLIndexMulti);    // take away the '~'

                        int dataLength = dataInPrint.length();

                        if (recDataString.charAt(0) == '#') {

                            //A valid message - extract each UV reading from string
                            String readingsString = recDataString.substring(1, dataLength);
                            Vector<String> readings = new Vector<String>(Arrays.asList(
                                    readingsString.split("\\+"))
                            );

                            //Construct file name based on current date
                            String fileName = getCurrDate() + ".readings";

                            //Write the readings to a file in internal storage
                            writeReadingsToFile(fileName, readings);

                            //Debugging - print out the constructed file in log
                            Vector<String> dataRead = readFile(fileName);
                            for (String line : dataRead) {
                                Log.e("arduinoSensors", line);
                            }
                        }

                        //Reset received data
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

        //Initialise UI components and buttons
        backHome = (Button) findViewById(R.id.backHome);
        backHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        //Button responsible for requesting logged readings from arduino as a string
        sync = (Button) findViewById(R.id.sync);
        sync.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Toast.makeText(getBaseContext(), "Synced", Toast.LENGTH_SHORT).show();

            }
        });

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();
    };


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
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
        try
        {
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
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    // ## BLUETOOTH STUFF

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

}
