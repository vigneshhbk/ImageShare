package com.example.finalproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;

import java.util.List;

/**
 * Created by Vignesh on 12/15/2015.
 */
public class ImageAdapter extends ArrayAdapter<ParseObject> {
    Context mContext;
    int layoutResourceId;
    List<ParseObject> lists;

    public ImageAdapter(Context mContext, int layoutResourceId, List<ParseObject> lists){
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

        final ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        final ParseObject cList = lists.get(position);
        if (cList.get("Image") != null) {
            ParseFile imageFile = (ParseFile) cList.get("Image");
            imageFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 100, 100, false));
                }
            });
        }

        return convertView;
    }
}
