package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Created by zipper on 7/22/16.
 */
public class MainActivity extends FragmentActivity {
    public static double midLat = 0;
    public static double midLong = 0;
    public static String username;
    public static ArrayList<String> checked = new ArrayList<String>();
    public static ArrayList<String> friends = new ArrayList<String>();
    public static ArrayList<String> pending = new ArrayList<String>();
    public static ArrayList<LatLng> latlongs = new ArrayList<LatLng>();
    public static ArrayList<String> userPhones = new ArrayList<String>();
    public static ArrayList<String> newUsers = new ArrayList<String>();
    public static FragmentActivity mainContext;
    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private DrawerLayout mDrawerLayout;
    private DatabaseReference dbReference;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(getApplicationContext(),BackgroundService.class));
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        username = prefs.getString("meetHere-username","Default");
        if(mainContext==null) FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mainContext = this;
        dbReference = FirebaseDatabase.getInstance().getReference("users");
        if(!username.equals("Default")) {
            getStuff();
            dbReference.child(username).child("friends").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) friends = dataSnapshot.getValue(type);
                    else friends = new ArrayList<String>();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            dbReference.child(username).child("pending").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) pending = dataSnapshot.getValue(type);
                    else pending = new ArrayList<String>();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mainUI();
        }
        else {
            setContentView(R.layout.registration_login);
            findViewById(R.id.registerButtonShit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    registerStuff();
                }
            });
            findViewById(R.id.logInButtonShit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    loginStuff();
                }
            });
        }
    }
    public void mainUI() {
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.userName)).setText(prefs.getString("meetHere-realName","Default"));
        mNavItems.add(new NavItem("Home", "", R.mipmap.home));
        mNavItems.add(new NavItem("meetHere", "Find a place to meet with your friends!", R.mipmap.ic_launcher));
        mNavItems.add(new NavItem("Friends", "Manage your friends!", R.mipmap.friend));
        mNavItems.add(new NavItem("Pending", "Accept or deny new friends!", R.mipmap.pending));
        mNavItems.add(new NavItem("Help","Email us with anything you'd like to tell us.",R.mipmap.help));
        mNavItems.add(new NavItem("Logout","",R.mipmap.logout));

        // DrawerLayout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        // Populate the Navigtion Drawer with options
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        // Drawer Item click listeners
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });
        findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(mDrawerPane, true);
            }
        });

        MobileAds.initialize(getApplicationContext(), "ca-app-pub-4122970782896620/5395595791");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();

        mAdView.loadAd(adRequest);
    }

    private void selectItemFromDrawer(int position) {
        findViewById(R.id.welcomeText).setVisibility(View.GONE);
        findViewById(R.id.textView3).setVisibility(View.GONE);
        findViewById(R.id.imageView).setVisibility(View.GONE);
        if(mNavItems.get(position).mTitle.equals("Home")) {
            Fragment fragment = new HomeFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();
        }
        else if(mNavItems.get(position).mTitle.equals("Friends")) {
            final Fragment fragment = new FriendsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();

        }
        else if(mNavItems.get(position).mTitle.equals("meetHere")) {
            final Fragment fragment = new MeetFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();

        }
        else if(mNavItems.get(position).mTitle.equals("Pending")) {
            final Fragment fragment = new PendingFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();

        }
        else if(mNavItems.get(position).mTitle.equals("Help")) {
            final Fragment fragment = new HelpFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.mainContent, fragment).commit();

        }
        else if(mNavItems.get(position).mTitle.equals("Logout")) {
            editor.putString("meetHere-username","Default");
            editor.putString("meetHere-realName","Default");
            editor.commit();
            startActivity(new Intent(this,MainActivity.class));
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    public void loginStuff() {
        setContentView(R.layout.activity_login);
        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nameText = ((EditText)findViewById(R.id.userNameEntry)).getText().toString();
                final String passText = ((EditText)findViewById(R.id.passwordEntry)).getText().toString();
                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(nameText).hasChild("password")) {
                            String salt = dataSnapshot.child(nameText).child("salt").getValue().toString();
                            String winPass = dataSnapshot.child(nameText).child("password").getValue().toString();
                            if (sha256(passText + salt).equals(winPass)) {
                                editor.putString("meetHere-realName", dataSnapshot.child(nameText).child("realName").getValue().toString());
                                editor.putString("meetHere-username", nameText);
                                editor.commit();
                                username = nameText;
                                dbReference.child(username).child("friends").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) friends = dataSnapshot.getValue(type);
                                        else friends = new ArrayList<String>();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                dbReference.child(username).child("pending").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) pending = dataSnapshot.getValue(type);
                                        else pending = new ArrayList<String>();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                getStuff();
                                mainUI();
                            }
                            else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(),"Password incorrect",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }
                        else {
                            dbReference.child(nameText).removeValue();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"Username not found",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dbReference.child(nameText).child("loggingIn").setValue("1");
                dbReference.child(nameText).child("loggingIn").removeValue();
            }
        });
    }
    public void registerStuff() {
        setContentView(R.layout.activity_register);
        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String usernameText = ((EditText) findViewById(R.id.registerUsername)).getText().toString();
                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.hasChild(usernameText)) {
                            final String nameText = ((EditText) findViewById(R.id.registerName)).getText().toString();
                            final String passText = ((EditText) findViewById(R.id.registerPass)).getText().toString();
                            final String phoneText = ((EditText) findViewById(R.id.registerPhone)).getText().toString();
                            final String realNameText = ((EditText) findViewById(R.id.registerRealName)).getText().toString();
                            final String salt = ((int)(Math.random()*10000000))+""+((int)(Math.random()*10000000))+""+((int)(Math.random()*10000000))+""+((int)(Math.random()*10000000));
                            final String hashSaltedPass = sha256(passText+salt);
                            dbReference.child(usernameText).child("password").setValue(hashSaltedPass);
                            dbReference.child(usernameText).child("salt").setValue(salt);
                            dbReference.child(usernameText).child("email").setValue(nameText);
                            dbReference.child(usernameText).child("phone").setValue(phoneText);
                            dbReference.child(usernameText).child("realName").setValue(realNameText).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getApplicationContext(),"Registered!",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        editor.putString("meetHere-username",usernameText);
                                        editor.putString("meetHere-realName", realNameText);
                                        editor.commit();
                                        mainUI();
                                    }
                                }
                            });
                        }
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(),"Username taken",Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dbReference.child("registeringUser").setValue("1");
                dbReference.child("registeringUser").removeValue();
            }
        });
    }
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }
    public void getStuff() {
        dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(username).hasChild("friends")) {
                    friends = dataSnapshot.child(username).child("friends").getValue(type);
                }
                if(dataSnapshot.child(username).hasChild("pending")) {
                    pending = dataSnapshot.child(username).child("pending").getValue(type);
                }
                for(String friend : friends) {
                    userPhones.add(dataSnapshot.child(friend).child("phone").getValue().toString());
                    double latitude = Double.parseDouble(dataSnapshot.child(friend).child("curLat").getValue().toString());
                    double longitude = Double.parseDouble(dataSnapshot.child(friend).child("curLong").getValue().toString());
                    latlongs.add(new LatLng(latitude,longitude));
                }
                userPhones.add(dataSnapshot.child(username).child("phone").getValue().toString());
                latlongs.add(new LatLng(Double.parseDouble(dataSnapshot.child(username).child("curLat").getValue().toString()),Double.parseDouble(dataSnapshot.child(username).child("curLong").getValue().toString())));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dbReference.child(username).child("requestingStuff").setValue("1");
        dbReference.child(username).child("requestingStuff").removeValue();
    }
}
