package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class PendingFragment extends Fragment {
    String curName="";
    View lastView = null;
    View fragView;
    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};
    final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("users");
    public PendingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragView = inflater.inflate(R.layout.fragment_pending, container, false);
        ((ListView)fragView.findViewById(R.id.pending)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                curName = "";
                if(lastView!=null) {
                    lastView.setBackgroundColor(Color.rgb(240,240,240));
                }
                if(view!=lastView) {
                    view.setBackgroundColor(Color.argb(100, 0, 200, 0));
                    curName = MainActivity.pending.get(i);
                }
                lastView = view;
            }
        });
        dbReference.child(MainActivity.username).child("pending").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MainActivity.mainContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updatePending();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        fragView.findViewById(R.id.confirmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPending(curName);
            }
        });
        fragView.findViewById(R.id.denyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removePending(curName);
            }
        });
        updatePending();
        return fragView;
    }
    public void updatePending() {
        final CustomAdapter adapter = new CustomAdapter(MainActivity.mainContext, R.layout.text_item, MainActivity.pending);
        ((ListView) fragView.findViewById(R.id.pending)).setAdapter(adapter);
    }
    public void addPending(final String name) {
        if(!name.equals("")) {
            new Thread() {
                public void run() {
                    dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(name)) {
                                if (dataSnapshot.child(MainActivity.username).hasChild("friends")) {
                                    ArrayList<String> curFriends = dataSnapshot.child(MainActivity.username).child("friends").getValue(type);
                                    curFriends.add(name);
                                    dbReference.child(MainActivity.username).child("friends").setValue(curFriends);
                                } else {
                                    ArrayList<String> curFriends = new ArrayList<String>();
                                    curFriends.add(name);
                                    dbReference.child(MainActivity.username).child("friends").setValue(curFriends);
                                }
                                if (dataSnapshot.child(name).hasChild("friends")) {
                                    ArrayList<String> curFriends = dataSnapshot.child(name).child("friends").getValue(type);
                                    curFriends.add(MainActivity.username);
                                    dbReference.child(name).child("friends").setValue(curFriends);
                                } else {
                                    ArrayList<String> curFriends = new ArrayList<String>();
                                    curFriends.add(MainActivity.username);
                                    dbReference.child(name).child("friends").setValue(curFriends);
                                }
                                ArrayList<String> curPending = dataSnapshot.child(MainActivity.username).child("pending").getValue(type);
                                int index = in(curPending, name);
                                if (index != -1) curPending.remove(index);
                                dbReference.child(MainActivity.username).child("pending").setValue(curPending);
                            } else {
                                MainActivity.mainContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.mainContext, "This user could not be found", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child(MainActivity.username).child("addingPending").setValue("1");
                    dbReference.child(MainActivity.username).child("addingPending").removeValue();
                }
            }.start();
        }

    }
    public void removePending(final String name) {
        if(!name.equals("")) {
            new Thread() {
                public void run() {
                    dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            ArrayList<String> curPending = dataSnapshot.child(MainActivity.username).child("pending").getValue(type);
                            int index = in(curPending, name);
                            if (index != -1) curPending.remove(index);
                            dbReference.child(MainActivity.username).child("pending").setValue(curPending);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dbReference.child(MainActivity.username).child("removingPending").setValue("1");
                    dbReference.child(MainActivity.username).child("removingPending").removeValue();
                }
            }.start();
        }
    }
    public int in(ArrayList<String> arr, String check) {
        for(int i = 0; i < arr.size(); i++) {
            if(arr.get(i).equals(check)) {
                return i;
            }
        }
        return -1;
    }
}