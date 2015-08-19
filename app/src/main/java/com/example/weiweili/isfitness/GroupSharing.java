package com.example.weiweili.isfitness;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class GroupSharing extends ActionBarActivity implements View.OnClickListener{
    ListView lvGroupSharing;
    ImageView ivFrinedPicture, ivFrPhoto, ivGroupSharingPhoto,ivGroupSharingHead;
    TextView tvFriendName, tvFriendFeeling;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sharing);
        lvGroupSharing = (ListView) findViewById(R.id.lvGroupSharing);
        ivFrinedPicture = (ImageView) findViewById(R.id.ivFrinedPicture);
        ivFrPhoto = (ImageView) findViewById(R.id.ivFrPhoto);
        ivGroupSharingPhoto = (ImageView) findViewById(R.id.ivGroupSharingPhoto);
        tvFriendName = (TextView) findViewById(R.id.tvFriendName);
        tvFriendFeeling = (TextView) findViewById(R.id.tvFriendFeeling);

        UserLocalStore userLocalStore = new UserLocalStore(this);
        String username = userLocalStore.getLoggedInUser().username;
        Log.d("username", username);
        new DownloadPhoto(username, ivGroupSharingPhoto).execute();
        doSearchFriends(username);

    }

    private void doSearchFriends(String username) {
        ServerRequest serverRequest = new ServerRequest(this);
        Log.d("username", username);
        JSONObject result = serverRequest.fetchGroupSharingInBackground(username);
        Log.d("result", result.toString());
        try{
            ArrayList<FollowedUser> followedUsers = new ArrayList<>();
            int length = result.getJSONArray("followedUser").length();
            for (int i = 0; i < length; i++) {
                String temp = result.getJSONArray("followedUser").getString(i);
                Log.d("followUser", temp);

                followedUsers.add(new FollowedUser(temp));
            }
            //lvGroupSharing.setAdapter(new SearchContentAdapter(this, contents));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_sharing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    private class DownloadPhoto extends AsyncTask<Void, Void, Bitmap> {
        String name;
        ImageView user_image;

        public DownloadPhoto(String name, ImageView user_image) {
            this.name = name;
            this.user_image = user_image;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            String url = SERVER_ADDRESS + "photo/" + name + ".JPG";
            Log.d("url", url);
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(1000 * 30);
                connection.setReadTimeout(1000 * 30);
                return BitmapFactory.decodeStream((InputStream) connection.getContent(), null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                user_image.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public void onClick(View v) {

    }
}

class FollowedUser {
    String username;
    public FollowedUser(String username) {
        this.username = username;
    }
}
