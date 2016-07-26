package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;
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

public class YelpFragment extends Fragment{
    ArrayList<Boolean> isOpens = new ArrayList<Boolean>();
    ArrayList<String> phones = new ArrayList<String>();
    ArrayList<String> urls = new ArrayList<String>();
    ArrayList<String> addresses = new ArrayList<String>();
    double midLat;
    double midLong;
    ArrayList<Business> businesses;
    ArrayList<String> businessNames;
    boolean networkingDone = false;
    ArrayList<LatLng> latlongs = new ArrayList<LatLng>();
    YelpAPIFactory apiFactory = new YelpAPIFactory("8tEL_-l8SMpai0PV0dUnpA", "ZO9RlcebiOqKcJiYrUdZfc85hj0", "FXn_gfDzucwbr_BEma3uFxvHxq3M94H2", "3bqVIJcP5jI3uOYeLwQAgKX27A8");
    YelpAPI yelpAPI = apiFactory.createAPI();
    public YelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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
                        latlongs.add(new LatLng(latitude, longitude));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                MainActivity.checked.clear();
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
        midLat = (topY + bottomY) / 2.0;
        midLong = (leftX + rightX) / 2.0;
        listenersSearch();
        return inflater.inflate(R.layout.fragment_yelp, container, false);
    }
    public void listenersSearch() {
        new Thread() {
            public void run() {
                try {
                    this.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                MainActivity.mainContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainContext.findViewById(R.id.backToMeet).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final Fragment fragment = new MeetFragment();
                                FragmentManager fragmentManager = getFragmentManager();
                                fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();
                            }
                        });
                        MainActivity.mainContext.findViewById(R.id.foodButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                search("food");
                            }
                        });
                        MainActivity.mainContext.findViewById(R.id.entButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                search("entertainment");
                            }
                        });
                        MainActivity.mainContext.findViewById(R.id.adultButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                search("adult");
                            }
                        });
                    }
                });
            }
        }.start();

    }
    public void doListeners() {
        MainActivity.mainContext.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((ListView) MainActivity.mainContext.findViewById(R.id.yelpList)).setAdapter(new YelpAdapter(MainActivity.mainContext, R.layout.yelp_item, businessNames, isOpens, phones, urls,addresses));
                MainActivity.mainContext.findViewById(R.id.backToMeet).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final Fragment fragment = new MeetFragment();
                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();
                    }
                });

            }
        });
    }
    public void search(final String term) {
        new Thread() {
            public void run() {
                Map<String, String> params = new HashMap<>();
                params.put("term", term);
                params.put("limit", "8");
                params.put("sort","1");
                CoordinateOptions coordinate = CoordinateOptions.builder().latitude(midLat).longitude(midLong).build();
                Call<SearchResponse> call = yelpAPI.search(coordinate, params);
                Response<SearchResponse> response = null;
                try {
                    response = call.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                businesses = response.body().businesses();
                businessNames = new ArrayList<String>();
                for(Business business : businesses) {
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
                doListeners();
            }
        }.start();
    }

}