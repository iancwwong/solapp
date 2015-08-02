package com.example.android.navigationdrawerexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

public class SettingsFragment extends BaseFragment {

    //## ATTRIBUTES
    TextView skinToneLabel;
    Spinner skinToneList;
    TextView ageLabel;
    EditText ageValue;
    Button saveButton;

    //MainActivity reference
    public MainActivity main;

    public SettingsFragment() {
        BaseLayout = R.layout.fragment_settings;
    }

    // Settings Code

    @Override
    public void init() {

        //Bind skinTone label
        skinToneLabel = (TextView) findViewById(R.id.skinToneLabel);

        //Initialise skin tone list dropdown
        skinToneList = (Spinner) findViewById(R.id.skinToneList);
        ArrayAdapter<String> skinTonesContent = new ArrayAdapter<String>(
            getActivity(), android.R.layout.simple_spinner_dropdown_item, getSkinToneList()
        );
        skinToneList.setAdapter(skinTonesContent);
        //Set listeners for each selection
        skinToneList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                skinToneList.setSelection(position); //change selected to the one clicked
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Do nothing when nothing selected
            }
        });


        //Bind Age components
        ageLabel = (TextView) findViewById(R.id.ageLabel);
        ageValue = (EditText) findViewById(R.id.ageValue);

        //Bind and initialise save button
        saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ToDO: Save settings to a ".settings" file
                ShortToast("Settings Saved.");
            }
        });

    }

    // ## HELPER FUNCTIONS

    public String[] getSkinToneList() {
        String[] skinTones = {"Pale", "White", "Tanned", "Brown", "Dark Brown", "Black"};
        return skinTones;
    }

}