/*Vignesh Karunanithi*/

package com.example.finalproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.LogInCallback;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseTwitterUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    Button btnNewAccount;
    Button btnLogin;
    EditText txtEmail;
    EditText txtPassword;
    ArrayList<String> permissions;
    Button btnFacebookLogin;
    Button btnTwitterLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        permissions = new ArrayList<>();
        permissions.add("public_profile");
        permissions.add("email");
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
        }

        txtEmail = (EditText) findViewById(R.id.txtLastName);
        txtPassword = (EditText) findViewById(R.id.txtPassword);
        btnNewAccount = (Button)findViewById(R.id.btnNewAccount);
        btnNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        btnFacebookLogin = (Button) findViewById(R.id.btnFacebookLogin);
        btnFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseFacebookUtils.logInWithReadPermissionsInBackground(LoginActivity.this, permissions, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, com.parse.ParseException e) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Facebook login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Facebook!");
                            getFacebookUserDetails(true, user);
                        } else {
                            Log.d("MyApp", "User logged in through Facebook!");
                            getFacebookUserDetails(false, user);
                        }
                    }
                });
            }
        });

        btnTwitterLogin = (Button) findViewById(R.id.btnTwitterLogin);
        btnTwitterLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseTwitterUtils.logIn(LoginActivity.this, new LogInCallback() {
                    @Override
                    public void done(ParseUser user, com.parse.ParseException e) {
                        if (user == null) {
                            Log.d("MyApp", "Uh oh. The user cancelled the Twitter login.");
                        } else if (user.isNew()) {
                            Log.d("MyApp", "User signed up and logged in through Twitter!");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        } else {
                            Log.d("MyApp", "User logged in through Twitter!");
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtEmail.getText().length() == 0 || txtPassword.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Either Username or Password is empty", Toast.LENGTH_SHORT).show();
                } else {
                    ParseUser.logInInBackground(txtEmail.getText().toString(), txtPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, com.parse.ParseException e) {
                            if (user != null) {
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), "Incorrect Username or Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
    }

    public void getFacebookUserDetails(final boolean firstTime, final ParseUser user){
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {
                        try{
                            Log.d("Vignesh", jsonObject.toString());
                            final String firstName = jsonObject.getString("first_name");
                            final String lastName = jsonObject.getString("last_name");
                            final String email = jsonObject.getString("email");
                            final String gender = jsonObject.getString("gender");
                            linkFBUsers(firstName, lastName, email, gender, firstTime, user);
                        }
                        catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "name, id, email, gender, first_name, last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void linkFBUsers(final String fName, final String lName, final String emailId, final String userGender, final boolean firstTime, final ParseUser user){
        if(firstTime){
            ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
            ParsePush push = new ParsePush();
            push.setQuery(pushQuery);
            push.setMessage(fName + " " + lName + " has registered newly!");
            push.sendInBackground();

            if(!ParseFacebookUtils.isLinked(user)){
                ParseFacebookUtils.linkWithReadPermissionsInBackground(user, LoginActivity.this, permissions, new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if(ParseFacebookUtils.isLinked(user)){
                            user.setUsername(emailId);
                            user.setEmail(emailId);
                            user.put("FirstName", fName);
                            user.put("LastName", lName);
                            user.put("Gender", userGender);

                            user.signUpInBackground(new SignUpCallback() {
                                @Override
                                public void done(com.parse.ParseException e) {
                                    if(e == null){
                                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                        installation.put("User", ParseUser.getCurrentUser());
                                        installation.saveInBackground();
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    }
                });
            }
            else{
                user.setUsername(emailId);
                user.setEmail(emailId);
                user.put("FirstName", fName);
                user.put("LastName", lName);
                user.put("Gender", userGender);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put("User", ParseUser.getCurrentUser());
                        installation.saveInBackground();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }
        else{
            if(!ParseFacebookUtils.isLinked(user)){
                ParseFacebookUtils.linkWithReadPermissionsInBackground(user, LoginActivity.this, permissions, new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if(ParseFacebookUtils.isLinked(user)){
                            user.setUsername(emailId);
                            user.setEmail(emailId);
                            user.put("FirstName", fName);
                            user.put("LastName", lName);
                            user.put("Gender", userGender);
                            user.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(com.parse.ParseException e) {
                                    ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                                    installation.put("User", ParseUser.getCurrentUser());
                                    installation.saveInBackground();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }
            else{
                user.setUsername(emailId);
                user.setEmail(emailId);
                user.put("FirstName", fName);
                user.put("LastName", lName);
                user.put("Gender", userGender);
                user.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
                        installation.put("User", ParseUser.getCurrentUser());
                        installation.saveInBackground();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
