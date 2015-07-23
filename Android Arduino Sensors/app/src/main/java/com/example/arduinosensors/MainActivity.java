package com.example.arduinosensors;
 
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Debugging

  
public class MainActivity extends Activity {
    
  Button measureUV, viewGraph;
  TextView txtArduino, txtString, txtStringLength, sensorView, uvLevel, uvFeedback;
  Handler bluetoothIn;

    //For debugging
    TextView valuesList;

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
  
    setContentView(R.layout.activity_main);
  
    //Link the buttons and textViews to respective views 
    measureUV = (Button) findViewById(R.id.measureUV);
    uvLevel = (TextView) findViewById(R.id.uvLevel);
    uvFeedback = (TextView) findViewById(R.id.uvFeedback);

    //Debugging
    valuesList = (TextView) findViewById(R.id.valuesList);

    viewGraph = (Button) findViewById(R.id.sync);
    txtString = (TextView) findViewById(R.id.txtString);
    txtStringLength = (TextView) findViewById(R.id.testView1);

    //Set ViewGraph button to be able to navigate to Readings page
    viewGraph.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            startActivity(new Intent(getApplicationContext(), ReadingsActivity.class));
        }
    });

    //Set MeasureUV button to be able to request a reading from Arduino
    measureUV.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
            mConnectedThread.write("1");    // Send "1" via Bluetooth
            Toast.makeText(getBaseContext(), "Measure UV", Toast.LENGTH_SHORT).show();

        }
    });

    bluetoothIn = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == handlerState) {										//if message is what we want
            	String readMessage = (String) msg.obj;                                                                // msg.arg1 = bytes from connect thread
                recDataString.append(readMessage);      								//keep appending to string until ~
                int EOLIndexMulti = recDataString.indexOf("~");                    // determine the end-of-line
                int EOLIndexSingle = recDataString.indexOf("$");
                if (EOLIndexMulti > 0) {
                    //Multiple values requested
                    String dataInPrint = recDataString.substring(0, EOLIndexMulti);    // extract string
                    txtString.setText("Data Received = " + dataInPrint);           		
                    int dataLength = dataInPrint.length();							//get length of data received
                    txtStringLength.setText("String Length = " + String.valueOf(dataLength));
                    
                    if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                    {
                    	String sensor = recDataString.substring(1, dataLength);

                        //Debugging
                        CharSequence oldValuesList = valuesList.getText();
                        String newValuesList = oldValuesList.toString();
                        newValuesList += "\n";
                        newValuesList += sensor;
                        valuesList.setText(newValuesList);

                    }
                    recDataString.delete(0, recDataString.length()); 					//clear all string data 
                   // strIncom =" ";
                    dataInPrint = " ";
                } else if(EOLIndexSingle > 0) { // single measurement was received
                    String dataInPrint = recDataString.substring(0, EOLIndexSingle);
                    txtString.setText("Data Received = " + dataInPrint);
                    int dataLength = dataInPrint.length();                            //get length of data received
                    txtStringLength.setText("String Length = " + String.valueOf(dataLength));

                    if (recDataString.charAt(0) == '#')                                //if it starts with # we know it is what we are looking for
                    {
                        String sensor = recDataString.substring(1, 5);
                        uvLevel.setText("Current UV level = " + sensor);
                        uvFeedback.setText(getFeedback(sensor));
                    }
                    recDataString.delete(0, recDataString.length());                    //clear all string data
                    // strIncom =" ";
                    dataInPrint = " ";
                }
            }
        }
    };
      
    btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
    checkBTState();

    //Have some initial data of UV level and UV feedback when app is started
    mConnectedThread.write("1");


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
}
    
