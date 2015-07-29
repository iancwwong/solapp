package com.example.arduinosensors;
 
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

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
  
public class MainActivity extends Activity {

  //UI Components
  Button measureUV, viewGraph, sync;
  TextView uvLevel, percExposed;
  Handler bluetoothIn;

  //BT attributes
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

  //Other Constants
  public static final int RECOMMENDED_EXPOSURE = 150;

@Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //Retrieve bluetooth device info from DeviceListActivity intent
    Intent intent = getIntent();
    address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

    //LINK UI COMPONENTS

    //TextViews
    uvLevel = (TextView) findViewById(R.id.uvLevel);
    uvLevel.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            Toast.makeText(getBaseContext(), "fdbck presented", Toast.LENGTH_SHORT).show();
        }
    });

    percExposed = (TextView) findViewById(R.id.percExposed);
    //initialise the value of percExposed
    updatePercExposed();

    //Set MeasureUV button to be able to request a reading from Arduino
    measureUV = (Button) findViewById(R.id.measureUV);
    measureUV.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            mConnectedThread.write("1");    // Send "1" via Bluetooth
            Toast.makeText(getBaseContext(), "Measure UV", Toast.LENGTH_SHORT).show();

        }
    });

    //Set ViewGraph button to be able to navigate to Readings page
    viewGraph = (Button) findViewById(R.id.viewGraph);
    viewGraph.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(MainActivity.this, ReadingsActivity.class);
            i.putExtra(EXTRA_DEVICE_ADDRESS, getIntent().getStringExtra(ReadingsActivity.EXTRA_DEVICE_ADDRESS)); //pass the currently connected device
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    });

    //Set the sync button to be able to request logged readings from arduino
    sync = (Button) findViewById(R.id.sync);
    sync.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            mConnectedThread.write("0");
            Toast.makeText(getBaseContext(), "Logs Requested", Toast.LENGTH_SHORT).show();
        }
    });

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

                        //Update the total perc exposed on the UI
                        updatePercExposed();
                    }
                    recDataString.delete(0, recDataString.length()); 					//clear all string data
                    dataInPrint = " ";
                } else if(EOLIndexSingle > 0) { // single measurement was received
                    String dataInPrint = recDataString.substring(0, EOLIndexSingle);
                    int dataLength = dataInPrint.length();
                    if (recDataString.charAt(0) == '#') {
                        String sensor = recDataString.substring(1, 5);
                        uvLevel.setText(sensor);
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
  }

   
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

    //Update the percentage exposed textview by recalculating the formula:
    // % exposed = (Total Accumulated Exposure) / (Recommended Exposure)
    public void updatePercExposed() {
        Double percExposedValue = getTotalExposure() / RECOMMENDED_EXPOSURE * 100;

        //format the result to 2dp
        DecimalFormat formatter = new DecimalFormat("0.##");

        percExposed.setText(String.valueOf(formatter.format(percExposedValue)) + "%");
    }

    //Calculate the total exposure of the day by opening the corresponding file
    // and accumulating all the readings
    Double getTotalExposure() {
        Double totalExposure = 0.0;

        //Open the file corr to the current day
        String fileName = getCurrDate() + ".readings";
        Vector<String> dataRead = readFile(fileName);

        //Calculate the total
        if (dataRead.size() != 0) {
            for (String line : dataRead) {
                String[] processed = line.split(",");
                totalExposure = totalExposure + Double.parseDouble(processed[0]);
            }
        } else {
            //No loggings recorded in file OR file doesn't exist
            // Do nothing - keep totalExposure to 0
        }

        return totalExposure;
    }

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
}
    
