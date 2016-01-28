package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class MessageActivity extends AppCompatActivity {
    String messageKey;
    TextView lblMessage;
    ImageView imageView;
    Button btnReply;
    Button btnDelete;
    TextView lblSender;
    ParseObject messageObject;
    User messageUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        lblSender = (TextView) findViewById(R.id.lblSender);
        lblMessage = (TextView) findViewById(R.id.lblMessage);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnReply = (Button) findViewById(R.id.btnReply);
        btnDelete = (Button) findViewById(R.id.btnDelete);
        if(getIntent().getExtras() != null) {
            messageKey = getIntent().getExtras().getString(MainActivity.MESSAGE_KEY);
        }

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.include("User");
        query.getInBackground(messageKey, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                messageObject = object;
                lblMessage.setText(object.getString("Message"));
                ParseObject user = object.getParseObject("Sender");
                user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        messageUser = new User(
                                object.getObjectId(),
                                object.getString("username"),
                                object.getString("FirstName"),
                                object.getString("LastName")
                        );
                        lblSender.setText(object.getString("username"));
                    }
                });

                if (object.get("Image") != null) {
                    ParseFile avatarFile = (ParseFile) object.get("Image");
                    avatarFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException e) {
                            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                            imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 150, 150, false));
                        }
                    });
                }
            }
        });

        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MessageActivity.this, ComposeActivity.class);
                intent.putExtra(MainActivity.USER_KEY, messageUser);
                startActivity(intent);
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageObject.deleteInBackground(new DeleteCallback() {
                    @Override
                    public void done(ParseException e) {
                        finish();
                    }
                });
            }
        });
    }
}
