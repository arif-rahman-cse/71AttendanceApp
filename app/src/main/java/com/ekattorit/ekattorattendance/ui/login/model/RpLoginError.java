package com.ekattorit.ekattorattendance.ui.login.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RpLoginError {
    @SerializedName("non_field_errors")
    private List<String> nonFieldErrors;

    public List<String> getNonFieldErrors(){
        return nonFieldErrors;
    }
}
