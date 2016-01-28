package com.example.finalproject;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

public class AddAlbumActivity extends AppCompatActivity {
    EditText txtAlbumName;
    CheckBox chkPrivacy;
    Button btnAddAlbum;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_album);
        txtAlbumName = (EditText) findViewById(R.id.txtAlbumName);
        chkPrivacy = (CheckBox) findViewById(R.id.chkPrivacy);
        btnAddAlbum = (Button) findViewById(R.id.btnAddAlbum);

        btnAddAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseObject album = new ParseObject("Albums");
                album.put("Owner", ParseUser.getCurrentUser());
                album.put("Name", txtAlbumName.getText().toString());
                album.put("Private", chkPrivacy.isChecked());
                album.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        finish();
                    }
                });
            }
        });
    }
}
