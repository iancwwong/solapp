package com.example.android.solapp;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class BaseFragment extends Fragment {

    public int BaseLayout = -1;
    public View root;
    public View findViewById(int id) {
        return root.findViewById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(BaseLayout, container, false);
        this.root = rootView;
        init();
        return rootView;
    }

    public void init() {

    }

    public void ShortToast(String keepo) {
        Toast.makeText(root.getContext(), keepo, Toast.LENGTH_SHORT).show();
    }

}