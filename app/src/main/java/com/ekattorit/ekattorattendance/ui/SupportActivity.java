package com.ekattorit.ekattorattendance.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivitySupportBinding;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;


public class SupportActivity extends AppCompatActivity {
    private ActivitySupportBinding binding;
    private UserCredentialPreference userCredentialPreference;
    private static final int REQUEST_PHONE_CALL = 104;
    String phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        binding = DataBindingUtil.setContentView(SupportActivity.this, R.layout.activity_support);
        userCredentialPreference = UserCredentialPreference.getPreferences(SupportActivity.this);
        binding.toolbar.title.setText(getString(R.string.support));

        binding.toolbar.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.row1.setOnClickListener(view -> {
            phone_number = binding.callNumber1.getText().toString();
            getCallPermission();
        });

        binding.row2.setOnClickListener(view -> {
            phone_number = binding.callNumber2.getText().toString();
            getCallPermission();
        });

        binding.row3.setOnClickListener(view -> {
            phone_number = binding.callNumber3.getText().toString();
            getCallPermission();
        });

    }

    private void getCallPermission() {
        if (ContextCompat.checkSelfPermission(SupportActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SupportActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        } else {
            call();
        }
    }

    private void call() {

        String dial = "tel:" + phone_number;
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCallPermission();
                } else {
                    Toast.makeText(SupportActivity.this, "Permission DENIED", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }

    }

}