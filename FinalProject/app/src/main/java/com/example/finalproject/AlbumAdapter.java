package com.example.finalproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

/**
 * Created by Vignesh on 12/15/2015.
 */
public class AlbumAdapter extends ArrayAdapter<ParseObject> {
    Context mContext;
    int layoutResourceId;
    List<ParseObject> lists;

    public AlbumAdapter(Context mContext, int layoutResourceId, List<ParseObject> lists){
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

        final ParseObject cList = lists.get(position);
        TextView txtAlbumName = (TextView) convertView.findViewById(R.id.lblAlbumName);
        txtAlbumName.setText(cList.getString("Name"));

        final CheckBox chkPrivacy = (CheckBox) convertView.findViewById(R.id.chkPrivacy);
        chkPrivacy.setChecked(cList.getBoolean("Private"));
        chkPrivacy.setFocusable(false);
        chkPrivacy.setFocusableInTouchMode(false);
        chkPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cList.put("Private", chkPrivacy.isChecked());
                ((AlbumsActivity) mContext).showProgress();
                cList.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        ((AlbumsActivity) mContext).dismissProgress();
                    }
                });
            }
        });

        ImageButton imageButton = (ImageButton) convertView.findViewById(R.id.imageButton);
        Bitmap bitmap = BitmapFactory.decodeResource(convertView.getResources(), R.drawable.trashcan);
        imageButton.setFocusable(false);
        imageButton.setFocusableInTouchMode(false);
        imageButton.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 50, 50, false));
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AlbumsActivity) mContext).showProgress();
                cList.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        ((AlbumsActivity) mContext).dismissProgress();
                    }
                });
            }
        });

        if(cList.get("Owner") != ParseUser.getCurrentUser()){
            chkPrivacy.setVisibility(View.INVISIBLE);
            imageButton.setVisibility(View.INVISIBLE);
        }
        else{
            chkPrivacy.setVisibility(View.VISIBLE);
            imageButton.setVisibility(View.VISIBLE);
        }

        return convertView;
    }
}
