package com.ekattorit.ekattorattendance.ui.scan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityScanPreviewBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.home.HomeActivity;
import com.ekattorit.ekattorattendance.ui.scan.model.RpNewScan;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ScanPreviewActivity extends AppCompatActivity {
    private static final String TAG = "ScanPreviewActivity";
    ActivityScanPreviewBinding binding;
    private File employeeImgFile;
    private UserCredentialPreference userCredentialPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_preview);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_scan_preview);
        userCredentialPreference = UserCredentialPreference.getPrefarences(ScanPreviewActivity.this);

        Intent scanIntent = getIntent();
        String empId = scanIntent.getStringExtra(AppConfig.EMP_ID);
        String empName = scanIntent.getStringExtra(AppConfig.EMP_NAME);
        String empDesignation = scanIntent.getStringExtra(AppConfig.DESIGNATION);
        String wordNo = scanIntent.getStringExtra(AppConfig.WORD_NO);
        String phone = scanIntent.getStringExtra(AppConfig.PHONE);
        String emp_img = scanIntent.getStringExtra(AppConfig.EMP_IMG);
        String businessId = scanIntent.getStringExtra(AppConfig.BUSINESS_ID);
        int supervisorId = scanIntent.getIntExtra(AppConfig.SUPERVISOR_ID, 0);
        boolean attendanceType = scanIntent.getBooleanExtra(AppConfig.ATTENDANCE_TYPE, false);
        String userCurrentAddress = getIntent().getStringExtra(AppConfig.ADDRESS);
        double latitude = getIntent().getDoubleExtra(AppConfig.LATITUDE, 0.0);
        double longitude = getIntent().getDoubleExtra(AppConfig.LONGITUDE, 0.0);

        setScanData(empId, empName, empDesignation, wordNo, phone, emp_img);
        Log.d(TAG, "onCreate: Link: " + emp_img);

        // getShiftName(empId, empName, wordNo);

        binding.btnUploadImg.setOnClickListener(view -> {

            ImagePicker.Companion.with(this)
                    .cameraOnly()
                    //.crop()                 //Crop image(Optional), Check Customization for more option
                    .compress(256)            //Final image size will be less than 200 KB  MB(Optional)
                    .maxResultSize(724, 724)    //Final image resolution will be less than 1080 x 1080(Optional)
                    .start();

        });

        binding.btnClose.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finish();
            startActivity(intent);

        });

        binding.btnCompleteScan.setOnClickListener(view -> {
            if (employeeImgFile != null) {
                binding.attendancePb.setVisibility(View.VISIBLE);
                binding.btnCompleteScan.setAlpha(0.3f);
                binding.btnCompleteScan.setClickable(false);

                //addNewAttendance(empId, empName, wordNo, businessId, supervisorId, attendanceType, userCurrentAddress, latitude, longitude);

                //addNewAttendance(empId, empName, wordNo);
                //getShiftName(empId, empName, wordNo, businessId, supervisorId, attendanceType, userCurrentAddress, latitude, longitude);

            } else {
                Snackbar.make(binding.mainView, "অনুগ্রহ করে এমপ্লয়ীর  ছবি তুলুন", BaseTransientBottomBar.LENGTH_LONG).show();
            }

        });

    }


    private void setScanData(String empId, String empName, String empDesignation, String wordNo, String phone, String imgLink) {
        binding.tvCardId.setText(empId);
        binding.tvCardOwner.setText(empName);
        binding.tvDesignation.setText(empDesignation);
        binding.tvWordNo.setText(wordNo);
        binding.tvPhone.setText(phone);

        Glide.with(this)
                .load(AppConfig.Base_URL_ONLINE_IMG + imgLink)
                .placeholder(R.drawable.loading_01)
                .centerInside()
                .error(R.drawable.default_profile_pic)
                .into(binding.ivEmpImgFixed);

    }

    /**
     *
    private void addNewAttendance(String empId, String empName, String wordNo, String businessId, int supervisorId, boolean attendanceType,
                                  String userCurrentAddress, double latitude, double longitude) {

        RequestBody requestInnerFile = RequestBody.create(MediaType.parse("multipart/form-data"), employeeImgFile);
        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part scanImage = MultipartBody.Part.createFormData("scan_img", employeeImgFile.getName(), requestInnerFile);

        double attendance_count = attendanceType ? 0.5 : 1;

        Call<RpNewScan> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .addNewAttendance(
                        businessId, supervisorId, wordNo, empId, latitude, longitude, attendance_count, userCurrentAddress, true, scanImage, userCredentialPreference.getUserId());

        newScanCall.enqueue(new Callback<RpNewScan>() {
            @Override
            public void onResponse(Call<RpNewScan> call, Response<RpNewScan> response) {
                Log.d(TAG, "onResponse: " + response.code());
                binding.attendancePb.setVisibility(View.GONE);
                binding.btnCompleteScan.setAlpha(1f);
                binding.btnCompleteScan.setClickable(true);
                if (response.code() == 201) {
                    final SweetAlertDialog sDialog = AppProgressBar.userActionSuccessPb(ScanPreviewActivity.this, empName + " এর হাজিরা সফল হয়েছে ");
                    sDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });

                }
                else if (response.code() == 200) {

                    final SweetAlertDialog sDialog = AppProgressBar.userActionSuccessPb(ScanPreviewActivity.this, empName + " এর হাজিরা আপডেট সফল হয়েছে ");
                    sDialog.setConfirmClickListener(sweetAlertDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    });

                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        final SweetAlertDialog sDialog = AppProgressBar.userActionErrorPb(ScanPreviewActivity.this, " কিছু একটা সমস্যা হয়েছে " + response.errorBody().string());
                        sDialog.setConfirmClickListener(sweetAlertDialog -> {
                            sDialog.dismissWithAnimation();
                            Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpNewScan> call, Throwable t) {
                binding.attendancePb.setVisibility(View.GONE);
                binding.btnCompleteScan.setClickable(true);
                binding.btnCompleteScan.setAlpha(1f);
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
                final SweetAlertDialog sDialog = AppProgressBar.userActionErrorPb(ScanPreviewActivity.this, " কিছু একটা সমস্যা হয়েছে " + t.getMessage());
                sDialog.setConfirmClickListener(sweetAlertDialog -> {
                    sDialog.dismissWithAnimation();
                    Intent intent = new Intent(ScanPreviewActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
            }
        });
    }

     */

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            //Image Uri will not be null for RESULT_OK
            Uri selectedImageUri = data.getData();
            binding.ivEmployeeImg.setImageURI(selectedImageUri);
            //You can get File object from intent
            employeeImgFile = ImagePicker.Companion.getFile(data);

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.Companion.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

}