package com.jacobzipper.meetHere;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FriendsFragment extends Fragment {
    final DatabaseReference dbReference = FirebaseDatabase.getInstance().getReference("users");
    View fragView;
    String curName="";
    View lastView = null;
    boolean dismissButton = false;
    GenericTypeIndicator<ArrayList<String>> type = new GenericTypeIndicator<ArrayList<String>>() {};
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragView = inflater.inflate(R.layout.fragment_friends, container, false);
        dbReference.child(MainActivity.username).child("friends").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                MainActivity.mainContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFriends();
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        updateFriends();
        doListeners();
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
        ((ListView) fragView.findViewById(R.id.friendsList)).setAdapter(adapter);
    }
    public void addFriend() {
        new Thread() {
            public void run() {
                final String addstr = curName;
                if(in(MainActivity.friends,addstr)==-1 && !addstr.equals(MainActivity.username) && !addstr.equals("")) {
                    dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(addstr)) {
                                if(dataSnapshot.child(addstr).hasChild("pending")) {
                                    ArrayList<String> theirPending = dataSnapshot.child(addstr).child("pending").getValue(type);
                                    if(in(theirPending,MainActivity.username)==-1) {
                                        theirPending.add(MainActivity.username);
                                        dbReference.child(addstr).child("pending").setValue(theirPending);
                                        MainActivity.mainContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.mainContext, "Friend added!", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        MainActivity.mainContext.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(MainActivity.mainContext,"You are still in this user's pending requests",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                                else {
                                    ArrayList<String> theirPending = new ArrayList<String>();
                                    theirPending.add(MainActivity.username);
                                    dbReference.child(addstr).child("pending").setValue(theirPending);
                                }
                            }
                            else {
                                MainActivity.mainContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.mainContext,"Could not find username",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                else {
                    MainActivity.mainContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!addstr.equals("")) {
                                Toast.makeText(MainActivity.mainContext, "You are already friends", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(MainActivity.mainContext, "Please type a username to add", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                dbReference.child(MainActivity.username).child("addingFriend").setValue("1");
                dbReference.child(MainActivity.username).child("addingFriend").removeValue();
            }
        }.start();

    }
    public void subFriend() {
        new Thread() {
            public void run() {
                final String substr = curName;
                dbReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int index = in(MainActivity.friends,substr);
                        if(index!=-1) {
                            MainActivity.friends.remove(index);
                            dbReference.child(MainActivity.username).child("friends").setValue(MainActivity.friends);
                            ArrayList<String> theirFriends = dataSnapshot.child(substr).child("friends").getValue(type);
                            theirFriends.remove(MainActivity.username);
                            dbReference.child(substr).child("friends").setValue(theirFriends);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                dbReference.child(MainActivity.username).child("removingFriend").setValue("1");
                dbReference.child(MainActivity.username).child("removingFriend").removeValue();
            }
        }.start();

    }
    public void doListeners() {
        fragView.findViewById(R.id.addFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addFriend();
            }
        });
        fragView.findViewById(R.id.subFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subFriend();
            }
        });
        ((ListView)fragView.findViewById(R.id.friendsList)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                curName = "";
                if(lastView!=null) {
                    lastView.setBackgroundColor(Color.rgb(240,240,240));
                }
                if(view!=lastView) {
                    view.setBackgroundColor(Color.argb(100, 0, 200, 0));
                    curName = MainActivity.friends.get(i);
                }
                lastView = view;
            }
        });
        fragView.findViewById(R.id.addFriends).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragView.findViewById(R.id.friendsLayout).setBackgroundColor(Color.rgb(60, 60, 60));
                MainActivity.mainContext.findViewById(R.id.menu).setAlpha(.1f);
                MainActivity.mainContext.findViewById(R.id.menu).setClickable(false);
                fragView.findViewById(R.id.addFriends).setAlpha(.1f);
                fragView.findViewById(R.id.addFriends).setClickable(false);
                fragView.findViewById(R.id.subFriends).setAlpha(.1f);
                fragView.findViewById(R.id.subFriends).setClickable(false);
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View tempView = layoutInflater.inflate(R.layout.add_friends_popup, null);
                final PopupWindow window = new PopupWindow(tempView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                window.setFocusable(true);
                window.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        if (!dismissButton) {
                            window.setFocusable(true);
                            window.showAtLocation(tempView, Gravity.CENTER, 0, 0);
                        }
                        dismissButton = false;
                    }
                });
                window.showAtLocation(tempView, Gravity.CENTER, 0, 0);
                tempView.findViewById(R.id.popupCancelFriend).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        fragView.findViewById(R.id.friendsLayout).setBackgroundColor(Color.rgb(255, 255, 255));
                        MainActivity.mainContext.findViewById(R.id.menu).setAlpha(1f);
                        MainActivity.mainContext.findViewById(R.id.menu).setClickable(true);
                        fragView.findViewById(R.id.addFriends).setAlpha(1f);
                        fragView.findViewById(R.id.addFriends).setClickable(true);
                        fragView.findViewById(R.id.subFriends).setAlpha(1f);
                        fragView.findViewById(R.id.subFriends).setClickable(true);
                        dismissButton = true;
                        window.dismiss();
                    }
                });
                tempView.findViewById(R.id.popupAddFriendButton).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        curName = ((EditText)tempView.findViewById(R.id.popupAddFriend)).getText().toString();
                        addFriend();
                        fragView.findViewById(R.id.friendsLayout).setBackgroundColor(Color.rgb(255, 255, 255));
                        MainActivity.mainContext.findViewById(R.id.menu).setAlpha(1f);
                        MainActivity.mainContext.findViewById(R.id.menu).setClickable(true);
                        fragView.findViewById(R.id.addFriends).setAlpha(1f);
                        fragView.findViewById(R.id.addFriends).setClickable(true);
                        fragView.findViewById(R.id.subFriends).setAlpha(1f);
                        fragView.findViewById(R.id.subFriends).setClickable(true);
                        dismissButton = true;
                        window.dismiss();
                    }
                });
            }
        });
    }
}