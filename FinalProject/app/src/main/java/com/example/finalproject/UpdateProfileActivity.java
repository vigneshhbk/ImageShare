package com.example.finalproject;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;

public class UpdateProfileActivity extends AppCompatActivity {
    Button btnCancel;
    Button btnUpdate;
    EditText txtFName;
    EditText txtLName;
    EditText txtEmail;
    CheckBox chkPrivate;
    private ImageButton imgAvatar;
    public static final int RETRIEVE_IMAGE = 100;
    private Uri selectedImage = null;
    Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        ParseUser currentUser = ParseUser.getCurrentUser();
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        txtFName = (EditText) findViewById(R.id.txtFName);
        txtFName.setText(currentUser.getString("FirstName"));
        txtLName = (EditText) findViewById(R.id.txtLastName);
        txtLName.setText(currentUser.getString("LastName"));
        Log.d("Vignesh", currentUser.getString("Gender"));
        if(currentUser.getString("Gender").equalsIgnoreCase("male")){
            ((RadioButton) findViewById(R.id.rbMale)).setChecked(true);
        }
        else{
            ((RadioButton) findViewById(R.id.rbFemale)).setChecked(true);
        }

        imgAvatar = (ImageButton) findViewById(R.id.imgBtnAvatar);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setText(currentUser.getEmail());
        chkPrivate = (CheckBox) findViewById(R.id.chkPrivacy);
        if(currentUser.getString("Privacy").equals("private")){
            chkPrivate.setChecked(true);
        }

        bitmap = null;
        ParseFile imageFile = (ParseFile) currentUser.get("Avatar");
        if(imageFile != null) {
            imageFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] data, ParseException e) {
                    bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    imgAvatar.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 125, 125, false));
                }
            });
        }

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

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtFName.getText().length() == 0 || txtLName.getText().length() == 0
                        || ((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId() == -1 || bitmap == null
                        || txtEmail.getText().length() == 0){
                    Toast.makeText(getApplicationContext(), "All fields are mandatory", Toast.LENGTH_SHORT).show();
                }
                else{
                    RadioButton gender = (RadioButton) findViewById(((RadioGroup) findViewById(R.id.radioGroup)).getCheckedRadioButtonId());
                    final ParseUser user = ParseUser.getCurrentUser();
                    final String fName = txtFName.getText().toString();
                    final String lName = txtLName.getText().toString();
                    user.setUsername(fName + " " + lName);
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
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Toast.makeText(getApplicationContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                                        finish();
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
