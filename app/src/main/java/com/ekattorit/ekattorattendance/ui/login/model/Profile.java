package com.ekattorit.ekattorattendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

public class Profile {

    @SerializedName("image")
    private String image;

    @SerializedName("report_access")
    private Object reportAccess;

    @SerializedName("access")
    private String access;

    @SerializedName("business")
    private String business;

    @SerializedName("range")
    private int range;

    @SerializedName("users_type")
    private String usersType;


    @SerializedName("id")
    private int id;

    @SerializedName("designation")
    private String designation;

    @SerializedName("face_remove_permission")
    private boolean isFaceRemovePermission;

    @SerializedName("supervisor_longitude")
    private String supervisorLongitude;

    @SerializedName("supervisor_ward")
    private String supervisorWard;

    @SerializedName("user")
    private int user;

    @SerializedName("supervisor_latitude")
    private String supervisorLatitude;

    public String getImage() {
        return image;
    }

    public Object getReportAccess() {
        return reportAccess;
    }

    public String getAccess() {
        return access;
    }

    public String getBusiness() {
        return business;
    }

    public int getRange() {
        return range;
    }

    public String getUsersType() {
        return usersType;
    }


    public int getId() {
        return id;
    }

    public String getDesignation() {
        return designation;
    }

    public boolean isFaceRemovePermission() {
        return isFaceRemovePermission;
    }

    public String getSupervisorLongitude() {
        return supervisorLongitude;
    }

    public String getSupervisorWard() {
        return supervisorWard;
    }

    public int getUser() {
        return user;
    }

    public String getSupervisorLatitude() {
        return supervisorLatitude;
    }
}