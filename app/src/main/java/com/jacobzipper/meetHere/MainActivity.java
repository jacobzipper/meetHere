package com.jacobzipper.meetHere;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.yelp.clientlib.entities.Business;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by zipper on 7/22/16.
 */
public class MainActivity extends FragmentActivity {
    public static ArrayList<String> checked;
    public static Activity mainContext;
    public static String pickedBusinessName;
    public static ArrayList<Business> businesses;
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

        if (!prefs.getString("name", "Default").equals("Default")) {
            mainUI();
        } else {
            setContentView(R.layout.activity_register);
            findViewById(R.id.register).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final String nameText = ((EditText) findViewById(R.id.registerName)).getText().toString();
                    editor.putString("name", nameText);
                    editor.commit();
                    new Thread() {
                        public void run() {
                            try {
                                HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/register.php")).openConnection();
                                connection.setDoOutput(true);
                                String content = "name=" + URLEncoder.encode(nameText);
                                connection.setRequestMethod("POST");
                                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                connection.setFixedLengthStreamingMode(content.getBytes().length);
                                DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                                output.writeBytes(content);
                                output.flush();
                                output.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
    }
    public void mainUI() {
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((TextView)findViewById(R.id.userName)).setText(prefs.getString("name","Default"));
        mNavItems.add(new NavItem("Home", "", R.mipmap.home));
        mNavItems.add(new NavItem("meetHere", "Find a place to meet with your friends!", R.mipmap.ic_launcher));
        mNavItems.add(new NavItem("Friends", "Manage your friends!", R.mipmap.friend));

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
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }


}
