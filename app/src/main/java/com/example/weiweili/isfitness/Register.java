package com.example.weiweili.isfitness;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class Register extends ActionBarActivity implements View.OnClickListener{

    //DatabaseHelper helper = new DatabaseHelper(this);
    Button bRegister;
    EditText etName, etAge, etUsername, etPassword1, etPassword2, etEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = (EditText) findViewById(R.id.etName);
        etAge = (EditText) findViewById(R.id.etAge);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etUsername = (EditText) findViewById(R.id.etUsername);
        etPassword1 = (EditText) findViewById(R.id.etPassword1);
        etPassword2 = (EditText) findViewById(R.id.etPassword2);

        bRegister = (Button) findViewById(R.id.bRegister);
        bRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.bRegister:

                String name = etName.getText().toString();
                String username = etUsername.getText().toString();
                String email = etEmail.getText().toString();
                String password1 = etPassword1.getText().toString();
                String password2 = etPassword2.getText().toString();
                int age = 0;
                try {
                    age = Integer.parseInt(etAge.getText().toString());
                }
                catch(NumberFormatException e) {
                    Toast pass = Toast.makeText(Register.this, "Age format wrong!", Toast.LENGTH_SHORT);
                    pass.show();
                    break;
                }

                if(!password1.equals(password2)) {
                    Toast pass = Toast.makeText(Register.this, "Passwords don't match!", Toast.LENGTH_SHORT);
                    pass.show();
                }
                else {
                    if(checkUser(username)) {
                        Toast existed = Toast.makeText(Register.this, "Username existed!", Toast.LENGTH_SHORT);
                        existed.show();
                    }
                    else {
                        registerUser(new User(name, age, username, email, password1));
                    }
                }
                break;
        }
    }

    private boolean checkUser(String username) {
        ServerRequest serverRequest = new ServerRequest(this);
        return serverRequest.checkUserNameAsyncTask(username);
    }
    private void registerUser(User user) {
        ServerRequest serverRequest = new ServerRequest(this);
        serverRequest.storeUserDataInBackground(user, new GetUserCallBack() {
            @Override
            public void done(User returnedUser) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });
    }

}
