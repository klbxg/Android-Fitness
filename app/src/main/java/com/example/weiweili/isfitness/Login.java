package com.example.weiweili.isfitness;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class Login extends ActionBarActivity implements View.OnClickListener{

    Button bLogin;
    EditText etUsername, etPassword;
    TextView tvRegisterLink;
    UserLocalStore userLocalStore;
    DatabaseHelper helper = new DatabaseHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword = (EditText) findViewById(R.id.etPassword);
        bLogin = (Button) findViewById(R.id.bLogin);
        tvRegisterLink = (TextView) findViewById(R.id.tvRegisterLink);

        bLogin.setOnClickListener(this);
        tvRegisterLink.setOnClickListener(this);

        userLocalStore = new UserLocalStore(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bLogin:

                User user = new User(null, null);
                userLocalStore.storeUserData(user);
                userLocalStore.setUserLoggedIn(true);

                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();

                String pass = helper.searchPass(username);

                if(pass.equals(password)) {
                    Intent intent = new Intent(Login.this, MainActivity.class);
                    intent.putExtra("Username", username);
                    User user1 = helper.searchInfo(username);
                    intent.putExtra("Email", user1.email);
                    intent.putExtra("Name", user1.name);
                    startActivity(intent);
                } else {
                    Toast temp = Toast.makeText(Login.this, "Passwords don't match!", Toast.LENGTH_SHORT);
                    temp.show();
                }

                break;
            case  R.id.tvRegisterLink:

                startActivity(new Intent(this, Register.class));
                break;
        }
    }


}
