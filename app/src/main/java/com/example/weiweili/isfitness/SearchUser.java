package com.example.weiweili.isfitness;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;


public class SearchUser extends ActionBarActivity {
    EditText etSearchFriendResult;
    ListView lvSearchFriendResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        lvSearchFriendResult = (ListView) findViewById(R.id.lvSearchFriendResult);

        //Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }

    }

    //Do the search
    private void doMySearch(String query) {

        ServerRequest serverRequest = new ServerRequest(this);
        UserLocalStore userLocalStore = new UserLocalStore(this);
        String useNow = userLocalStore.getLoggedInUser().username;
        JSONObject result = serverRequest.fetchSearchUserInBackground(query);
        try{
            ArrayList<UserSearched> users = new ArrayList<>();
            int length = result.getJSONArray("username").length();
            Log.d("search", length + "");

            for (int i = 0; i < length; i++) {
                String tmp = result.getJSONArray("username").getString(i);
                if (tmp.compareTo(useNow) != 0) {
                    users.add(new UserSearched(tmp));
                }
            }

            lvSearchFriendResult.setAdapter(new SearchUserAdapter(this, users));
        }
        catch (Exception e) {

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_search_user, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_friend).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false); // Do not iconify the widget; expand it by default

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

class UserSearched {
    String username;
    //String userimage;
//    public UserSearched(String username, String userimage) {
//        this.username = username;
//        this.userimage = userimage;
//    }
    public UserSearched(String username) {
        this.username = username;
    }
}

class SearchUserAdapter extends BaseAdapter {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
    private LayoutInflater layoutInflater;
    ArrayList<UserSearched> userList;
    int count;
    Context context;
    public SearchUserAdapter(Context context, ArrayList<UserSearched> userList) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.userList = userList;
        this.context = context;
        this.count = userList.size();
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
        return userList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        UserSearched userSearched = userList.get(position);
        ServerRequest serverRequest = new ServerRequest(context);
        UserLocalStore userLocalStore = new UserLocalStore(context);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.search_list_element, null);
            holder = new ViewHolder();
            holder.user_name = (TextView)convertView.findViewById(R.id.search_result_username);
            holder.user_image = (ImageView)convertView.findViewById(R.id.search_result_userImage);
            holder.addFriend = (ImageButton)convertView.findViewById(R.id.add_friend);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.user_name.setText(userSearched.username);
        new DownloadImage(userSearched.username, holder.user_image).execute();
        holder.addFriend.setOnClickListener(new FollowClickListener(userSearched, context));
        //holder.deleteFriend.setVisibility(View.GONE);
        //holder.user_image.setImageBitmap();
        Log.d("ttt","ttt");
        boolean followed = serverRequest.checkFollowedInBackground(userLocalStore.getLoggedInUser().username, userSearched.username);
        if (!followed) {
            Log.d("Followed", "False");
            holder.addFriend.setImageResource(R.drawable.add_friend);
            holder.addFriend.setOnClickListener(new FollowClickListener(userSearched, context, holder));
            //holder.deleteFriend.setVisibility(View.GONE);
        }
        else {
            Log.d("Followed", "True");
            //holder.deleteFriend.setVisibility(View.VISIBLE);
            holder.addFriend.setImageResource(R.drawable.delete_friend);
        }

        return convertView;
    }

    static class ViewHolder {
        ImageView user_image;
        TextView user_name;
        ImageButton addFriend;
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

            String url = SERVER_ADDRESS + "photo/" + name + ".JPG";
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(1000 * 30);
                connection.setReadTimeout(1000 * 30);
                return BitmapFactory.decodeStream((InputStream) connection.getContent(), null, null);
            }catch (Exception e) {
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

class ViewHolder {
    //ImageView user_image;
    TextView user_name;
    ImageButton addFriend;
    //ImageButton deleteFriend;
}

// this is the follow button click listener
class FollowClickListener implements OnClickListener {
    UserSearched userSearched;
    Context context;
    UserLocalStore userLocalStore;
    ViewHolder holder;

    public FollowClickListener(UserSearched userSearched, Context context, ViewHolder holder) {
        this.userSearched = userSearched;
        this.context = context;
        userLocalStore = new UserLocalStore(context);
        this.holder = holder;
    }

    @Override
    public void onClick(View v) {
        // update the database to follow
        ServerRequest serverRequest = new ServerRequest(context);
        serverRequest.addFollowInBackground(userLocalStore.getLoggedInUser().username, userSearched.username, new AddFollowCallBack() {
            @Override
            public void done() {
                holder.addFriend.setImageResource(R.drawable.delete_friend);
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, "Follow " + userSearched.username + "Success", duration);
                toast.show();
            }
        });
    }

}