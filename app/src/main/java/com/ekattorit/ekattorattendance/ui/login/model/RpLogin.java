package com.ekattorit.ekattorattendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class RpLogin {
    @SerializedName("is_active")
    private boolean isActive;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("id")
    private int id;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("email")
    private String email;

    @SerializedName("username")
    private String username;

    @SerializedName("image")
    private String image;

    public String getImage() {
        return image;
    }

    public boolean isIsActive(){
        return isActive;
    }

    public String getLastName(){
        return lastName;
    }

    public int getId(){
        return id;
    }

    public String getFirstName(){
        return firstName;
    }

    public String getEmail(){
        return email;
    }

    public String getUsername(){
        return username;
    }
}
