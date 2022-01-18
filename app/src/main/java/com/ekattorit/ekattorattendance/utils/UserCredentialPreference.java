package com.ekattorit.ekattorattendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class UserCredentialPreference {

    private static UserCredentialPreference userCredentialPreference;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private UserCredentialPreference(Context context) {
        sharedPreferences = context.getSharedPreferences(AppConfig.SHARED_PREFARANCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

    }

    //Creating object of private constructor for SharedPrefarance ready to data stored!
    public static UserCredentialPreference getPrefarences(Context context) {
        if (userCredentialPreference == null) {
            userCredentialPreference = new UserCredentialPreference(context);
        }
        return userCredentialPreference;
    }


    //-----------------------------Set value and get value form sharedPrefarance------------------------------//

    public void setUserPhone(String userPhone) {
        editor.putString(AppConfig.USER_PHONE, userPhone);
        editor.apply();
    }

    public String getUserPhone() {
        return sharedPreferences.getString(AppConfig.USER_PHONE, null);
    }

    public void setUserId(int userId) {
        editor.putInt(AppConfig.USER_ID, userId);
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(AppConfig.USER_ID, 0);
    }

    public void setPassword(String password) {
        editor.putString(AppConfig.USER_PASS, password);
        editor.apply();
    }

    public String getPassword() {
        return sharedPreferences.getString(AppConfig.USER_PASS, null);
    }


    public void setName(String name) {
        editor.putString(AppConfig.PERSON_NAME, name);
        editor.apply();
    }

    public String getName() {
        return sharedPreferences.getString(AppConfig.PERSON_NAME, null);
    }

    public void setTotalScan(int totalScan) {
        editor.putInt(AppConfig.TOTAL_SCAN, totalScan);
        editor.apply();
    }

    public int getTotalScan() {
        return sharedPreferences.getInt(AppConfig.TOTAL_SCAN, 0);
    }




    public String getProfileUrl() {
        return sharedPreferences.getString(AppConfig.PROFILE_IMG_URL, null);
    }


    public void setProfileUrl(String profileUrl) {
        editor.putString(AppConfig.PROFILE_IMG_URL, profileUrl);
        editor.apply();
    }


    public void deleteSharedPrefarance(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(AppConfig.SHARED_PREFARANCE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


}
