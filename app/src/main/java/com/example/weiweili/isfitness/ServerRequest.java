package com.example.weiweili.isfitness;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by mengyegong on 7/27/15.
 */
public class ServerRequest {
    ProgressDialog progressDialog;
    public static final int CONNECTION_TIME = 1000 * 15;
    public static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    public ServerRequest(Context context) {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Processing");
        progressDialog.setMessage("Please wait...");
    }

    public void storeUserDataInBackground(User user, GetUserCallBack callback) {
        progressDialog.show();
        new StoreUserDataAsyncTask(user, callback ).execute();
    }

    public void fetchUserDataInBackground(User user, GetUserCallBack callback) {
        progressDialog.show();
        new FetchUserDataAsyncTask(user, callback ).execute();
    }
    // Search users in background
    public JSONObject fetchSearchUserInBackground(String username) {
        progressDialog.show();
        JSONObject jObject = new JSONObject();
        try {
            jObject = new FetchSearchUserAsyncTask(username).execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return jObject;
    }

    // Add follow in background
    public void addFollowInBackground(String username, String wantfollow, AddFollowCallBack callback) {
        progressDialog.show();
        new AddFollowAsyncTask(username, wantfollow, callback).execute();
    }

    // Delete follow in background
    public void unFollowInBackground(String username, String wantunfollow, UnFollowCallBack callback) {
        progressDialog.show();
        new UnFollowAsyncTask(username, wantunfollow, callback).execute();
    }

    // Check whether followed in background
    public boolean checkFollowedInBackground(String username, String otherUserName) {
        boolean result = false;
        try {
            result = new CheckFollowedAsyncTask(username, otherUserName).execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean checkUserNameAsyncTask(String username) {
        boolean result = false;
        try {
            result = new CheckUserNameAsyncTask(username).execute().get();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public class CheckFollowedAsyncTask extends AsyncTask<Void, Void, Boolean> {
        String username;
        String otherUsername;

        public CheckFollowedAsyncTask(String username, String otherUsername) {
            this.username = username;
            this.otherUsername = otherUsername;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", username));
            dataToSend.add(new BasicNameValuePair("otherUsername", otherUsername));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "CheckFollowed.php");

            Boolean existed = false;

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                Log.d("followed", result);
                JSONObject jObject = new JSONObject(result);

                if(jObject.length() == 0) {
                    existed = false;
                }
                else {
                    existed = true;
                }
            }
            catch (Exception e) {
                //e.printStackTrace();
                existed = false;
            }

            return existed;
        }

        @Override
        protected void onPostExecute(Boolean existed) {
            super.onPostExecute(existed);
        }
    }

    public class AddFollowAsyncTask extends AsyncTask<Void, Void, Void> {
        String username;
        String wantfollow;
        AddFollowCallBack callback;

        public AddFollowAsyncTask(String username, String wantfollow, AddFollowCallBack callback) {
            this.username = username;
            this.wantfollow = wantfollow;
            this.callback = callback;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", username));
            dataToSend.add(new BasicNameValuePair("wantfollow", wantfollow));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "AddFollow.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            callback.done();
            super.onPostExecute(aVoid);
        }
    }

    public class UnFollowAsyncTask extends AsyncTask<Void, Void, Void> {
        String username;
        String wantunfollow;
        UnFollowCallBack callback;

        public UnFollowAsyncTask(String username, String wantunfollow, UnFollowCallBack callback) {
            this.username = username;
            this.wantunfollow = wantunfollow;
            this.callback = callback;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", username));
            dataToSend.add(new BasicNameValuePair("wantunfollow", wantunfollow));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "UnFollow.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            callback.done();
            super.onPostExecute(aVoid);
        }
    }

    public class StoreUserDataAsyncTask extends AsyncTask<Void, Void, Void> {
        User user;
        GetUserCallBack userCallback;

        public  StoreUserDataAsyncTask(User user, GetUserCallBack userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("name", user.name));
            dataToSend.add(new BasicNameValuePair("age", user.age + ""));
            dataToSend.add(new BasicNameValuePair("email", user.email));
            dataToSend.add(new BasicNameValuePair("username", user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "Register.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
            userCallback.done(null);
            super.onPostExecute(aVoid);
        }
    }

    public class FetchSearchUserAsyncTask extends AsyncTask<Void, Void, JSONObject> {
        String username;
        SearchUserCallBack userCallBack;

        public FetchSearchUserAsyncTask(String username) { this.username = username; }
        @Override
        protected JSONObject doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", username));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SearchUser.php");
            JSONObject jObject = new JSONObject();
            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                jObject = new JSONObject(result);
                Log.d("search", jObject.getJSONArray("username").getString(0));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return jObject;
        }
        @Override
        protected void onPostExecute(JSONObject jObject) {
            progressDialog.dismiss();
            super.onPostExecute(jObject);
        }
    }

    public class CheckUserNameAsyncTask extends  AsyncTask<Void, Void, Boolean> {
        String username;
        CheckUserCallBack userCallBack;

        public CheckUserNameAsyncTask(String username) {
            this.username = username;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", username));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "CheckUsername.php");

            Boolean existed = false;

            try{
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                Log.d("checkusername", result.length() + "");
                JSONObject jObject = new JSONObject(result);

                if(jObject.length() == 0) {
                    existed = false;
                }
                else {
                    existed = true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                existed = false;
            }
            return existed;
        }

        @Override
        protected void onPostExecute(Boolean existed) {
            super.onPostExecute(existed);
        }
    }

    public class FetchUserDataAsyncTask extends AsyncTask<Void, Void, User> {
        User user;
        GetUserCallBack userCallback;

        public FetchUserDataAsyncTask(User user, GetUserCallBack userCallback) {
            this.user = user;
            this.userCallback = userCallback;
        }

        @Override
        protected User doInBackground(Void... params) {
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("username", user.username));
            dataToSend.add(new BasicNameValuePair("password", user.password));

            HttpParams httpRequestParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpRequestParams, CONNECTION_TIME);
            HttpConnectionParams.setSoTimeout(httpRequestParams, CONNECTION_TIME);
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "FetchUserInfo.php");

            User returnedUser = null;

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                HttpResponse httpResponse = client.execute(post);
                HttpEntity entity = httpResponse.getEntity();
                String result = EntityUtils.toString(entity);
                JSONObject jObject = new JSONObject(result);

                if(jObject.length() == 0) {
                    returnedUser = null;
                }
                else {
                    String name = jObject.getString("name");
                    int age = jObject.getInt("age");
                    String email = jObject.getString("email");
                    String username = jObject.getString("username");
                    //String
                    returnedUser = new User(name, age, username, email, user.password, null);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            return returnedUser;
        }

        @Override
        protected void onPostExecute(User returnedUser) {
            progressDialog.dismiss();
            userCallback.done(returnedUser);
            super.onPostExecute(returnedUser);
        }
    }
}
