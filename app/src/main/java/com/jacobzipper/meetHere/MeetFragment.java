package com.jacobzipper.meetHere;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MeetFragment extends Fragment {
    ArrayList<String> friends = new ArrayList<String>();
    ArrayList<String> checked = new ArrayList<String>();

    public MeetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity.checked.clear();
        askPermsAndLocation();
        new Thread() {
            public void run() {
                updateFriends();
                doListeners();
            }
        }.start();
        return inflater.inflate(R.layout.fragment_meet, container, false);
    }
    public int in(ArrayList<String> arr, String check) {
        for(int i = 0; i < arr.size(); i++) {
            if(arr.get(i).equals(check)) {
                return i;
            }
        }
        return -1;
    }
    public void updateFriends() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        try {
            friends = new ArrayList<String>();
            String nameString = prefs.getString("name","Default");
            HttpURLConnection connection = (HttpURLConnection)(new URL("http://jacobzipper.com/meetmethere/friends.php")).openConnection();
            connection.setDoOutput(true);
            String content = "name="+ URLEncoder.encode(nameString);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setFixedLengthStreamingMode(content.getBytes().length);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(content);
            output.flush();
            output.close();
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
            JSONArray arrFriends = new JSONArray(response.toString());
            for(int i = 0; i < arrFriends.length(); i++) {
                friends.add(arrFriends.getString(i));
            }
            final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext,R.layout.text_item,friends);
            MainActivity.mainContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ListView)MainActivity.mainContext.findViewById(R.id.meetList)).setAdapter(adapter);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void locationStuff() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        new Thread() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            public void run() {

                while(true) {
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
                    try {
                        this.sleep(1000);
                    } catch (InterruptedException e) {e.printStackTrace();}
                }
            }
        }.start();
    }
    public void askPermsAndLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.mainContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationStuff();
    }
    public void doListeners() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        ((ListView) MainActivity.mainContext.findViewById(R.id.meetList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(in(checked,friends.get(i))!=-1) {
                    checked.remove(friends.get(i));
                    view.setBackgroundColor(Color.rgb(240,240,240));

                }
                else {
                    checked.add(friends.get(i));
                    view.setBackgroundColor(Color.argb(100,0,200,0));
                }
            }
        });
        (MainActivity.mainContext.findViewById(R.id.meetHereButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checked.add(prefs.getString("name","Default"));
                MainActivity.checked = checked;
                startActivity(new Intent(MainActivity.mainContext,SexyMapFragment.class));

            }
        });
    }

}