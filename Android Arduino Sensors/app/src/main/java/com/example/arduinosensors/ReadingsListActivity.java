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
import android.widget.TextView;
import android.widget.Toast;

/**
 * Responsible for:
 *  - Requesting to sync data with the arduino
 *  - Presenting UV readings as a graph
 */


public class ReadingsListActivity extends Activity {

    //UI Components
    Button sync;
    Button backHome;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readings_list);

        //Assign attributes to UI components
        sync = (Button) findViewById(R.id.sync);
        backHome = (Button) findViewById(R.id.backHome);

        //Initialise buttons
        sync.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                //Request arduino to send all data
            }
        });


    }
}
