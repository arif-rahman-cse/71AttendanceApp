package com.ekattorit.ekattorattendance.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivitySplashScreen2Binding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.home.HomeActivity;
import com.ekattorit.ekattorattendance.ui.login.LoginActivity;
import com.ekattorit.ekattorattendance.ui.login.model.RpLogin;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySplashScreen extends AppCompatActivity {
    private static final String TAG = "ActivitySplashScreen";

    private ActivitySplashScreen2Binding binding;
    private UserCredentialPreference userCredentialPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen_2);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash_screen_2);
        userCredentialPreference = UserCredentialPreference.getPreferences(this);

        if (userCredentialPreference.getUserPhone() != null && userCredentialPreference.getPassword() != null) {
            String userPhone = userCredentialPreference.getUserPhone();
            tryLogin(userPhone, userCredentialPreference.getPassword());

        } else if (userCredentialPreference.getUserPhone() == null && userCredentialPreference.getPassword() == null) {
            goLoginScreen();


        }

    }

    private void tryLogin(String phone, String password) {
        binding.splashPb.setVisibility(View.VISIBLE);
        Call<RpLogin> loginCall = RetrofitClient.getInstance().getApi().login(phone, password);

        loginCall.enqueue(new Callback<RpLogin>() {
            @Override
            public void onResponse(Call<RpLogin> call, Response<RpLogin> response) {
                binding.splashPb.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onResponse: " + response.code());

                if (response.code() == 200 && response.isSuccessful()) {

                    RpLogin rpLogin = response.body();
                    assert rpLogin != null;
                    userCredentialPreference.setName(rpLogin.getFirstName() + " " + rpLogin.getLastName());
                    userCredentialPreference.setUserId(rpLogin.getId());
                    userCredentialPreference.setProfileUrl(rpLogin.getProfile().getImage());

                    userCredentialPreference.setUserType(rpLogin.getProfile().getUsersType());
                    userCredentialPreference.setSuperVisorLatitude(rpLogin.getProfile().getSupervisorLatitude());
                    userCredentialPreference.setSuperVisorLongitude(rpLogin.getProfile().getSupervisorLongitude());
                    userCredentialPreference.setSuperVisorRange(rpLogin.getProfile().getRange());
                    userCredentialPreference.setIsFaceRemovePermission(rpLogin.getProfile().isFaceRemovePermission());

                    if (rpLogin.getProfile().getSupervisorWard() == null || rpLogin.getProfile().getSupervisorWard().isEmpty()) {
                        userCredentialPreference.setSuperVisorWard("0");
                    } else {
                        userCredentialPreference.setSuperVisorWard(rpLogin.getProfile().getSupervisorWard());
                    }


                    Intent intent = new Intent(ActivitySplashScreen.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);


                } else {
                    String error = "";
                    try {
                        error = response.errorBody().string();
                        Log.d(TAG, "onResponse: " + error);

                    } catch (IOException e) {
                        // handle failure at error parse
                    }

                    //goLoginScreen();
                    showErrorLogin(error);
                }
            }

            @Override
            public void onFailure(Call<RpLogin> call, Throwable t) {
                binding.splashPb.setVisibility(View.INVISIBLE);
                Log.d(TAG, "onResponse: Error");
                Log.e(TAG, "onFailure: " + t.getMessage());
                showErrorLogin(t.getMessage());


                //goLoginScreen();
                //Toast.makeText(SplashScreen.this, "User not found or Password doesn't match", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showErrorLogin(String error) {
        Log.d(TAG, "showErrorLogin: "+error);
        Snackbar snackbar = Snackbar
                .make(binding.mainView, "Unable to connect server " + error, Snackbar.LENGTH_INDEFINITE)
                .setAction("RETRY", view -> {
                    binding.splashPb.setVisibility(View.VISIBLE);
                    String userPhone = userCredentialPreference.getUserPhone();
                    tryLogin(userPhone, userCredentialPreference.getPassword());
                });

        snackbar.show();
    }

    private void goLoginScreen() {

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}