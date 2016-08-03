package com.jacobzipper.meetHere;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class SexyMapFragment extends FragmentActivity implements GoogleMap.OnInfoWindowLongClickListener,GoogleMap.OnInfoWindowClickListener, OnMapReadyCallback {
    OnMapReadyCallback fuckThis = this;
    ArrayList<Business> businesses = new ArrayList<Business>();
    ArrayList<String> businessNames = new ArrayList<String>();
    YelpAPIFactory apiFactory = new YelpAPIFactory("8tEL_-l8SMpai0PV0dUnpA", "ZO9RlcebiOqKcJiYrUdZfc85hj0", "FXn_gfDzucwbr_BEma3uFxvHxq3M94H2", "3bqVIJcP5jI3uOYeLwQAgKX27A8");
    YelpAPI yelpAPI = apiFactory.createAPI();
    String curTerm = "";
    boolean searchDone = false;
    public SexyMapFragment() {
        // Required empty public constructor
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sexy_map);
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4122970782896620/5395595791");
        AdView mAdView = (AdView) findViewById(R.id.adMap);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        double leftX = Integer.MAX_VALUE;
        double rightX = Integer.MIN_VALUE;
        double topY = Integer.MIN_VALUE;
        double bottomY = Integer.MAX_VALUE;
        for (LatLng ltlng : MainActivity.latlongs) {
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
        findViewById(R.id.backMapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.friends.clear();
                MainActivity.userPhones.clear();
                MainActivity.checked.clear();
                MainActivity.latlongs.clear();
                MainActivity.newUsers.clear();
                MainActivity.pending.clear();
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });
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
        googleMap.clear();
        googleMap.setOnInfoWindowClickListener(this);
        for(String name : MainActivity.checked) {
            int index = in(MainActivity.friends,name);
            googleMap.addMarker(new MarkerOptions().position(MainActivity.latlongs.get(index)).title(name).snippet(MainActivity.userPhones.get(index)));
        }
        googleMap.addMarker(new MarkerOptions().position(new LatLng(MainActivity.midLat, MainActivity.midLong)).title("MIDPOINT").icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MainActivity.midLat, MainActivity.midLong), 13));
        if(!curTerm.equals("")) {
            businesses.clear();
            search(curTerm);
            while (!searchDone) ;
            searchDone = false;
            for (Business business : businesses) {
                googleMap.addMarker(new MarkerOptions().position(new LatLng(business.location().coordinate().latitude(), business.location().coordinate().longitude())).title(business.name()).snippet(getAddressFromName(business.name())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }
        }
        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }
            @Override
            public View getInfoContents(Marker marker) {
                View myContentView = null;
                if(in(businessNames,marker.getTitle())!=-1) {
                    myContentView = getLayoutInflater().inflate(
                        R.layout.info_item, null);
                    TextView tvTitle = ((TextView) myContentView
                        .findViewById(R.id.bizNameText));
                    TextView tvSnippet = ((TextView) myContentView
                        .findViewById(R.id.snippetText));
                    tvTitle.setText(marker.getTitle());
                    tvSnippet.setText(marker.getSnippet());
                }
                else  {
                    myContentView = getLayoutInflater().inflate(R.layout.classic_item,null);
                    ((TextView)myContentView.findViewById(R.id.markerTitleWin)).setText(marker.getTitle());
                    ((TextView)myContentView.findViewById(R.id.markerSnippetWin)).setText(marker.getSnippet());
                }
                return myContentView;
            }
        });
        googleMap.setOnInfoWindowLongClickListener(this);
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
                if(response==null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),"Nothing within 25 miles! Is your midpoint in the middle of the ocean?",Toast.LENGTH_LONG).show();
                        }
                    });
                    searchDone = true;
                    return;
                }
                ArrayList<Business> myBusinesses = response.body().businesses();
                businessNames = new ArrayList<String>();
                for(Business business : myBusinesses) {
                    businesses.add(business);
                    businessNames.add(business.name());
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
            for(String phoneNum : MainActivity.userPhones) {
                if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.mainContext, new String[]{Manifest.permission.SEND_SMS}, 1);
                }
                else {
                    SmsManager sms = SmsManager.getDefault();
                    String message = "Hey, lets meet at " + marker.getTitle() + "! It's located at "+getAddressFromName(marker.getTitle())+".\n\nSent from meetHere.";
                    sms.sendTextMessage(phoneNum, null, message, null, null);
                }
            }
        }
    }
    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q="+getAddressFromName(marker.getTitle()).replace(" ","+")));
        startActivity(browserIntent);
    }
    public String getAddressFromName(String name) {
        String ret = "";
        for(Business business : businesses) {
            if(business.name().equals(name)) {
                ArrayList<String> addy = business.location().displayAddress();
                for(String addpart : addy) {
                    ret+=addpart +" ";
                }
                ret = ret.substring(0,ret.length()-1);
                return ret;
            }
        }
        return ret;
    }
}

