package com.ekattorit.ekattorattendance.utils;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import cn.pedant.SweetAlert.SweetAlertDialog;

public final class AppProgressBar {
    private static final String TAG = "AppUtils";

    static SweetAlertDialog pmDialog;
    static SweetAlertDialog pDialog;


    public static void showMessageProgress(Context context, String msg) {
        pmDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pmDialog.getProgressHelper().setBarColor(Color.parseColor("#ECA800"));
        pmDialog.setTitleText(msg);
        pmDialog.setCancelable(true);
        pmDialog.show();
    }

    public static void hideMessageProgress() {
        if (pmDialog != null) {
            pmDialog.dismissWithAnimation();
        } else {
            Log.d(TAG, "hideNetworkProgress: Nothing to dismiss!");
        }
    }

    public static void hideProgress() {
        if (pDialog != null) {
            pDialog.dismissWithAnimation();
        } else {
            Log.d(TAG, "hideNetworkProgress: Nothing to dismiss!");
        }
    }


    public static void userAttentionPb(Context context, String content) {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE);
        pDialog.setTitleText(" নির্দেশনা !");
        pDialog.setContentText(content);
        pDialog.setConfirmButtonBackgroundColor(Color.parseColor("#E21F26"));
        pDialog.show();
        pDialog.setCancelable(false);
    }

    public static SweetAlertDialog userActionSuccessPb(Context context, String content) {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE);
        pDialog.setTitleText(" সফল !");
        pDialog.setContentText(content);
        pDialog.show();
        pDialog.setCancelable(false);
        return pDialog;
    }

    public static SweetAlertDialog userActionErrorPb(Context context, String content) {
        pDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);
        pDialog.setTitleText(" ব্যর্থ !");
        pDialog.setContentText(content);
        pDialog.show();
        return pDialog;
    }



}
