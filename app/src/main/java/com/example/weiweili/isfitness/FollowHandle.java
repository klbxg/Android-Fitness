package com.example.weiweili.isfitness;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class FollowHandle extends AppCompatActivity {
    ImageView userImage;
    TextView selectedUserName;
    ImageButton addFriend;
    Intent intent;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_handle);
        userImage = (ImageView) findViewById(R.id.search_result_userImage);
        selectedUserName = (TextView) findViewById(R.id.search_result_username);
        addFriend = (ImageButton) findViewById(R.id.add_friend);
        intent = this.getIntent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        String userNow = intent.getStringExtra("userNow");
        String userSelected = intent.getStringExtra("userSelect");
        ServerRequest serverRequest = new ServerRequest(this);

        selectedUserName.setText(userSelected);
        new DownloadImage(userSelected, userImage).execute();
        boolean followed = serverRequest.checkFollowedInBackground(userNow, userSelected);
        if (!followed) {
            Log.d("Followed", "False");
            addFriend.setImageResource(R.drawable.add_friend);
            addFriend.setOnClickListener(new FollowClickListener(userSelected, userNow, this, addFriend));
        } else {
            Log.d("Followed", "True");
            addFriend.setImageResource(R.drawable.delete_friend);
            addFriend.setOnClickListener(new UnFollowClickListener(userSelected, userNow, this, addFriend));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_follow_handle, menu);
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

    // this class is used to download picture for the selectUser
    private class DownloadImage extends AsyncTask<Void, Void, Bitmap> {
        String name;
        ImageView user_image;

        public DownloadImage(String name, ImageView user_image) {
            this.name = name;
            this.user_image = user_image;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            String url = SERVER_ADDRESS + "photo/" + name + ".JPG";
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
}

// this is the follow button click listener
class FollowClickListener implements View.OnClickListener {
    String userSelected;
    String userNow;
    ImageButton addFriend;
    Context context;

    public FollowClickListener(String userSelected, String userNow, Context context, ImageButton addFriend) {
        this.userSelected = userSelected;
        this.userNow = userNow;
        this.context = context;
        this.addFriend = addFriend;
    }

    @Override
    public void onClick(View v) {
        // update the database to follow
        ServerRequest serverRequest = new ServerRequest(context);
        serverRequest.addFollowInBackground(userNow, userSelected, new AddFollowCallBack() {
            @Override
            public void done() {
                addFriend.setImageResource(R.drawable.delete_friend);
                addFriend.setOnClickListener(new UnFollowClickListener(userSelected, userNow, context, addFriend));
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Follow " + userSelected + " Success", duration);
                toast.show();
            }
        });
    }
}

// this is the follow button click listener
class UnFollowClickListener implements View.OnClickListener {
    String userSelected;
    String userNow;
    ImageButton addFriend;
    Context context;

    public UnFollowClickListener(String userSelected, String userNow, Context context, ImageButton addFriend) {
        this.userSelected = userSelected;
        this.userNow = userNow;
        this.context = context;
        this.addFriend = addFriend;
    }

    @Override
    public void onClick(View v) {
        // update the database to follow
        ServerRequest serverRequest = new ServerRequest(context);
        serverRequest.unFollowInBackground(userNow, userSelected, new UnFollowCallBack() {
            @Override
            public void done() {
                addFriend.setImageResource(R.drawable.add_friend);
                addFriend.setOnClickListener(new FollowClickListener(userSelected, userNow, context, addFriend));
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "UnFollow " + userSelected + " Success", duration);
                toast.show();
            }
        });
    }
}