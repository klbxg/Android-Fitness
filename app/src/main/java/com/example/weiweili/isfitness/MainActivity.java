package com.example.weiweili.isfitness;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {
    private static final int RESULT_LOAD_IMAGE = 1;

    Button bLogout, bUploadImage, bDownloadImage;
    EditText etName, etEmail, etUsername, etUploadName, etDownloadName;
    ImageView imageToUpload, DownloadImage;

    UserLocalStore userLocalStore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);

        etUploadName = (EditText) findViewById(R.id.etUploadName);
        etDownloadName = (EditText) findViewById(R.id.etDownloadName);

        imageToUpload = (ImageView) findViewById(R.id.imageToUpload);
        DownloadImage = (ImageView) findViewById(R.id.DownloadImage);

        bUploadImage = (Button) findViewById(R.id.bUploadImage);
        bDownloadImage = (Button) findViewById(R.id.bDownloadImage);

        bLogout = (Button) findViewById(R.id.bLogout);
        bLogout.setOnClickListener(this);
        imageToUpload.setOnClickListener(this);
        bUploadImage.setOnClickListener(this);
        bDownloadImage.setOnClickListener(this);

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
//        String uname = getIntent().getStringExtra("Username");
//        String name = getIntent().getStringExtra("Name");
//        String email = getIntent().getStringExtra("Email");
        etUsername.setText(user.username);
        etEmail.setText(user.email);
        etName.setText(user.name);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageToUpload:
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RESULT_LOAD_IMAGE);
                break;

            case R.id.bLogout:
                userLocalStore.clearUserData();
                userLocalStore.setUserLoggedIn(false);

                startActivity(new Intent(this, Login.class));
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            imageToUpload.setImageURI(selectedImage);
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
