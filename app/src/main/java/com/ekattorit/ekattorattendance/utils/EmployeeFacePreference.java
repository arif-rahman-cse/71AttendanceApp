package com.ekattorit.ekattorattendance.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.ekattorit.ekattorattendance.SimilarityClassifier;
import com.google.gson.Gson;

import java.util.HashMap;

public class EmployeeFacePreference {

    private static EmployeeFacePreference employeeFacePreference;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    private EmployeeFacePreference(Context context) {
        sharedPreferences = context.getSharedPreferences(AppConfig.EMPLOYEE_FACE_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.apply();

    }

    //Creating object of private constructor for SharedPreference ready to data stored!

    public static EmployeeFacePreference getPreferences(Context context) {
        if (employeeFacePreference == null) {
            employeeFacePreference = new EmployeeFacePreference(context);
        }
        return employeeFacePreference;
    }

    //-----------------------------Set value and get value form SharedPreference------------------------------//
    public void setEmpFace(String empFace) {
        editor.putString(AppConfig.EMP_FACE, empFace);
        editor.apply();
    }

    public String getEmpFace() {
        return sharedPreferences.getString(AppConfig.EMP_FACE, new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>()));
    }


    public void deleteEmployeeFaceSharedPreference(Context mContext) {
        sharedPreferences = mContext.getSharedPreferences(AppConfig.EMPLOYEE_FACE_PREFERENCE, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }


}
