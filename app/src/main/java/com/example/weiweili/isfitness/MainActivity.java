package com.example.weiweili.isfitness;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;



public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    Button bLogout, bSearchUser, bShareContent, bMyPage, bGroupSharing, bSport, bMySport;
    EditText etName, etEmail, etUsername;
    ImageView ivPhoto;


    UserLocalStore userLocalStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);


        bLogout = (Button) findViewById(R.id.bLogout);
        bShareContent = (Button) findViewById(R.id.bShareContent);
        bSearchUser = (Button) findViewById(R.id.bSearchUser);
        bMyPage = (Button) findViewById(R.id.bMyPage);
        bGroupSharing = (Button) findViewById(R.id.bGroupSharing);
        bSport = (Button) findViewById(R.id.bSport);
        bMySport = (Button) findViewById(R.id.bMySport);
        ivPhoto = (ImageView) findViewById(R.id.ivPhoto);

        bLogout.setOnClickListener(this);
        bShareContent.setOnClickListener(this);
        bSearchUser.setOnClickListener(this);
        bMyPage.setOnClickListener(this);
        bGroupSharing.setOnClickListener(this);
        bSport.setOnClickListener(this);
        bMySport.setOnClickListener(this);
        ivPhoto.setOnClickListener(this);


        userLocalStore = new UserLocalStore(this);
    }
    @Override
    protected void onStart() {
        super.onStart();

        if(authenticate() == true) {
            displayUserDetails();
        }
    }

    private  boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }

    private void displayUserDetails() {
        User user = userLocalStore.getLoggedInUser();
        etUsername.setText(user.username);
        etEmail.setText(user.email);
        etName.setText(user.name);
        new DownloadImage().execute();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivPhoto:
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);

                break;

            case R.id.bSearchUser:
                Intent intent1 = new Intent(MainActivity.this, SearchUser.class);
                startActivity(intent1);
                break;

            case R.id.bLogout:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);

                startActivity(new Intent(this, Login.class));
                break;

            case R.id.bShareContent:
                Intent intent2 = new Intent(MainActivity.this, ShareContent.class);
                startActivity(intent2);
                break;

            case R.id.bMyPage:
                Intent intent3 = new Intent(MainActivity.this, MyPage.class);
                startActivity(intent3);
                break;

            case R.id.bGroupSharing:
                Intent intent4 = new Intent(MainActivity.this, GroupSharing.class);
                startActivity(intent4);
                break;

            case R.id.bSport:
                Intent intent5 = new Intent(MainActivity.this, Sport.class);
                startActivity(intent5);
                break;
            case R.id.bMySport:
                Intent intent6 = new Intent(MainActivity.this, MyActivities.class);
                startActivity(intent6);
                break;
        }
    }
    private class Uploadphoto extends AsyncTask<Void, Void, Void> {
        Bitmap image;
        String username;
        public Uploadphoto(Bitmap image, String username) {
            this.image = image;
            this.username = username;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedPhoto = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("photo", encodedPhoto));
            dataToSend.add(new BasicNameValuePair("username", username));

            HttpParams httpRequestParams = getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SavePhoto.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Photo Uploaded", Toast.LENGTH_SHORT).show();
        }
    }
    private HttpParams getHttpRequestParams() {
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
        HttpConnectionParams.setSoTimeout(httpRequestParams, 1000 * 30);
        return httpRequestParams;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            ivPhoto.setImageURI(selectedImage);
            User user = userLocalStore.getLoggedInUser();
            //String username = etUsername.getText().toString();
            Bitmap image = ((BitmapDrawable) ivPhoto.getDrawable()).getBitmap();
            new Uploadphoto(image, user.username).execute();

        }
    }

    private class DownloadImage extends AsyncTask<Void, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(Void... params) {
            User user = userLocalStore.getLoggedInUser();
            String url = SERVER_ADDRESS + "photo/" + user.username + ".JPG";
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
                ivPhoto.setImageBitmap(bitmap);
            }
        }
    }




    //    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
}
