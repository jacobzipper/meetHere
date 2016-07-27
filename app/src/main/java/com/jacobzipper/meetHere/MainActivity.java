package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by zipper on 7/22/16.
 */
public class MainActivity extends FragmentActivity {
    public static double midLat = 0;
    public static double midLong = 0;
    public static ArrayList<String> checked = new ArrayList<String>();
    public static FragmentActivity mainContext;
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    private DrawerLayout mDrawerLayout;

    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor editor = prefs.edit();
        mainContext = this;
        super.onCreate(savedInstanceState);
        startService(new Intent(this,BackgroundService.class));
        if (prefs.getString("name", "Default").equals("Default")) {
            setContentView(R.layout.activity_register);
            findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String nameText = ((EditText) findViewById(R.id.registerName)).getText().toString();
                    final String passText = ((EditText) findViewById(R.id.registerPass)).getText().toString();
                    final String realNameText = ((EditText) findViewById(R.id.registerRealName)).getText().toString();
                    final String phoneText = ((EditText) findViewById(R.id.registerPhone)).getText().toString();

                    new Thread() {
                        public void run() {
                            String resp;
                            do {
                                resp = tryRegistering(nameText,passText,realNameText,phoneText);
                                if(resp.equals("username taken")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Username taken",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Registered!",Toast.LENGTH_LONG).show();
                                            editor.putString("name", nameText);
                                            editor.putString("loggedIn","true");
                                            editor.commit();
                                        }
                                    });
                                }
                            }while(resp.equals("username taken"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainUI();
                                }
                            });

                        }
                    }.start();


                }
            });
        }
        else if(prefs.getString("loggedIn","Default").equals("false")) {
            setContentView(R.layout.activity_login);
            findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String username = ((EditText)findViewById(R.id.userNameEntry)).getText().toString();
                    final String password = ((EditText)findViewById(R.id.passwordEntry)).getText().toString();
                    new Thread() {
                        public void run() {
                            String resp;
                            do {
                                resp = tryLoggingIn(username,password);
                                if(resp.equals("Username not found")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Username not found",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else if(resp.equals("Incorrect password")) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Incorrect password",Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                                else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(),"Logged in!",Toast.LENGTH_LONG).show();
                                            editor.putString("loggedIn","true");
                                            editor.commit();
                                        }
                                    });
                                }
                            }while(resp.equals("Username not found") || resp.equals("Incorrect password"));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainUI();
                                }
                            });
                        }
                    }.start();
                }
            });
        }
        else {
           mainUI();

        }
    }
    public void mainUI() {
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((TextView)findViewById(R.id.userName)).setText(prefs.getString("name","Default"));
        mNavItems.add(new NavItem("Home", "", R.mipmap.home));
        mNavItems.add(new NavItem("meetHere", "Find a place to meet with your friends!", R.mipmap.ic_launcher));
        mNavItems.add(new NavItem("Friends", "Manage your friends!", R.mipmap.friend));
        mNavItems.add(new NavItem("Pending", "Accept or deny new friends!", R.mipmap.friend));
        mNavItems.add(new NavItem("Logout","",R.mipmap.ic_launcher));

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
        else if(mNavItems.get(position).mTitle.equals("Logout")) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString("loggedIn","false");
            editor.commit();
            startActivity(new Intent(this,MainActivity.class));
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }
    public String tryRegistering(String nameText, String passText, String realNameText, String phoneText) {
        StringBuffer response = new StringBuffer();
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/register.php")).openConnection();
            connection.setDoOutput(true);
            String content = "name=" + URLEncoder.encode(nameText) + "&password=" + URLEncoder.encode(passText) + "&realname=" + URLEncoder.encode(realNameText) + "&phone=" + URLEncoder.encode(phoneText);
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
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
        }catch (Exception e) {e.printStackTrace();}
        return response.toString();
    }
    public String tryLoggingIn(String username, String password) {
        StringBuffer response = new StringBuffer();
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/login.php")).openConnection();
            connection.setDoOutput(true);
            String content = "name=" + URLEncoder.encode(username) + "&password=" + URLEncoder.encode(password);
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
            while ((line = rd.readLine()) != null) {
                response.append(line);
            }
            rd.close();
        }catch (Exception e) {e.printStackTrace();}
        return response.toString();
    }

}
