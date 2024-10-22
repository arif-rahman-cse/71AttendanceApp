package com.ekattorit.ekattorattendance.ui.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityHomeBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.SupportActivity;
import com.ekattorit.ekattorattendance.ui.face.EmployeeListActivity;
import com.ekattorit.ekattorattendance.ui.face.FaceRecognitionActivity;
import com.ekattorit.ekattorattendance.ui.face.model.RpUpFace;
import com.ekattorit.ekattorattendance.ui.home.adapter.RecentScanAdapter;
import com.ekattorit.ekattorattendance.ui.home.model.RpRecentScan;
import com.ekattorit.ekattorattendance.ui.home.model.ScanItem;
import com.ekattorit.ekattorattendance.ui.login.LoginActivity;
import com.ekattorit.ekattorattendance.ui.report.AttendanceSummary;
import com.ekattorit.ekattorattendance.ui.report.DailyAttendanceStatus;
import com.ekattorit.ekattorattendance.ui.report.SingleRangeAttendance;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.EmployeeFacePreference;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";
    ActivityHomeBinding binding;
    UserCredentialPreference userCredentialPreference;
    EmployeeFacePreference employeeFacePreference;
    private List<ScanItem> scanItemList = new ArrayList<>();
    private RecentScanAdapter recentScanAdapter;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final int LOCATION_PERMISSION_ID = 102;
    boolean isScanFaceCircleClicked = false;
    int OUTPUT_SIZE = 192; //Output size of model


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);
        userCredentialPreference = UserCredentialPreference.getPreferences(HomeActivity.this);
        employeeFacePreference = EmployeeFacePreference.getPreferences(HomeActivity.this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        binding.navigationDrawer.setNavigationItemSelectedListener(this);
        binding.tvTotalScanCard.setText(MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", userCredentialPreference.getTotalScan()));

        if (getIntent().getBooleanExtra(AppConfig.IS_SCAN_SUCCESS, false)) {
            String msg = getIntent().getStringExtra(AppConfig.SCAN_SUCCESS_MSG);
            Snackbar.make(binding.mainView, msg, Snackbar.LENGTH_LONG).show();
        }
        initRecyclerView();
        getRecentScan();

        binding.drawerMenuBtn.setOnClickListener(v -> {
            TextView userName = findViewById(R.id.tv_name);
            TextView phone = findViewById(R.id.tv_phone);
            ImageView profileImage = findViewById(R.id.circleImageView);
            userName.setText(userCredentialPreference.getName());
            phone.setText(userCredentialPreference.getUserPhone());
            Log.d(TAG, "onCreate: URL: " + AppConfig.Base_URL_ONLINE_IMG + userCredentialPreference.getProfileUrl());
            Glide.with(HomeActivity.this)
                    .load(AppConfig.Base_URL_ONLINE_IMG + "/media/" + userCredentialPreference.getProfileUrl())
                    .placeholder(R.drawable.loading_01)
                    .centerInside()
                    .error(R.drawable.default_profile_pic)
                    .into(profileImage);
            binding.drawerLayout.openDrawer(GravityCompat.START);
        });

        binding.ivScan.setOnClickListener(v -> {
            isScanFaceCircleClicked = true;
            getUserCurrentLocation();
        });


    }

    private void getRecentScan() {

        Call<RpRecentScan> recentScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .getRecentScan(userCredentialPreference.getUserId());

        recentScanCall.enqueue(new Callback<RpRecentScan>() {
            @Override
            public void onResponse(Call<RpRecentScan> call, Response<RpRecentScan> response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onResponse: CODE: " + response.code());
                //Log.d(TAG, "onResponse: "+response.body().getResults().get(0).getFirstScan());

                if (response.isSuccessful() && response.code() == 200) {
                    scanItemList.clear();

                    if (response.body() != null) {
                        Log.d(TAG, "onResponse: Not Null");

                        if (response.body().getResults().size() > 0) {
                            binding.tvTotalScanCard.setText(MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", response.body().getCount()));
                            userCredentialPreference.setTotalScan(response.body().getCount());
                            binding.errorView.setVisibility(View.GONE);
                            scanItemList.addAll(response.body().getResults());
                            recentScanAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "onResponse: null");
                            binding.tvTotalScanCard.setText(MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", 0));
                            userCredentialPreference.setTotalScan(0);
                            binding.errorView.setVisibility(View.VISIBLE);
                        }


                    } else {
                        Log.d(TAG, "onResponse: null");
                        binding.tvTotalScanCard.setText(MessageFormat.format("মোট স্ক্যান হয়েছে {0} জন", 0));
                        userCredentialPreference.setTotalScan(0);
                        binding.errorView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d(TAG, "onResponse: Error");
                }

            }

            @Override
            public void onFailure(Call<RpRecentScan> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onResponse: " + t.getMessage());
            }
        });
    }


    private void initRecyclerView() {
        recentScanAdapter = new RecentScanAdapter(this, scanItemList);
        binding.rvDailySales.setAdapter(recentScanAdapter);
        binding.rvDailySales.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.log_out) {
            logout();
            return true;

        } else if (id == R.id.daily_attendance_report) {
            Intent intent = new Intent(HomeActivity.this, DailyAttendanceStatus.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.single_attendance_report) {
            Intent intent = new Intent(HomeActivity.this, SingleRangeAttendance.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.attendance_summery) {
            Intent intent = new Intent(HomeActivity.this, AttendanceSummary.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.support_number) {
            Intent intent = new Intent(HomeActivity.this, SupportActivity.class);
            startActivity(intent);
            return true;

        } else if (id == R.id.id_add_new_face) {
            closeDrawer();
            Intent intent = new Intent(HomeActivity.this, EmployeeListActivity.class);
            startActivity(intent);
            //finish();
            return true;
        } else if (id == R.id.sync_face) {
            //closeDrawer();
            syncFace();
            //finish();
            return true;
        }

        return false;
    }

    private void syncFace() {
        Call<RpUpFace> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .syncFace(userCredentialPreference.getUserId());

        newScanCall.enqueue(new Callback<RpUpFace>() {
            @Override
            public void onResponse(Call<RpUpFace> call, Response<RpUpFace> response) {
                Log.d(TAG, "onResponse: " + response.code());
                if (response.code() == 200) {
                    assert response.body() != null;
                   if (response.body().getFaceEmbeddings() != null && !response.body().getFaceEmbeddings().isEmpty()){
                       Log.d(TAG, "onResponse:  Face Ebd found: ");
                       employeeFacePreference.setEmpFace(response.body().getFaceEmbeddings());
                   }else {
                       employeeFacePreference.setEmpFace(getString(R.string.face_default_value));
                   }

                    Snackbar.make(binding.mainView, "Face sync success", Snackbar.LENGTH_LONG).show();
                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        Toast.makeText(HomeActivity.this, " কিছু একটা সমস্যা হয়েছে " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpUpFace> call, Throwable t) {
                Toast.makeText(HomeActivity.this, " কিছু একটা সমস্যা হয়েছে " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
            }
        });
    }

    //Save Faces to Shared Preferences.Conversion of Recognition objects to json string
    private void insertToSP(String face) {
//        SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("map", face);
//        editor.apply();
        employeeFacePreference.setEmpFace(face);


    }

    private void logout() {
        userCredentialPreference.deleteSharedPreference(HomeActivity.this);
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

    private void closeDrawer() {
        binding.drawerLayout.closeDrawer(GravityCompat.START);
    }


    //-------------------------------------- Location related --------------------------------------
    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }


    private void getUserCurrentLocation() {

        if (checkPermissions()) {
            if (isLocationEnabled()) {
                //AppUtils.showMessageProgress(OutletVerificationActivity.this, "অপেক্ষা করুন ...");
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mFusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                        Location location = task.getResult();
                        if (location == null) {
                            Log.d(TAG, "getUserCurrentLocation: Location is null");
                            //getUserCurrentLocation();

                        } else {
                            Log.d(TAG, "getLastLocation: onComplete: " + location.getLatitude());
                            Log.d(TAG, "getLastLocation: onComplete: " + location.getLongitude());
                            //getAddress(location.getLatitude(), location.getLongitude());

                        }
                        // Always call New Location data because FusedLocationProviderClient hold previous location information.
                        Log.d(TAG, "getUserCurrentLocation: Request new Location");

                        requestNewLocationData();
                    });

                } else {
                    // Permission is not granted. Request for permission
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
                }


            } else {

                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitleText("নির্দেশনা!!!")
                        .setContentText("আপনার ফোনের লোকেশন বন্ধ আছে । অনুগ্রহ করে লোকেশন এনাবল করুন ।")
                        .setConfirmText("এনাবল করুন")
                        .setConfirmClickListener(sDialog -> {
                            Log.d(TAG, "onClick: User Agreed!");
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                            sDialog.dismissWithAnimation();
                        }).show();
                sweetAlertDialog.setCancelable(false);


            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }
    }

    private void requestNewLocationData() {
        AppProgressBar.showMessageProgress(HomeActivity.this, "Wait for a moment...");
        Log.d(TAG, "requestNewLocationData: Request new Location data ");
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(3000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "requestNewLocationData: Request new Location data update");
            //Activate Looper
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());


        } else {
            // Permission is not granted. Request for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_ID);
        }

    }

    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {

            Location mLastLocation = locationResult.getLastLocation();
            Log.d(TAG, "onLocationResult: onComplete: " + mLastLocation.getLatitude());
            Log.d(TAG, "onLocationResult: onComplete: " + mLastLocation.getLongitude());
            double outletLat = mLastLocation.getLatitude();
            double outletLong = mLastLocation.getLongitude();

            //getAddress(outletLat, outletLong);
            Log.d(TAG, "onLocationResult: LAT: " + outletLat);
            Log.d(TAG, "onLocationResult: LONG: " + outletLong);

            if (isScanFaceCircleClicked) {
                checkSupervisorRadius(outletLat, outletLong);
            }

        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            Log.d(TAG, "onLocationAvailability: " + locationAvailability.isLocationAvailable());
        }
    };


    private void checkSupervisorRadius(double userCurrentLat, double userCurrentLong) {

        Log.d(TAG, "checkOutletRadius: User Current Lat: " + userCurrentLat);
        Log.d(TAG, "checkOutletRadius: User Current Long: " + userCurrentLong);

        String superVisorLat = userCredentialPreference.getSuperVisorLatitude();
        String superVisorLong = userCredentialPreference.getSuperVisorLongitude();


        if (superVisorLat != null && superVisorLong != null) {

            if (userCurrentLat > 0 && userCurrentLong > 0) {

                Location startPoint = new Location("locationA");
                startPoint.setLatitude(userCurrentLat);
                startPoint.setLongitude(userCurrentLong);

                Location endPoint = new Location("locationA");
                endPoint.setLatitude(Double.parseDouble(superVisorLat));
                endPoint.setLongitude(Double.parseDouble(superVisorLong));
                double distance = startPoint.distanceTo(endPoint);

                Log.d(TAG, "checkOutletRadius: Distance in Meters: " + distance);

                if (distance <= userCredentialPreference.getSuperVisorRange()) {
                    //AppProgressBar.hideMessageProgress();
                    Log.d(TAG, "checkOutletRadius: Distance in Meters: " + distance);
                    getAddress(userCurrentLat, userCurrentLong);


                } else {
                    AppProgressBar.hideMessageProgress();
                    // mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    isScanFaceCircleClicked = false;

                    //mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                    SweetAlertDialog sDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
                    sDialog.setCancelable(false);
                    sDialog.setTitleText("আপনাকে অবশ্যই " + userCredentialPreference.getSuperVisorWard() + " নাম্বার ওয়ার্ড এর সীমার মধ্যে থাকতে হবে")
                            .setContentText(userCredentialPreference.getSuperVisorWard() + " নাম্বার ওয়ার্ড এর সীমানা থেকে " + String.format(Locale.getDefault(), "%.1f", distance) + " মিটার দূরে আছেন ")
                            .setConfirmText("রিফ্রেশ")
                            .setCancelText("বাতিল")
                            .showCancelButton(false)
                            .setConfirmButtonBackgroundColor(Color.RED)
                            .setConfirmClickListener(sweetAlertDialog -> {
                                Log.d(TAG, "onClick: Stay Here!");
                                //AppProgressBar.showMessageProgress(HomeActivity.this, "লোকেশন রিফ্রেশ হচ্ছে ... ");
                                isScanFaceCircleClicked = true;
                                getUserCurrentLocation();
                                sweetAlertDialog.dismissWithAnimation();
                            }).show();
                }
            } else {
                AppProgressBar.hideMessageProgress();
                isScanFaceCircleClicked = false;
                AppProgressBar.userAttentionPb(this, "আপনার লোকেশন পাওয়া যায়নি আবার চেষ্টা করুন ");
            }

        } else {
            AppProgressBar.hideMessageProgress();
            isScanFaceCircleClicked = false;
            AppProgressBar.userAttentionPb(this, "সুপারভাইজার এর লোকেশন পাওয়া যায়নি সাপোর্ট এ যোগাযোগ করুন ");
        }


    }

    private void getAddress(double latitude, double longitude) {
        Log.d(TAG, "getAddress: called");
        //AppProgressBar.hideMessageProgress();
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                String supervisorAddress = address.getAddressLine(0);
                Intent intent = new Intent(HomeActivity.this, FaceRecognitionActivity.class);
                intent.putExtra(AppConfig.ADDRESS, supervisorAddress);
                intent.putExtra(AppConfig.LATITUDE, latitude);
                intent.putExtra(AppConfig.LONGITUDE, longitude);
                mFusedLocationClient.removeLocationUpdates(mLocationCallback);
                startActivity(intent);
                finish();

            }
        } catch (IOException e) {
            Log.d(TAG, "getAddress: IOException: " + e.getMessage());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


}