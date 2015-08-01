package com.example.android.navigationdrawerexample;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Locale;

public class SettingsFragment extends BaseFragment {

    public SettingsFragment() {
        BaseLayout = R.layout.fragment_settings;
    }

    // Settings Code

    @Override
    public void init() {
        ShortToast("yay settings");
    }

}