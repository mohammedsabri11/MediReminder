package com.erh.erh.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.erh.erh.DarkModePrefManager;
import com.erh.erh.OnFragment;
import com.erh.erh.R;


import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;


public class SettingsFragment extends Fragment  {
    // TODO: Rename parameter arguments, choose names that match
    private Switch darkModeSwitch;

    public SettingsFragment() {
        // Required empty public constructor
    }


    public OnFragment fComm;




    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragment)
            fComm = (OnFragment)context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragment)
            fComm= (OnFragment)activity;
    }


    private void setDarkModeSwitch(){

        darkModeSwitch.setChecked(new DarkModePrefManager(getContext()).isNightMode());

        darkModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DarkModePrefManager darkModePrefManager = new DarkModePrefManager(getActivity());
                darkModePrefManager.setDarkMode(!darkModePrefManager.isNightMode());
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                fComm.onDarkMode();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate( R.layout.fragment_settings, container, false);


        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        setDarkModeSwitch();
        return view;
    }


}

