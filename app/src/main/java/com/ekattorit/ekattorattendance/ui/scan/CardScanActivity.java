package com.ekattorit.ekattorattendance.ui.scan;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import com.ekattorit.ekattorattendance.MainActivity;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityCardScanBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import java.io.IOException;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CardScanActivity extends AppCompatActivity {
    private static final String TAG = "CardScanActivity";

    private ActivityCardScanBinding binding;
    private UserCredentialPreference userCredentialPreference;
    //private CodeScanner mCodeScanner;
    private String qrData, userCurrentAddress;
    private static final int REQUEST_CAMERA = 1000;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_scan);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_card_scan);
        userCredentialPreference = UserCredentialPreference.getPreferences(this);
        userCurrentAddress = getIntent().getStringExtra(AppConfig.ADDRESS);
        latitude = getIntent().getDoubleExtra(AppConfig.LATITUDE, 0.0);
        longitude = getIntent().getDoubleExtra(AppConfig.LONGITUDE, 0.0);

        if (ContextCompat.checkSelfPermission(getBaseContext(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();

        } else {
            //readQRCode();
            //getEmployeeDetails("DNCC-RE-21-00015");

        }


    }

    /*
    private void readQRCode() {

        //scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, binding.scannerView);

        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            qrData = result.getText().trim();
            Log.d(TAG, "qrData: " + qrData);
            Intent intent = new Intent(CardScanActivity.this, MainActivity.class);
            intent.putExtra(AppConfig.EMP_ID, qrData);
            startActivity(intent);
            finish();

            //AppProgressBar.showMessageProgress(CardScanActivity.this, "অপেক্ষা করুন ...");
            //getEmployeeDetails(qrData);


        }));
        mCodeScanner.startPreview();

    }

     */

    private void getEmployeeDetails(String qrData) {

        Call<RpEmpDetails> cardDetailsCall = RetrofitClient
                .getInstance()
                .getApi()
                .getEmpDetails(qrData);

        cardDetailsCall.enqueue(new Callback<RpEmpDetails>() {
            @Override
            public void onResponse(Call<RpEmpDetails> call, Response<RpEmpDetails> response) {

                //AppProgressBar.hideMessageProgress();
                if (response.isSuccessful() && response.code() == 200) {

                    if (response.body() != null) {

                        RpEmpDetails empDetails = response.body();
                        Log.d(TAG, "onResponse: Details: " + empDetails.getEmpId());
                        Intent intent = new Intent(CardScanActivity.this, MainActivity.class);
                        intent.putExtra(AppConfig.EMP_ID, empDetails.getEmpId());
                        startActivity(intent);
                        finish();
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        //intent.putExtra(AppConfig.EMP_NAME, empDetails.getEmpName());
//                        intent.putExtra(AppConfig.PHONE, empDetails.getMobile());
//                        intent.putExtra(AppConfig.DESIGNATION, empDetails.getDesignation());
//                        intent.putExtra(AppConfig.WORD_NO, empDetails.getWordNo());
//                        intent.putExtra(AppConfig.EMP_IMG, empDetails.getEmployeeImg());
//                        intent.putExtra(AppConfig.BUSINESS_ID, empDetails.getBusiness());
//                        intent.putExtra(AppConfig.ATTENDANCE_TYPE, empDetails.isAttendanceType());
//                        intent.putExtra(AppConfig.SUPERVISOR_ID, empDetails.getUser());
//
//                        intent.putExtra(AppConfig.ADDRESS, userCurrentAddress);
//                        intent.putExtra(AppConfig.LATITUDE, latitude);
//                        intent.putExtra(AppConfig.LONGITUDE, longitude);


                    }


                } else {
                    Log.d(TAG, "onResponse: Code: "+response.code());
                    try {
                        Log.d(TAG, "onResponse: Error: "+ response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
//                    SweetAlertDialog sw = new SweetAlertDialog(CardScanActivity.this, SweetAlertDialog.WARNING_TYPE);
//                    sw.setCancelable(false);
//                            sw
//                            .setTitleText(" নির্দেশনা !")
//                            .setContentText("কার্ডটি ইনএক্টিভ বা ডাটাবেজে নেই !")
//                            .setConfirmText("ঠিক আছে ")
//                            .setConfirmClickListener(sDialog -> {
//                                sDialog.dismissWithAnimation();
//                                finish();
//                            })
//                            .show();
                }
            }

            @Override
            public void onFailure(Call<RpEmpDetails> call, Throwable t) {
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
                //AppProgressBar.hideMessageProgress();
            }
        });

    }


    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA);
    }



    @Override
    protected void onResume() {
        super.onResume();
        //mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        //mCodeScanner.releaseResources();
        super.onPause();
    }

}