package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PendingFragment extends Fragment {
    ArrayList<String> pending = new ArrayList<String>();
    String curName="";
    View lastView = null;
    public PendingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        new Thread() {
            public void run() {
                try {
                    this.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ((ListView)MainActivity.mainContext.findViewById(R.id.pending)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        if(lastView!=null) {
                            lastView.setBackgroundColor(Color.rgb(240,240,240));
                        }
                        lastView = view;
                        view.setBackgroundColor(Color.argb(100,0,200,0));
                        curName = pending.get(i);
                    }
                });
                updatePending();
                MainActivity.mainContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.mainContext.findViewById(R.id.denyButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.mainContext,"DENIED",Toast.LENGTH_LONG).show();
                                subPending(curName);
                            }
                        });
                        MainActivity.mainContext.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.mainContext,"Accepted!",Toast.LENGTH_LONG).show();
                                addPending(curName);
                            }
                        });
                    }
                });
            }
        }.start();
        return inflater.inflate(R.layout.fragment_pending, container, false);
    }
    public void updatePending() {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        new Thread() {
            public void run() {
                try {
                    pending.clear();
                    String nameString = prefs.getString("name", "Default");
                    HttpURLConnection connection = (HttpURLConnection) (new URL("http://jacobzipper.com/meetmethere/pending.php")).openConnection();
                    connection.setDoOutput(true);
                    String content = "name=" + URLEncoder.encode(nameString);
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
                    JSONArray arrFriends = new JSONArray(response.toString());
                    for (int i = 0; i < arrFriends.length(); i++) {
                        pending.add(arrFriends.getString(i));
                    }
                    final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext, R.layout.text_item, pending);
                    MainActivity.mainContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((ListView) MainActivity.mainContext.findViewById(R.id.pending)).setAdapter(adapter);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
    public void addPending(final String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        new Thread() {
            public void run() {
                try {

                    HttpURLConnection connection = (HttpURLConnection)(new URL("http://jacobzipper.com/meetmethere/pending_to_friend.php")).openConnection();
                    connection.setDoOutput(true);
                    String content = "name="+URLEncoder.encode(prefs.getString("name","Default"))+"&friend="+URLEncoder.encode(name);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setFixedLengthStreamingMode(content.getBytes().length);
                    DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                    output.writeBytes(content);
                    output.flush();
                    output.close();
                    updatePending();
                    MainActivity.mainContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext, R.layout.text_item, pending);
                            MainActivity.mainContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((ListView) MainActivity.mainContext.findViewById(R.id.pending)).setAdapter(adapter);
                                }
                            });
                        }
                    });
                }catch(Exception e) {e.printStackTrace();}
            }
        }.start();
    }
    public void subPending(final String name) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mainContext);
        new Thread() {
            public void run() {
                try {
                    HttpURLConnection connection = (HttpURLConnection)(new URL("http://jacobzipper.com/meetmethere/sub_pending.php")).openConnection();
                    connection.setDoOutput(true);
                    String content = "name="+URLEncoder.encode(prefs.getString("name","Default"))+"&friend="+URLEncoder.encode(name);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    connection.setFixedLengthStreamingMode(content.getBytes().length);
                    DataOutputStream output = new DataOutputStream(connection.getOutputStream());
                    output.writeBytes(content);
                    output.flush();
                    output.close();
                    updatePending();
                    MainActivity.mainContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext, R.layout.text_item, pending);
                            MainActivity.mainContext.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ((ListView) MainActivity.mainContext.findViewById(R.id.pending)).setAdapter(adapter);
                                }
                            });
                        }
                    });
                }catch(Exception e) {e.printStackTrace();}
            }
        }.start();
    }
}