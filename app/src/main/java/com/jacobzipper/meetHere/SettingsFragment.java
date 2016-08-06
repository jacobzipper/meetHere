package com.jacobzipper.meetHere;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

public class SettingsFragment extends Fragment {


    public SettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View fragview = inflater.inflate(R.layout.fragment_settings, container, false);
        final ToggleButton button = (ToggleButton) fragview.findViewById(R.id.serviceToggle);
        if(isMyServiceRunning(BackgroundService.class)) {
            button.setChecked(true);
        }
        else {
            button.setChecked(false);
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isMyServiceRunning(BackgroundService.class)) {
                    MainActivity.mainContext.stopService(new Intent(MainActivity.mainContext,BackgroundService.class));
                }
                else {
                    MainActivity.mainContext.startService(new Intent(MainActivity.mainContext,BackgroundService.class));
                }
            }
        });
        return fragview;
    }
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) MainActivity.mainContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}