package com.example.asus.chatapp;

public class Requests {

    private String user_name, user_thumb_image;

    public Requests(){

    }

    public Requests(String user_name, String user_thumb_image) {
        this.user_name = user_name;
        this.user_thumb_image = user_thumb_image;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_thumb_image() {
        return user_thumb_image;
    }

    public void setUser_thumb_image(String user_thumb_image) {
        this.user_thumb_image = user_thumb_image;
    }
}
