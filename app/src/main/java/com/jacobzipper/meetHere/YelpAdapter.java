package com.jacobzipper.meetHere;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
public class YelpAdapter extends ArrayAdapter<String> {
    ArrayList<String> names;
    ArrayList<Boolean> isOpens;
    ArrayList<String> phones;
    ArrayList<String> urls;
    ArrayList<String> addresses;
    public YelpAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public YelpAdapter(Context context, int resource, ArrayList<String> names,ArrayList<Boolean> isOpens,ArrayList<String> phones,ArrayList<String> urls,ArrayList<String> addresses) {
        super(context, resource, names);
        this.names = names;
        this.isOpens = isOpens;
        this.phones = phones;
        this.urls = urls;
        this.addresses = addresses;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.yelp_item, null);
        }

        ((TextView)v.findViewById(R.id.bizName)).setText(names.get(position));
        ((TextView)v.findViewById(R.id.phone)).setText(phones.get(position));
        ((TextView)v.findViewById(R.id.address)).setText(addresses.get(position));
        if(!isOpens.get(position).booleanValue()) {
            ((TextView)v.findViewById(R.id.openText)).setText("Open!");
        }
        else {
            ((TextView)v.findViewById(R.id.openText)).setText("Closed");
        }
        v.findViewById(R.id.webButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse(urls.get(position));

                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                MainActivity.mainContext.startActivity(intent);
            }
        });
        final View vv = v;
        v.findViewById(R.id.mapButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = ((TextView) vv.findViewById(R.id.address)).getText().toString();
                Uri uri = Uri.parse("geo:0,0?q="+address.replace(' ','+'));
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                MainActivity.mainContext.startActivity(intent);
            }
        });
        return v;
    }





}