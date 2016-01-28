package com.example.finalproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ParseUser selectedUser = null;
    public static final String USER_KEY = "userKey";
    public static final String MESSAGE_KEY = "messageKey";
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        displayInbox();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_inbox:
                displayInbox();
                return true;
            case R.id.menu_compose:
                // location found
                composeMail();
                return true;
            case R.id.menu_signOut:
                signOut();
                return true;
            case R.id.menu_Albums:
                showAlbums();
                return true;
            case R.id.menu_UpdateProfile:
                updateProfile();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void updateProfile(){
        Intent intent = new Intent(MainActivity.this, UpdateProfileActivity.class);
        startActivity(intent);
    }

    public void displayInbox(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        query.include("User");
        query.whereEqualTo("Recipient", ParseUser.getCurrentUser());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if(objects != null) {
                    InboxAdapter adapter = new InboxAdapter(MainActivity.this, R.layout.inbox_list_view, objects);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ParseObject updateObject = objects.get(position);
                            updateObject.put("IsRead", true);
                            updateObject.saveInBackground();
                            Intent intent = new Intent(MainActivity.this, MessageActivity.class);
                            intent.putExtra(MESSAGE_KEY, objects.get(position).getObjectId());
                            startActivity(intent);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "No messages", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void composeMail(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo("Privacy", "public");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    UserAdapter adapter = new UserAdapter(MainActivity.this, R.layout.user_list_view, objects);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Select a user")
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    selectedUser = objects.get(which);
                                    User user = new User(
                                            selectedUser.getObjectId(),
                                            selectedUser.getUsername(),
                                            selectedUser.getString("FirstName"),
                                            selectedUser.getString("LastName")
                                    );

                                    Intent intent = new Intent(MainActivity.this, ComposeActivity.class);
                                    intent.putExtra(USER_KEY, user);
                                    startActivity(intent);
                                }
                            });

                    final AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Toast.makeText(getApplicationContext(), e.getMessage().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void showAlbums(){
        Intent intent = new Intent(MainActivity.this, AlbumsActivity.class);
        startActivity(intent);
    }

    public void signOut(){
        ParseUser.logOut();
        finish();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayInbox();
    }
}
