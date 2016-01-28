package com.example.finalproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ViewAlbumActivity extends AppCompatActivity {
    String albumKey;
    GridView gridView;
    String albumName;
    public static final int RETRIEVE_IMAGE = 100;
    private Uri selectedImage = null;
    boolean isShared = false;
    Bitmap bitmap;
    public static final String ALBUMIMAGE_KEY = "albumImagekey";
    public static final String SHAREDALBUM_KEY = "sharedAlbumKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_album);
        gridView = (GridView) findViewById(R.id.gridView);
        if(getIntent().getExtras() != null) {
            albumKey = getIntent().getExtras().getString(AlbumsActivity.ALBUM_KEY);
            isShared = getIntent().getExtras().getBoolean(AlbumsActivity.ALBUM_OWNER);
            albumName = getIntent().getExtras().getString(AlbumsActivity.ALBUM_NAME);
            setTitle(albumName);
            displayImages();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_album, menu);
        if(isShared == true){
            menu.findItem(R.id.menu_InviteUser).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.menu_AddImage:
                addImage();
                return true;
            case R.id.menu_ViewSharedUsers:
                // location found
                viewSharedUsers();
                return true;
            case R.id.menu_InviteUser:
                inviteUser();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addImage(){
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

            final ParseObject albumImageObject = new ParseObject("AlbumImage");
            albumImageObject.put("IsShared", isShared);
            albumImageObject.put("Uploader", ParseUser.getCurrentUser());
            if(isShared == true){
                ParseQuery<ParseObject> objectQuery = ParseQuery.getQuery("Albums");
                Log.d("Vignesh", albumKey);
                objectQuery.getInBackground(albumKey, new GetCallback<ParseObject>() {
                    @Override
                    public void done(ParseObject object, ParseException e) {
                        Log.d("Vignesh", object.getObjectId());
                        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                        pushQuery.whereEqualTo("User", object.get("Owner"));
                        ParsePush push = new ParsePush();
                        push.setQuery(pushQuery);
                        push.setMessage(ParseUser.getCurrentUser().getString("FirstName") + " " +
                                ParseUser.getCurrentUser().getString("LastName") + " has uploaded an image to your album");
                        push.sendInBackground();
                    }
                });

                Toast.makeText(getApplicationContext(), "The image will be visible once the owner of this album verifies it", Toast.LENGTH_SHORT).show();
            }

            albumImageObject.put("AlbumKey", albumKey);
            byte[] imageData = getBytesFromBitmap(bitmap);
            final ParseFile file = new ParseFile("image.jpg", imageData);
            file.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        albumImageObject.put("Image", file);
                        albumImageObject.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                displayImages();
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] getBytesFromBitmap(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    public void viewSharedUsers(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("SharedAlbums");
        query.whereEqualTo("AlbumKey", albumKey);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                List<ParseUser> users = new ArrayList<ParseUser>();
                for(ParseObject object : objects){

                }
            }
        });
    }

    public void inviteUser(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("objectId", ParseUser.getCurrentUser().getObjectId());
        query.whereEqualTo("Privacy", "public");
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> objects, ParseException e) {
                if (e == null) {
                    UserAdapter adapter = new UserAdapter(ViewAlbumActivity.this, R.layout.user_list_view, objects);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ViewAlbumActivity.this);
                    builder.setTitle("Select a user")
                            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final ParseObject shareAlbum = new ParseObject("SharedAlbums");
                                    shareAlbum.put("Owner", ParseUser.getCurrentUser());
                                    shareAlbum.put("AlbumKey", albumKey);
                                    shareAlbum.put("SharedUser", objects.get(which));
                                    ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                                    pushQuery.whereEqualTo("User", objects.get(which));
                                    ParsePush push = new ParsePush();
                                    push.setQuery(pushQuery);
                                    push.setMessage(ParseUser.getCurrentUser().getString("FirstName") + " " +
                                            ParseUser.getCurrentUser().getString("LastName") + " has shared an album with you");
                                    push.sendInBackground();
                                    shareAlbum.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            Toast.makeText(getApplicationContext(), "User invited!", Toast.LENGTH_SHORT).show();
                                        }
                                    });
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

    public void displayImages(){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("AlbumImage");
        query.whereEqualTo("AlbumKey", albumKey);
        if(isShared == true){
            query.whereEqualTo("IsShared", false);
        }

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(final List<ParseObject> objects, ParseException e) {
                if(objects != null) {
                    ImageAdapter adapter = new ImageAdapter(ViewAlbumActivity.this, R.layout.view_album_list_view, objects);
                    gridView.setAdapter(adapter);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(ViewAlbumActivity.this, ViewImageActivity.class);
                            intent.putExtra(ALBUMIMAGE_KEY, objects.get(position).getObjectId());
                            intent.putExtra(SHAREDALBUM_KEY, isShared);
                            startActivity(intent);
                        }
                    });
                }
                else{
                    Toast.makeText(getApplicationContext(), "No images available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        displayImages();
    }
}
