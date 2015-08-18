package com.example.weiweili.isfitness;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.lang.Object;


public class MyPage extends ActionBarActivity {
    ListView lvSearchMyContent;
    ImageView ivphoto;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        lvSearchMyContent = (ListView) findViewById(R.id.lvSearchMyContent);
        ivphoto = (ImageView) findViewById(R.id.ivphoto);

        UserLocalStore userLocalStore = new UserLocalStore(this);
        String username = userLocalStore.getLoggedInUser().username;
        Log.d("username", username);
        new DownloadPhoto(username, ivphoto).execute();
        doSearchContents(username);


    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//    }

    private void doSearchContents(String username) {
        ServerRequest serverRequest = new ServerRequest(this);
        JSONObject result = serverRequest.fetchUserPageInBackground(username);
        Log.d("result", result.toString());
        try{
            ArrayList<UserContent> contents = new ArrayList<>();
            int length = result.getJSONArray("contents").length();
            for (int i = 0; i < length; i++) {
                JSONObject content = result.getJSONArray("contents").getJSONObject(i);
                String feeling = content.getString("feeling");
                String picName = content.getString("picName");
                String time = content.getString("time");
                Log.d("time", time);

                contents.add(new UserContent(feeling, picName, time));
            }
            lvSearchMyContent.setAdapter(new SearchContentAdapter(this, contents));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_page, menu);
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
}
class UserContent {
    String feeling;
    String picName;
    String time;
    public UserContent(String feeling, String picName, String time) {
        this.feeling = feeling;
        this.picName = picName;
        this.time = time;
    }
}

class SearchContentAdapter extends BaseAdapter {
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
    private LayoutInflater layoutInflater;
    ArrayList<UserContent> contentList;
    int count;
    Context context;

    public SearchContentAdapter(Context context, ArrayList<UserContent> contentList) {
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.contentList = contentList;
        this.context = context;
        this.count = contentList.size();
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
        return contentList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContentViewHolder holder;
        UserContent userContent = contentList.get(position);

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.mypage_list_element, null);
            holder = new ContentViewHolder();
            holder.feeling = (TextView) convertView.findViewById(R.id.tvFeeling);
            holder.picture = (ImageView) convertView.findViewById(R.id.ivPicture);
            holder.time = (TextView) convertView.findViewById(R.id.tvTime);
            convertView.setTag(holder);
        } else {
            holder = (ContentViewHolder) convertView.getTag();
        }

        holder.feeling.setText(userContent.feeling);
        new DownloadImage(userContent.picName, holder.picture).execute();
        holder.time.setText(userContent.time);
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
    class ContentViewHolder {
        ImageView picture;
        TextView feeling;
        TextView time;
    }