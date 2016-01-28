package com.example.finalproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.ParseUser;

import java.util.List;

/**
 * Created by Vignesh on 12/13/2015.
 */
public class UserAdapter extends ArrayAdapter<ParseUser> {
    Context mContext;
    int layoutResourceId;
    List<ParseUser> users = null;

    public UserAdapter(Context mContext, int layoutResourceId, List<ParseUser> users){
        super(mContext, layoutResourceId, users);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        ParseUser user = users.get(position);
        TextView textViewItem = (TextView) convertView.findViewById(R.id.txtUserName);
        textViewItem.setText(user.getUsername());
        return convertView;
    }
}
