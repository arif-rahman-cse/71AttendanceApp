package com.ekattorit.ekattorattendance.ui.face;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.widget.Toast;

import com.ekattorit.ekattorattendance.MainActivity;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityEmployeeCardScanBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.home.HomeActivity;
import com.ekattorit.ekattorattendance.ui.login.model.RpLoginError;
import com.ekattorit.ekattorattendance.ui.scan.CardScanActivity;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.ui.scan.model.RpError;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeCardScanActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeCardScan";
    private ActivityEmployeeCardScanBinding binding;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    UserCredentialPreference userCredentialPreference;
    boolean isQrDetected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_card_scan);
        binding = DataBindingUtil.setContentView(EmployeeCardScanActivity.this, R.layout.activity_employee_card_scan);
        userCredentialPreference = UserCredentialPreference.getPrefarences(EmployeeCardScanActivity.this);

    }

    private void initialiseDetectorsAndSources() {

        //Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        binding.surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(EmployeeCardScanActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(binding.surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(EmployeeCardScanActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                //Toast.makeText(getApplicationContext() , "To prevent memory leaks barcode scanner has been stopped" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    Log.d(TAG, "receiveDetections: "+barcodes.valueAt(0).displayValue);
                    String qrCode = barcodes.valueAt(0).displayValue;
                    //cameraSource.release();
                    barcodeDetector.release();
                    getEmployeeDetails(qrCode.trim());
                }
            }
        });
    }


    private void getEmployeeDetails(String qrData) {
        Log.d(TAG, "getEmployeeDetails: Called");
        Call<RpEmpDetails> cardDetailsCall = RetrofitClient
                .getInstance()
                .getApi()
                .getEmpDetails(qrData);

        cardDetailsCall.enqueue(new Callback<RpEmpDetails>() {
            @Override
            public void onResponse(Call<RpEmpDetails> call, Response<RpEmpDetails> response) {

                AppProgressBar.hideMessageProgress();
                if (response.isSuccessful() && response.code() == 200) {

                    if (response.body() != null) {

                        RpEmpDetails empDetails = response.body();
                        Log.d(TAG, "onResponse: Details: " + empDetails.getEmpId());
                        if (userCredentialPreference.getSuperVisorWard().equals(empDetails.getWordNo())) {
                            Log.d(TAG, "onResponse: Go to face add activity ");
                            Intent intent = new Intent(EmployeeCardScanActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else {
                            SweetAlertDialog sw = new SweetAlertDialog(EmployeeCardScanActivity.this, SweetAlertDialog.WARNING_TYPE);
                            sw.setCancelable(false);
                            sw.setTitleText(" নির্দেশনা !")
                                    .setContentText("স্ক্যানকৃত কর্মী আপনার ওয়ার্ড এর নয়, তাই এই এমপ্লয়ী এর উপস্থিতি নিশ্চিতের  জন্য পারমিশন এর প্রয়াজন হতে পারে ।")
                                    .setConfirmText("ঠিক আছে ")
                                    .setConfirmClickListener(sDialog -> {
                                        sDialog.dismissWithAnimation();
                                        Intent intent = new Intent(EmployeeCardScanActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    })
                                    .show();
                        }

                    }


                } else if (response.code() == 404) {
                    Log.d(TAG, "onResponse: Code: " + response.code());
                    Gson gson = new GsonBuilder().create();
                    RpError loginError;
                    try {
                        loginError = gson.fromJson(response.errorBody().string(), RpError.class);
                        Log.d(TAG, "onResponse: msg: " + loginError);
                        //Toast.makeText(LoginActivity.this, loginError.getNonFieldErrors().get(0), Toast.LENGTH_LONG).show();
                        SweetAlertDialog sw = new SweetAlertDialog(EmployeeCardScanActivity.this, SweetAlertDialog.WARNING_TYPE);
                        sw.setCancelable(false);
                        sw.setTitleText(" নির্দেশনা !")
                                .setContentText(loginError.getError())
                                .setConfirmText("ঠিক আছে ")
                                .setConfirmClickListener(sDialog -> {
                                    sDialog.dismissWithAnimation();
                                    Intent intent = new Intent(EmployeeCardScanActivity.this, EmployeeListActivity.class);
                                    startActivity(intent);
                                    finish();
                                })
                                .show();
                        Log.d(TAG, "onResponse: " + response.errorBody().string());
                    } catch (IOException e) {
                        // handle failure at error parse
                    }
                }
            }

            @Override
            public void onFailure(Call<RpEmpDetails> call, Throwable t) {
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
                AppProgressBar.hideMessageProgress();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();
    }
}