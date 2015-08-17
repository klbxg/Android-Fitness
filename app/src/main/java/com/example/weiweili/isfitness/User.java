package com.example.weiweili.isfitness;

import android.graphics.Bitmap;

/**
 * Created by weiweili on 7/16/15.
 */
public class User {
    String name, username, password, email, photo;
    int age;

    public User(String name, int age, String username, String email, String password, String photo) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.username = username;
        this.password = password;
        this.photo = photo;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.age = -1;
        this.name = "";
    }
}
