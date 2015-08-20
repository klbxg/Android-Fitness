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


public class ShareContent extends ActionBarActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    Button bUploadImage;
    EditText etUploadImageName;
    ImageView imageToUpload;
    UserLocalStore userLocalStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_content);

        etUploadImageName = (EditText) findViewById(R.id.etUploadName);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);

        bUploadImage = (Button) findViewById(R.id.bUploadImage);

        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        userLocalStore = new UserLocalStore(this);

    }
    @Override
    protected void onStart() {
        super.onStart();

        if(authenticate()) {
            displayUserDetails();
        }
    }
    private  boolean authenticate() {
        return userLocalStore.getUserLoggedIn();
    }
    private void displayUserDetails() {
        User user = userLocalStore.getLoggedInUser();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_share_content, menu);
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageToUpload:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                break;

            case R.id.bUploadImage:
                User user = userLocalStore.getLoggedInUser();
                Bitmap image = ((BitmapDrawable) imageToUpload.getDrawable()).getBitmap();
                new UploadImage(image, etUploadImageName.getText().toString(), user.username).execute();
                Intent intent1 = new Intent(ShareContent.this, MyPage.class);
                startActivity(intent1);
                break;
        }
    }
    private class UploadImage extends AsyncTask<Void, Void, Void> {
        Bitmap image;
        String name;
        String username;
        public UploadImage(Bitmap image, String name, String username) {
            this.image = image;
            this.name = name;
            this.username = username;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("image", encodedImage));
            dataToSend.add(new BasicNameValuePair("name", name));
            dataToSend.add(new BasicNameValuePair("username", username));

            HttpParams httpRequestParams = getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SavePictures.php");

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
            Toast.makeText(getApplicationContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
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
            imageToUpload.setImageURI(selectedImage);
        }
    }
}
