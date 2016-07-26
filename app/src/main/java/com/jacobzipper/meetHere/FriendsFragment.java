package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
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

public class FriendsFragment extends Fragment {
    ArrayList<String> friends;

    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        new Thread() {
            public void run() {
                updateFriends();
                doListeners();
            }
        }.start();
        return inflater.inflate(R.layout.fragment_friends, container, false);
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
                    ((ListView)MainActivity.mainContext.findViewById(R.id.friendsList)).setAdapter(adapter);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void addFriend() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        try {
            String addstr = ((EditText) MainActivity.mainContext.findViewById(R.id.friendField)).getText().toString();
            MainActivity.mainContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((EditText) MainActivity.mainContext.findViewById(R.id.friendField)).setText("");
                }
            });
            HttpURLConnection connection = (HttpURLConnection)(new URL("http://jacobzipper.com/meetmethere/add_friends.php")).openConnection();
            connection.setDoOutput(true);
            String content = "name="+URLEncoder.encode(prefs.getString("name","Default"))+"&friend="+URLEncoder.encode(addstr);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setFixedLengthStreamingMode(content.getBytes().length);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(content);
            output.flush();
            output.close();
            updateFriends();
        }catch(Exception e) {e.printStackTrace();}
    }
    public void subFriend() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        try {
            String substr = ((EditText) MainActivity.mainContext.findViewById(R.id.friendField)).getText().toString();
            MainActivity.mainContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((EditText) MainActivity.mainContext.findViewById(R.id.friendField)).setText("");
                }
            });
            HttpURLConnection connection = (HttpURLConnection)(new URL("http://jacobzipper.com/meetmethere/sub_friends.php")).openConnection();
            connection.setDoOutput(true);
            String content = "name="+URLEncoder.encode(prefs.getString("name","Default"))+"&friend="+URLEncoder.encode(substr);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setFixedLengthStreamingMode(content.getBytes().length);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(content);
            output.flush();
            output.close();
            updateFriends();
        }catch(Exception e) {e.printStackTrace();}
    }
    public void doListeners() {
        MainActivity.mainContext.findViewById(R.id.addFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        addFriend();
                    }
                }.start();
            }
        });
        MainActivity.mainContext.findViewById(R.id.subFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        subFriend();
                    }
                }.start();
            }
        });
        ((ListView)MainActivity.mainContext.findViewById(R.id.friendsList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ((EditText)MainActivity.mainContext.findViewById(R.id.friendField)).setText(friends.get(i));
            }
        });
    }
}