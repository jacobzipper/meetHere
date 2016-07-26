package com.jacobzipper.meetHere;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import java.util.ArrayList;
public class CustomAdapter extends ArrayAdapter<String> {
    ArrayList<String> items;
    public CustomAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CustomAdapter(Context context, int resource, ArrayList<String> items) {
        super(context, resource, items);
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.text_item, null);
        }

        ((TextView)v.findViewById(R.id.textView)).setText(items.get(position));

        return v;
    }


}