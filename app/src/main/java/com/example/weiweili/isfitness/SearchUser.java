package com.example.weiweili.isfitness;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import org.json.JSONObject;

import java.lang.reflect.Array;
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
        JSONObject result = serverRequest.fetchSearchUserInBackground(query);
        try{
            ArrayList<UserSearched> users = new ArrayList<>();
            int length = result.getJSONArray("username").length();
            Log.d("search", length + "");

            for (int i = 0; i < length; i++) {
                users.add(new UserSearched(result.getJSONArray("username").getString(i)));
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

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.search_list_element, null);
            holder = new ViewHolder();
            holder.user_name = (TextView)convertView.findViewById(R.id.search_result_username);
            //holder.user_image = (ImageView)convertView.findViewById(R.id.search_result_userImage);
            holder.addFriend = (ImageButton)convertView.findViewById(R.id.add_friend);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.user_name.setText(userSearched.username);
        //holder.user_image.setImageBitmap();
        holder.addFriend.setOnClickListener(new FollowClickListener(userSearched, context));

        return convertView;
    }

    static class ViewHolder {
        //ImageView user_image;
        TextView user_name;
        ImageButton addFriend;
    }
}

// this is the follow button click listener
class FollowClickListener implements OnClickListener {
    UserSearched userSearched;
    Context context;

    public FollowClickListener(UserSearched userSearched, Context context) {
        this.userSearched = userSearched;
        this.context = context;
    }

    @Override
    public void onClick(View v) {

    }

}