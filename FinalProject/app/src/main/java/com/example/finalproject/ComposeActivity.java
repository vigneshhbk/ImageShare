package com.example.finalproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class ComposeActivity extends AppCompatActivity {
    public static final int RETRIEVE_IMAGE = 100;
    private Uri selectedImage = null;
    Bitmap bitmap;
    ImageView imageView;
    private User user;
    Button btnSend;
    EditText txtMessage;
    TextView txtRecipient;
    ParseUser recipientUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        imageView = (ImageView) findViewById(R.id.imageView);
        btnSend = (Button) findViewById(R.id.btnReply);
        txtMessage = (EditText) findViewById(R.id.txtMessage);
        txtRecipient = (TextView) findViewById(R.id.txtRecipient);
        final ProgressDialog progressDialog;

        if(getIntent().getExtras() != null){
            user = getIntent().getExtras().getParcelable(MainActivity.USER_KEY);
            txtRecipient.setText("To: " + user.getUserName());
            progressDialog = new ProgressDialog(ComposeActivity.this);
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
            ParseQuery<ParseUser> parseUser = ParseUser.getQuery();
            parseUser.getInBackground(user.getUserId(), new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser object, ParseException e) {
                    recipientUser = object;
                    progressDialog.dismiss();
                }
            });
        }

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ParseObject message = new ParseObject("Message");
                message.put("Sender", ParseUser.getCurrentUser());
                message.put("Message", txtMessage.getText().toString());
                message.put("Recipient", recipientUser);
                ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                pushQuery.whereEqualTo("User", recipientUser);
                ParsePush push = new ParsePush();
                push.setQuery(pushQuery);
                push.setMessage(ParseUser.getCurrentUser().getString("FirstName") + " " +
                        ParseUser.getCurrentUser().getString("LastName") + " has sent you a message");
                push.sendInBackground();

                message.put("IsRead", false);

                if(bitmap != null){
                    byte[] imageData = new SignUpActivity().getBytesFromBitmap(bitmap);
                    final ParseFile file = new ParseFile("message.jpg", imageData);
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            message.put("Image", file);
                            message.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        Toast.makeText(getApplicationContext(), "Message sent successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        Intent intent = new Intent(ComposeActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    });
                }
                else{
                    message.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(getApplicationContext(), "Message sent successfully!", Toast.LENGTH_SHORT).show();
                                finish();
                                Intent intent = new Intent(ComposeActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_compose, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_attachImage:
                attachImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void attachImage(){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RETRIEVE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == RETRIEVE_IMAGE && resultCode == RESULT_OK && data != null){
            selectedImage = data.getData();
            bitmap = null;
            try{
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }

            imageView.setImageURI(selectedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
