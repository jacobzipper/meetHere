package com.jacobzipper.meetHere;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.yelp.clientlib.connection.YelpAPI;
import com.yelp.clientlib.connection.YelpAPIFactory;
import com.yelp.clientlib.entities.Business;
import com.yelp.clientlib.entities.SearchResponse;
import com.yelp.clientlib.entities.options.CoordinateOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SexyMapFragment extends FragmentActivity implements GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {
    OnMapReadyCallback fuckThis = this;
    ArrayList<Boolean> isOpens = new ArrayList<Boolean>();
    ArrayList<String> phones = new ArrayList<String>();
    ArrayList<String> urls = new ArrayList<String>();
    ArrayList<String> addresses = new ArrayList<String>();
    ArrayList<Business> businesses = new ArrayList<Business>();
    ArrayList<String> businessNames;
    ArrayList<String> userPhones = new ArrayList<String>();
    boolean networkingDone = false;
    ArrayList<LatLng> latlongs = new ArrayList<LatLng>();
    YelpAPIFactory apiFactory = new YelpAPIFactory("8tEL_-l8SMpai0PV0dUnpA", "ZO9RlcebiOqKcJiYrUdZfc85hj0", "FXn_gfDzucwbr_BEma3uFxvHxq3M94H2", "3bqVIJcP5jI3uOYeLwQAgKX27A8");
    YelpAPI yelpAPI = apiFactory.createAPI();
    String curTerm = "";
    private GoogleMap mMap;
    boolean searchDone = false;
    public SexyMapFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sexy_map);

        new Thread() {
            public void run() {
                for (String name : MainActivity.checked) {
                    try {
                        HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/get_info.php")).openConnection();
                        connection.setDoOutput(true);
                        String content = "name=" + URLEncoder.encode(name);
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
                        while ((line = rd.readLine()) != null) {
                            response.append(line);
                        }
                        rd.close();
                        JSONObject person = new JSONObject(response.toString());
                        double latitude = Double.parseDouble(person.getString("curLat"));
                        double longitude = Double.parseDouble(person.getString("curLong"));
                        userPhones.add(person.getString("phone"));
                        latlongs.add(new LatLng(latitude, longitude));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                networkingDone = true;
            }
        }.start();
        while(!networkingDone);
        double leftX = Integer.MAX_VALUE;
        double rightX = Integer.MIN_VALUE;
        double topY = Integer.MIN_VALUE;
        double bottomY = Integer.MAX_VALUE;
        for (LatLng ltlng : latlongs) {
            if (ltlng.latitude > topY) {
                topY = ltlng.latitude;
            }
            if (ltlng.latitude < bottomY) {
                bottomY = ltlng.latitude;
            }
            if (ltlng.longitude < leftX) {
                leftX = ltlng.longitude;
            }
            if (ltlng.longitude > rightX) {
                rightX = ltlng.longitude;
            }
        }
        MainActivity.midLat = (topY + bottomY) / 2.0;
        MainActivity.midLong = (leftX + rightX) / 2.0;
        // Inflate the layout for this fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        findViewById(R.id.foodButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTerm = "food";
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(fuckThis);
            }
        });
        findViewById(R.id.funButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTerm = "entertainment";
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(fuckThis);
            }
        });
        findViewById(R.id.studyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTerm = "study";
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                mapFragment.getMapAsync(fuckThis);
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);

        int i = 0;
        for(LatLng curL : latlongs) {
            mMap.addMarker(new MarkerOptions().position(curL).title(MainActivity.checked.get(i)));
            i++;
        }

        search(curTerm);
        while(!searchDone);
        searchDone = false;
        mMap.addMarker(new MarkerOptions().position(new LatLng(MainActivity.midLat,MainActivity.midLong)).title("MIDPOINT").icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher))));
        for(Business business : businesses) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(business.location().coordinate().latitude(),business.location().coordinate().longitude())).title(business.name()).snippet(business.snippetText()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.midLat,MainActivity.midLong),12));
    }

    public void search(final String term) {
        new Thread() {
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("term",term);
                params.put("limit", "8");
                params.put("sort","1");
                CoordinateOptions coordinate = CoordinateOptions.builder().latitude(MainActivity.midLat).longitude(MainActivity.midLong).build();
                Call<SearchResponse> call = yelpAPI.search(coordinate, params);
                Response<SearchResponse> response = null;
                try {
                    response = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ArrayList<Business> myBusinesses = response.body().businesses();
                businessNames = new ArrayList<String>();
                for(Business business : myBusinesses) {
                    businesses.add(business);
                    businessNames.add(business.name());
                    phones.add(business.displayPhone());
                    isOpens.add(business.isClosed());
                    urls.add(business.mobileUrl());
                    ArrayList<String> addy = business.location().displayAddress();
                    String addystr = "";
                    for(String cur : addy) {
                        addystr += cur+" ";
                    }
                    addystr = addystr.substring(0,addystr.length()-1);
                    addresses.add(addystr);
                }
            searchDone=true;
            }
        }.start();
    }
    public int in(ArrayList<String> arr, String check) {
        for(int i = 0; i < arr.size(); i++) {
            if(arr.get(i).equals(check)) {
                return i;
            }
        }
        return -1;
    }
    @Override
    public void onInfoWindowClick(Marker marker) {
        if(in(MainActivity.checked,marker.getTitle())==-1 && !marker.getTitle().equals("MIDPOINT")) {
            for(String phoneNum : userPhones) {
                if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.mainContext, new String[]{Manifest.permission.SEND_SMS}, 1);

                }
                else {
                    SmsManager sms = SmsManager.getDefault();
                    String message = "Hey, lets meet at " + marker.getTitle() + "!";
                    sms.sendTextMessage(phoneNum, null, message, null, null);
                }
            }
        }
    }
}
