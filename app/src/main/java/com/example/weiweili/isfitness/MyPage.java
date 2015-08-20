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
import java.lang.Object;

import com.example.weiweili.isfitness.XListView.IXListViewListener;

public class MyPage extends ActionBarActivity implements IXListViewListener {
//    ListView lvSearchMyContent;
    XListView lvSearchMyContent;
    ImageView ivphoto;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";
    private Handler mHandler;
    ArrayList<UserContent> contents = new ArrayList<>();
    SearchContentAdapter myAdapter;
    int offset;    // the database offset for searching use content, it should + 5 every pull up refresh
    UserLocalStore userLocalStore;
    String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);

        lvSearchMyContent = (XListView) findViewById(R.id.lvSearchMyContent);
        lvSearchMyContent.setPullLoadEnable(true);
        lvSearchMyContent.setPullRefreshEnable(false);
        lvSearchMyContent.setXListViewListener(this);
        mHandler = new Handler();
        ivphoto = (ImageView) findViewById(R.id.ivphoto);

        offset = 0;

        userLocalStore = new UserLocalStore(this);
        username = userLocalStore.getLoggedInUser().username;

        Log.d("username", username);
        new DownloadPhoto(username, ivphoto).execute();
        doSearchContents(username, contents, offset);
        myAdapter = new SearchContentAdapter(this, contents);
        lvSearchMyContent.setAdapter(myAdapter);
    }

    private void onLoad() {
        lvSearchMyContent.stopRefresh();
        lvSearchMyContent.stopLoadMore();
        lvSearchMyContent.setRefreshTime("JUST NOW");
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//
//    }

    private void doSearchContents(String username, ArrayList<UserContent> contents, int offset) {
        ServerRequest serverRequest = new ServerRequest(this);
        JSONObject result = serverRequest.fetchUserPageInBackground(username, offset);
        Log.d("result", result.toString());
        try{
            int length = result.getJSONArray("contents").length();
            for (int i = 0; i < length; i++) {
                JSONObject content = result.getJSONArray("contents").getJSONObject(i);
                String feeling = content.getString("feeling");
                String picName = content.getString("picName");
                String time = content.getString("time");

                contents.add(new UserContent(feeling, picName, time));
            }

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

    @Override
    public void onLoadMore() {
        offset += 5;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                doSearchContents(username, contents, offset);
                myAdapter.refresh(contents);
                onLoad();
            }
        }, 2000);
    }

    @Override
    public void onRefresh() {

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

    public void refresh(ArrayList<UserContent> contentList) {
        this.contentList = contentList;
        this.count = contentList.size();
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
        holder.time.setText(month + "/" + date.substring(3));
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