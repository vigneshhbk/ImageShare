package com.example.finalproject;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Vignesh on 12/15/2015.
 */
public class InboxAdapter extends ArrayAdapter<ParseObject> {
    Context mContext;
    int layoutResourceId;
    List<ParseObject> lists;

    public InboxAdapter(Context mContext, int layoutResourceId, List<ParseObject> lists){
        super(mContext, layoutResourceId, lists);
        this.mContext = mContext;
        this.layoutResourceId = layoutResourceId;
        this.lists = lists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        final TextView txtSender = (TextView) convertView.findViewById(R.id.txtSender);
        final ParseObject cList = lists.get(position);
        if(cList.getBoolean("IsRead") == false){
            txtSender.setTypeface(null, Typeface.BOLD);
        }
        else{
            txtSender.setTypeface(null, Typeface.NORMAL);
        }

        ParseObject user = cList.getParseObject("Sender");
        user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                txtSender.setText(object.getString("username"));
            }
        });

        TextView txtTime = (TextView) convertView.findViewById(R.id.txtTime);
        txtTime.setText(cList.getCreatedAt().toString());

        return convertView;
    }
}
