package com.example.weiweili.isfitness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import com.example.weiweili.isfitness.XListView.IXListViewListener;


public class GroupSharing extends ActionBarActivity implements IXListViewListener {
    XListView lvGroupSharing;
    ImageView ivFrinedPicture, ivFrPhoto, ivGroupSharingPhoto,ivGroupSharingHead;
    TextView tvFriendName, tvFriendFeeling;
    GroupSharingAdapter myAdapter;
    private Handler mHandler;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
    UserLocalStore userLocalStore;
    String username;
    int offset;

    ArrayList<FollowedUser> followedUsers;
    ArrayList<GroupUserContent> friendsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_sharing);
        lvGroupSharing = (XListView) findViewById(R.id.lvGroupSharing);
        lvGroupSharing.setPullLoadEnable(true);
        lvGroupSharing.setPullRefreshEnable(false);
        lvGroupSharing.setXListViewListener(this);
        mHandler = new Handler();
        ivFrinedPicture = (ImageView) findViewById(R.id.ivFrinedPicture);
        ivFrPhoto = (ImageView) findViewById(R.id.ivFrPhoto);
        ivGroupSharingPhoto = (ImageView) findViewById(R.id.ivGroupSharingPhoto);
        tvFriendName = (TextView) findViewById(R.id.tvFriendName);
        tvFriendFeeling = (TextView) findViewById(R.id.tvFriendFeeling);

        followedUsers = new ArrayList<>();
        friendsContent = new ArrayList<>();

        offset = 0;

        userLocalStore = new UserLocalStore(this);
        username = userLocalStore.getLoggedInUser().username;

        new DownloadPhoto(username, ivGroupSharingPhoto).execute();
        doSearchFriends(username);
        doSearchFriendsContent(followedUsers, offset);
        myAdapter = new GroupSharingAdapter(this, friendsContent);
        lvGroupSharing.setAdapter(myAdapter);
    }

    private void onLoad() {
        lvGroupSharing.stopRefresh();
        lvGroupSharing.stopLoadMore();
        lvGroupSharing.setRefreshTime("刚刚");
    }

    private void doSearchFriends(String username) {
        ServerRequest serverRequest = new ServerRequest(this);
        JSONObject result = serverRequest.fetchFriendsInBackground(username);
        try{
            int length = result.getJSONArray("followedUser").length();
            for (int i = 0; i < length; i++) {
                String temp = result.getJSONArray("followedUser").getString(i);

                followedUsers.add(new FollowedUser(temp));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void doSearchFriendsContent(ArrayList<FollowedUser> followedUsers, int offset) {
        ServerRequest serverRequest = new ServerRequest(this);
        JSONObject result = serverRequest.fetchFriendsContentInBackground(followedUsers, offset);
        if (result == null) {

        }
        else {
            try {
                int length = result.getJSONArray("contents").length();
                for (int i = 0; i < length; i++) {
                    JSONObject content = result.getJSONArray("contents").getJSONObject(i);
                    String feeling = content.getString("feeling");
                    String picName = content.getString("picName");
                    String time = content.getString("time");
                    String username = content.getString("owner");

                    friendsContent.add(new GroupUserContent(feeling, picName, time, username));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onLoadMore() {
        offset += 5;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doSearchFriendsContent(followedUsers, offset);
                myAdapter.refresh(friendsContent);
                onLoad();
            }
        }, 2000);
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
}

class GroupSharingAdapter extends BaseAdapter {
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
    private LayoutInflater layoutInflater;
    ArrayList<GroupUserContent> friendsContent;
    int count;
    Context context;

    public GroupSharingAdapter(Context context, ArrayList<GroupUserContent> friendsContent) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.friendsContent = friendsContent;
        this.context = context;
        this.count = friendsContent.size();
    }

    public void refresh(ArrayList<GroupUserContent> friendsContent) {
        this.friendsContent = friendsContent;
        this.count = friendsContent.size();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return friendsContent.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupContentViewHolder holder;
        GroupUserContent userContent = friendsContent.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.groupsharing_list_element, null);
            holder = new GroupContentViewHolder();
            holder.feeling = (TextView) convertView.findViewById(R.id.tvFriendFeeling);
            holder.picture = (ImageView) convertView.findViewById(R.id.ivFrinedPicture);
            holder.time = (TextView) convertView.findViewById(R.id.tvFriendName);
            holder.photo = (ImageView) convertView.findViewById(R.id.ivFrPhoto);
            convertView.setTag(holder);
        } else {
            holder = (GroupContentViewHolder) convertView.getTag();
        }

        holder.feeling.setText(userContent.feeling);
        new DownloadPhoto(userContent.username, holder.photo).execute();
        new DownloadImage(userContent.picName, holder.picture).execute();
        String date = userContent.time.substring(5, 10);
        String month = "";
        switch (date.substring(0, 2)) {
            case "01":
                month = "Jan";
                break;
            case "02":
                month = "Feb";
                break;
            case "03":
                month = "Mar";
                break;
            case "04":
                month = "Apr";
                break;
            case "05":
                month = "May";
                break;
            case "06":
                month = "Jun";
                break;
            case "07":
                month = "Jul";
                break;
            case "08":
                month = "Aug";
                break;
            case "09":
                month = "Sep";
                break;
            case "10":
                month = "Oct";
                break;
            case "11":
                month = "Nov";
                break;
            case "12":
                month = "Dec";
                break;
        }
        holder.time.setText(userContent.username + "    " + month + "/" + date.substring(3));
        return convertView;
    }

    private class DownloadImage extends AsyncTask<Void, Void, Bitmap> {
        String name;
        ImageView user_image;

        public DownloadImage(String name, ImageView user_image) {
            this.name = name;
            this.user_image = user_image;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            String url = SERVER_ADDRESS + name;
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

class FollowedUser {
    String username;
    public FollowedUser(String username) {
        this.username = username;
    }
}

class GroupContentViewHolder {
    ImageView picture;
    TextView feeling;
    TextView time;
    ImageView photo;
}

class GroupUserContent {
    String feeling;
    String picName;
    String time;
    String username;
    public GroupUserContent(String feeling, String picName, String time, String username) {
        this.feeling = feeling;
        this.picName = picName;
        this.time = time;
        this.username = username;
    }
}

class DownloadPhoto extends AsyncTask<Void, Void, Bitmap> {
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
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