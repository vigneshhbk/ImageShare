package com.example.finalproject;

import android.app.ProgressDialog;
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

import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AlbumsActivity extends AppCompatActivity {
    ListView listView;
    ProgressDialog progressDialog;
    public static final String ALBUM_KEY = "albumKey";
    public static final String ALBUM_OWNER = "albumOwner";
    public static final String ALBUM_NAME = "albumName";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_albums);
        listView = (ListView) findViewById(R.id.listView);
        displayAlbums();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_albums, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_addAlbum) {
            addAlbum();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayAlbums(){
        ParseQuery<ParseObject> sharedAlbumQuery = ParseQuery.getQuery("SharedAlbums");
        sharedAlbumQuery.whereEqualTo("SharedUser", ParseUser.getCurrentUser());
        ParseQuery<ParseObject> sharedQuery = ParseQuery.getQuery("Albums");
        sharedQuery.whereMatchesKeyInQuery("objectId", "AlbumKey", sharedAlbumQuery);

        ParseQuery<ParseObject> anotherQuery = ParseQuery.getQuery("Albums");
        anotherQuery.whereEqualTo("Owner", ParseUser.getCurrentUser());

        ParseQuery<ParseObject> subQuery = ParseQuery.getQuery("Albums");
        subQuery.whereEqualTo("Private", false);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(sharedQuery);
        queries.add(anotherQuery);
        queries.add(subQuery);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.include("User");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if (objects != null) {
                    AlbumAdapter adapter = new AlbumAdapter(AlbumsActivity.this, R.layout.album_list_view, objects);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            final int finalPosition = position;
                            ParseObject user = objects.get(position).getParseObject("Owner");
                            user.fetchIfNeededInBackground(new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject object, ParseException e) {
                                    boolean isShared = false;
                                    if(object.getObjectId() == ParseUser.getCurrentUser().getObjectId()){
                                        isShared = false;
                                    }
                                    else{
                                        isShared = true;
                                    }

                                    Intent intent = new Intent(AlbumsActivity.this, ViewAlbumActivity.class);
                                    intent.putExtra(ALBUM_KEY, objects.get(finalPosition).getObjectId());
                                    intent.putExtra(ALBUM_OWNER, isShared);
                                    intent.putExtra(ALBUM_NAME, objects.get(finalPosition).getString("Name"));
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "No Albums", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void addAlbum(){
        Intent intent = new Intent(AlbumsActivity.this, AddAlbumActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayAlbums();
    }

    public void showProgress(){
        progressDialog = new ProgressDialog(AlbumsActivity.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.show();
    }

    public void dismissProgress(){
        progressDialog.dismiss();
        displayAlbums();
    }
}
