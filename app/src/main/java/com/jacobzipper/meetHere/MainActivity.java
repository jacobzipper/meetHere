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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainContext = this;
        auth = FirebaseAuth.getInstance();
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = auth.getCurrentUser();
                if(user!=null) {
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
        };
    }

    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    protected void onStop() {
        super.onStop();
        if(authListener!=null) {
            auth.removeAuthStateListener(authListener);
        }

    }
    public void mainUI() {
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        ((TextView)findViewById(R.id.userName)).setText(prefs.getString("name","Default"));
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
            auth.signOut();
            startActivity(new Intent(this,MainActivity.class));
        }
        // Close the drawer
        mDrawerLayout.closeDrawer(mDrawerPane);
    }

    public void loginStuff() {
        final String nameText = ((EditText)findViewById(R.id.userNameEntry)).getText().toString();
        final String passText = ((EditText)findViewById(R.id.passwordEntry)).getText().toString();
        findViewById(R.id.loginButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth.signInWithEmailAndPassword(nameText,passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            mainUI();
                            Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Not able to log in. Please try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
    public void registerStuff() {
        setContentView(R.layout.activity_register);
        findViewById(R.id.registerButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String nameText = ((EditText) findViewById(R.id.registerName)).getText().toString();
                final String passText = ((EditText) findViewById(R.id.registerPass)).getText().toString();
                auth.createUserWithEmailAndPassword(nameText,passText).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            mainUI();
                            Toast.makeText(getApplicationContext(), "Registered!", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Registration unsuccessful, please try again", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
