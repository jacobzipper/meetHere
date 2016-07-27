package com.jacobzipper.meetHere;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by zipper on 7/26/16.
 */
public class BackgroundService extends Service {
    public int onStartCommand(Intent intent, int flags, int startId) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationManager lm = (LocationManager) MainActivity.mainContext.getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {}
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/update.php")).openConnection();
                    connection.setDoOutput(true);
                    String content = "name="+ URLEncoder.encode(prefs.getString("name","Default"))+"&curLat="+URLEncoder.encode(location.getLatitude()+"")+"&curLong="+URLEncoder.encode(location.getLongitude()+"");
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setFixedLengthStreamingMode(content.getBytes().length);
                    DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                    output.writeBytes(content);
                    output.flush();
                    output.close();
                }catch(Exception e) {e.printStackTrace();}
            }
        },3600*1000);
        return flags;
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
