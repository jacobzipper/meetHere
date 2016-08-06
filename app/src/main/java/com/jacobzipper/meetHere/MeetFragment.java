package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MeetFragment extends Fragment {
    ArrayList<String> checked = new ArrayList<String>();
    View fragView;
    boolean dismissButton = false;
    public MeetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_meet, container, false);
        MainActivity.checked.clear();
        askPermsAndLocation();
        updateFriends();
        ((ListView) fragView.findViewById(R.id.meetList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(in(checked,MainActivity.friends.get(i))!=-1) {
                    checked.remove(MainActivity.friends.get(i));
                    view.setBackgroundColor(Color.rgb(240,240,240));
                }
                else {
                    checked.add(MainActivity.friends.get(i));
                    view.setBackgroundColor(Color.argb(100,0,200,0));
                }
            }
        });
        (fragView.findViewById(R.id.meetHereButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checked.add(MainActivity.username);
                MainActivity.friends.add(MainActivity.username);
                for(String name : MainActivity.newUsers) {
                    checked.add(name);
                    MainActivity.friends.add(name);
                }
                MainActivity.checked = checked;
                Toast.makeText(MainActivity.mainContext,"Meeting...",Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.mainContext,SexyMapFragment.class));
            }
        });
        fragView.findViewById(R.id.popupOpener).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragView.findViewById(R.id.meetLayout).setBackgroundColor(Color.rgb(60,60,60));
                MainActivity.mainContext.findViewById(R.id.menu).setAlpha(.1f);
                MainActivity.mainContext.findViewById(R.id.menu).setClickable(false);
                fragView.findViewById(R.id.meetHereButton).setAlpha(.1f);
                fragView.findViewById(R.id.meetHereButton).setClickable(false);
                fragView.findViewById(R.id.popupOpener).setAlpha(.1f);
                fragView.findViewById(R.id.popupOpener).setClickable(false);
                LayoutInflater layoutInflater = (LayoutInflater)getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View tempView = layoutInflater.inflate(R.layout.add_meet_popup,null);
                final PopupWindow window = new PopupWindow(tempView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,true);
                window.setFocusable(true);
                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if(!dismissButton) {
                            window.setFocusable(true);
                            window.showAtLocation(tempView,Gravity.CENTER,0,0);
                        }
                        dismissButton = false;
                    }
                });
                window.showAtLocation(tempView,Gravity.CENTER,0,0);
                tempView.findViewById(R.id.popupCancel).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragView.findViewById(R.id.meetLayout).setBackgroundColor(Color.rgb(255,255,255));
                        MainActivity.mainContext.findViewById(R.id.menu).setAlpha(1f);
                        MainActivity.mainContext.findViewById(R.id.menu).setClickable(true);
                        fragView.findViewById(R.id.meetHereButton).setAlpha(1f);
                        fragView.findViewById(R.id.meetHereButton).setClickable(true);
                        fragView.findViewById(R.id.popupOpener).setAlpha(1f);
                        fragView.findViewById(R.id.popupOpener).setClickable(true);
                        dismissButton = true;
                        window.dismiss();
                    }
                });
                tempView.findViewById(R.id.popupAdder).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = ((TextView)tempView.findViewById(R.id.popupName)).getText().toString();
                        String phone = ((TextView)tempView.findViewById(R.id.popupPhone)).getText().toString();
                        String addy = ((TextView)tempView.findViewById(R.id.popupAddress)).getText().toString();
                        if(name.equals("") || phone.equals("") || addy.equals("")) {
                            Toast.makeText(MainActivity.mainContext,"Please enter in all fields",Toast.LENGTH_SHORT).show();
                        }
                        else {
                            MainActivity.newUsers.add(name);
                            MainActivity.userPhones.add(phone);
                            MainActivity.latlongs.add(getLocationFromAddress(MainActivity.mainContext,addy));
                            fragView.findViewById(R.id.meetLayout).setBackgroundColor(Color.rgb(240,240,240));
                            MainActivity.mainContext.findViewById(R.id.menu).setAlpha(1f);
                            MainActivity.mainContext.findViewById(R.id.menu).setClickable(true);
                            fragView.findViewById(R.id.meetHereButton).setAlpha(1f);
                            fragView.findViewById(R.id.meetHereButton).setClickable(true);
                            fragView.findViewById(R.id.popupOpener).setAlpha(1f);
                            fragView.findViewById(R.id.popupOpener).setClickable(true);
                            dismissButton = true;
                            window.dismiss();
                            Toast.makeText(MainActivity.mainContext,name+" added!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return fragView;
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
        final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext, R.layout.text_item, MainActivity.friends);
        ((ListView) fragView.findViewById(R.id.meetList)).setAdapter(adapter);

    }
    public void locationStuff() {
        new Thread() {
            public void run() {
                try {
                    LocationManager lm = (LocationManager) MainActivity.mainContext.getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    }
                    Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("users");
                    dbReference.child(MainActivity.username).child("curLat").setValue(location.getLatitude() + "");
                    dbReference.child(MainActivity.username).child("curLong").setValue(location.getLongitude() + "");
                }catch(Exception e) {e.printStackTrace();}
            }
        }.start();

    }
    public void askPermsAndLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.mainContext, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.mainContext, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        locationStuff();
    }
    public LatLng getLocationFromAddress(Context context, String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (Exception ex) {

            ex.printStackTrace();
        }

        return p1;
    }
}