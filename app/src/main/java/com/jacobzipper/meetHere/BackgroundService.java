package com.jacobzipper.meetHere;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by zipper on 7/26/16.
 */
public class BackgroundService extends Service {
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (!prefs.getString("meetHere-username", "Default").equals("Default")) {
                        try {
                            LocationManager lm = (LocationManager) MainActivity.mainContext.getSystemService(Context.LOCATION_SERVICE);
                            if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            }
                            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("users");
                            dbReference.child(prefs.getString("meetHere-username", "Default")).child("curLat").setValue(location.getLatitude() + "");
                            dbReference.child(prefs.getString("meetHere-username", "Default")).child("curLong").setValue(location.getLongitude() + "");
                        }catch (Exception e){e.printStackTrace();}
                    }
                    try {
                        this.sleep(300000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
        return flags;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
