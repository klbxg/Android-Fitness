package com.example.weiweili.isfitness;

/**
 * Created by weiweili on 7/16/15.
 */
public class User {
    String name, username, password, email;
    int age;

    public User(String name, int age, String username, String email, String password) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.username = username;
        this.password = password;

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.age = -1;
        this.name = "";
    }
}
