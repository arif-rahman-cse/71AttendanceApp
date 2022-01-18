package com.ekattorit.ekattorattendance.ui.login;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityLoginBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.home.HomeActivity;
import com.ekattorit.ekattorattendance.ui.login.model.RpLogin;
import com.ekattorit.ekattorattendance.ui.login.model.RpLoginError;
import com.ekattorit.ekattorattendance.utils.HideKeyboard;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    ActivityLoginBinding binding;
    UserCredentialPreference userCredentialPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        userCredentialPreference = UserCredentialPreference.getPrefarences(LoginActivity.this);


        binding.btnLogin.setOnClickListener(v -> {
            String phone = binding.etPhone.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (validateData(phone, password)) {
                HideKeyboard.hideKeyboard(LoginActivity.this);
                binding.loginPb.setVisibility(View.VISIBLE);
                tryLogin(phone, password);
                disableBtn();

            }

        });

        binding.etPhone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableBtn();

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                enableBtn();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void enableBtn() {
        binding.btnLogin.setClickable(true);
        binding.btnLogin.setBackgroundColor(getResources().getColor(R.color.green));
    }

    private void disableBtn() {
        binding.btnLogin.setClickable(false);
        binding.btnLogin.setBackgroundColor(getResources().getColor(R.color.disable_color));
    }

    private void tryLogin(String phone, String password) {
        Call<RpLogin> loginCall = RetrofitClient.getInstance().getApi().login(phone, password);

        loginCall.enqueue(new Callback<RpLogin>() {
            @Override
            public void onResponse(Call<RpLogin> call, Response<RpLogin> response) {
                binding.loginPb.setVisibility(View.GONE);

                Log.d(TAG, "onResponse: " + response.code());

                if (response.code() == 200 && response.isSuccessful()) {

                    RpLogin rpLogin = response.body();
                    userCredentialPreference.setUserPhone(rpLogin.getUsername());
                    userCredentialPreference.setPassword(password);
                    userCredentialPreference.setName(rpLogin.getFirstName() + " " + rpLogin.getLastName());
                    userCredentialPreference.setUserId(rpLogin.getId());
                    userCredentialPreference.setProfileUrl(rpLogin.getImage());


                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    finish();
                    startActivity(intent);


                } else if (response.code() == 400) {
                    enableBtn();
                    Gson gson = new GsonBuilder().create();
                    RpLoginError loginError;
                    try {
                        loginError = gson.fromJson(response.errorBody().string(), RpLoginError.class);
                        Log.d(TAG, "onResponse: msg: " + loginError);
                        //Toast.makeText(LoginActivity.this, loginError.getNonFieldErrors().get(0), Toast.LENGTH_LONG).show();
                        Snackbar.make(binding.mainView, loginError.getNonFieldErrors().get(0), BaseTransientBottomBar.LENGTH_LONG).show();
                        Log.d(TAG, "onResponse: " + response.errorBody().string());
                    } catch (IOException e) {
                        // handle failure at error parse
                    }


                }
            }

            @Override
            public void onFailure(Call<RpLogin> call, Throwable t) {
                enableBtn();
                binding.loginPb.setVisibility(View.GONE);
                Log.d(TAG, "onResponse: Error");
                Log.e(TAG, "onFailure: " + t.getMessage());
                Toast.makeText(LoginActivity.this, R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
            }
        });


    }


    private boolean validateData(String phone, String password) {
        if (phone.isEmpty()) {
            binding.etPhone.setError("ফোন নম্বর দিন ");
            binding.etPhone.requestFocus();
            return false;

        } else if (phone.length() != 11) {
            //Snackbar.make(binding.mainView, "ফোন নম্বর ১১ ডিজিট হতে হবে", BaseTransientBottomBar.LENGTH_LONG).show();
            binding.etPhone.setError("ফোন নম্বর ১১ ডিজিট হতে হবে");
            binding.etPhone.requestFocus();
            return false;

        } else if (password.isEmpty()) {
            binding.etPassword.setError("পাসওয়ার্ড দিন");
            binding.etPassword.requestFocus();
            return false;
        }
        return true;
    }


}