package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ViewImageActivity extends AppCompatActivity {
    String albumImageKey;
    ImageView imageView;
    Button btnDelete;
    Button btnVerify;
    boolean sharedAlbumKey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        btnVerify = (Button) findViewById(R.id.btnVerify);
        if(getIntent().getExtras() != null) {
            albumImageKey = getIntent().getExtras().getString(ViewAlbumActivity.ALBUMIMAGE_KEY);
            sharedAlbumKey = getIntent().getExtras().getBoolean(ViewAlbumActivity.SHAREDALBUM_KEY);
            if(sharedAlbumKey == true){
                btnDelete.setVisibility(View.INVISIBLE);
            }

            displayImage();
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("AlbumImage");
                query.getInBackground(albumImageKey, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        object.deleteInBackground(new DeleteCallback() {
                            @Override
                            public void done(ParseException e) {
                                finish();
                            }
                        });
                    }
                });
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("AlbumImage");
                query.getInBackground(albumImageKey, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        object.put("IsShared", false);
                        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                        pushQuery.whereEqualTo("User", object.get("Uploader"));
                        ParsePush push = new ParsePush();
                        push.setQuery(pushQuery);
                        push.setMessage(ParseUser.getCurrentUser().getString("FirstName") + " " +
                                ParseUser.getCurrentUser().getString("LastName") + " has verified your image");
                        push.sendInBackground();
                        object.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Toast.makeText(getApplicationContext(), "Image verified!", Toast.LENGTH_SHORT).show();
                                btnVerify.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                });
            }
        });
    }

    public void displayImage(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AlbumImage");
        query.getInBackground(albumImageKey, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(object.getBoolean("IsShared") == false){
                    btnVerify.setVisibility(View.INVISIBLE);
                }

                ParseFile imageFile = (ParseFile) object.get("Image");
                imageFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] data, ParseException e) {
                        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                        imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 350, 350, false));
                    }
                });
            }
        });
    }
}
