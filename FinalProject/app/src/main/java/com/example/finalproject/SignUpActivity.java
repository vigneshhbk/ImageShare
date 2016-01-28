package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;

public class SignUpActivity extends AppCompatActivity {
    Button btnCancel;
    Button btnSignUp;
    EditText txtFName;
    EditText txtLName;
    EditText txtPassword;
    EditText txtConfirmPassword;
    EditText txtEmail;
    CheckBox chkPrivate;
    private ImageButton imgAvatar;
    public static final int RETRIEVE_IMAGE = 100;
    private Uri selectedImage = null;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnSignUp = (Button) findViewById(R.id.btnUpdate);
        txtFName = (EditText) findViewById(R.id.txtFName);
        txtLName = (EditText) findViewById(R.id.txtLastName);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        txtConfirmPassword = (EditText) findViewById(R.id.txtConfirmPassword);
        imgAvatar = (ImageButton) findViewById(R.id.imgBtnAvatar);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        chkPrivate = (CheckBox) findViewById(R.id.chkPrivacy);
        bitmap = null;

        imgAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RETRIEVE_IMAGE);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtFName.getText().length() == 0 || txtLName.getText().length() == 0 || txtPassword.getText().length() == 0 || txtConfirmPassword.getText().length() == 0
                         || ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == -1 || bitmap == null
                        || txtEmail.getText().length() == 0){
                    Toast.makeText(getApplicationContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show();
                }
                else if(txtPassword.getText().toString().equals(txtConfirmPassword.getText().toString()) == false){
                    Toast.makeText(getApplicationContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
                else{
                    RadioButton gender = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId());
                    final ParseUser user = new ParseUser();
                    final String fName = txtFName.getText().toString();
                    final String lName = txtLName.getText().toString();
                    user.setUsername(fName + " " + lName);
                    user.setPassword(txtPassword.getText().toString());
                    user.put("FirstName", fName);
                    user.put("LastName", lName);
                    user.put("Gender", gender.getTag().toString());
                    user.setEmail(txtEmail.getText().toString());
                    if(chkPrivate.isChecked() == true){
                        user.put("Privacy", "private");
                    }
                    else{
                        user.put("Privacy", "public");
                    }

                    byte[] imageData = getBytesFromBitmap(bitmap);
                    final ParseFile file = new ParseFile("avatar.jpg", imageData);
                    file.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null) {
                                user.put("Avatar", file);
                                user.signUpInBackground(new SignUpCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if (e == null) {
                                            Toast.makeText(getApplicationContext(), "Successfully logged in!", Toast.LENGTH_SHORT).show();
                                            ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                                            ParsePush push = new ParsePush();
                                            push.setQuery(pushQuery);
                                            push.setMessage(fName + " " + lName + " has registered newly!");
                                            push.sendInBackground();

                                            ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                            installation.put("User", ParseUser.getCurrentUser());
                                            installation.saveInBackground();
                                            finish();
                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                            }
                            else{
                                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
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

            imgAvatar.setImageURI(selectedImage);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }
}
