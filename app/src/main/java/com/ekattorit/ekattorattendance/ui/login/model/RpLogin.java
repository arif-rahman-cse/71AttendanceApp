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

    @SerializedName("users_type")
    private String users_type;

    @SerializedName("supervisor_latitude")
    private String supervisor_latitude;

    @SerializedName("supervisor_longitude")
    private String supervisor_longitude;

    @SerializedName("range")
    private int range;


    @SerializedName("supervisor_ward")
    private String supervisor_ward;


    public String getImage() {
        return image;
    }

    public boolean isIsActive() {
        return isActive;
    }

    public String getLastName() {
        return lastName;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getUsers_type() {
        return users_type;
    }

    public String getSupervisor_latitude() {
        return supervisor_latitude;
    }

    public String getSupervisor_longitude() {
        return supervisor_longitude;
    }

    public int getRange() {
        return range;
    }

    public String getSupervisor_ward() {
        return supervisor_ward;
    }
}
