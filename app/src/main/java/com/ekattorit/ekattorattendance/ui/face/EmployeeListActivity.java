package com.ekattorit.ekattorattendance.ui.face;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.ekattorit.ekattorattendance.MainActivity;
import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityEmployeeListBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.splash_screen;
import com.ekattorit.ekattorattendance.ui.report.SingleRangeAttendance;
import com.ekattorit.ekattorattendance.ui.report.adapter.DailyAttendanceAdapter;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendance;
import com.ekattorit.ekattorattendance.ui.scan.CardScanActivity;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeListActivity extends AppCompatActivity {
    private static final String TAG = "EmployeeListActivity";
    ActivityEmployeeListBinding binding;
    private UserCredentialPreference userCredentialPreference;

    private List<RpEmpDetails> empDetailsList = new ArrayList<>();
    private EmployeeListAdapter employeeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        binding = DataBindingUtil.setContentView(EmployeeListActivity.this, R.layout.activity_employee_list);
        userCredentialPreference = UserCredentialPreference.getPrefarences(EmployeeListActivity.this);
        binding.title.setText(getString(R.string.employee_list));

        initRecyclerView();
        getEmployee(userCredentialPreference.getUserId());

        binding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.addNewFaceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, CardScanActivity.class);
            finish();
            startActivity(intent);
        });
    }

    private void getEmployee(int userId) {

        Call<List<RpEmpDetails>> rpShift = RetrofitClient.getInstance().getApi().getEmployee(userId);
        rpShift.enqueue(new Callback<List<RpEmpDetails>>() {
            @Override
            public void onResponse(Call<List<RpEmpDetails>> call, Response<List<RpEmpDetails>> response) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onResponse: Code Word:" + response.code());
                if (response.isSuccessful() && response.code() == 200) {
                    empDetailsList.clear();
                    assert response.body() != null;
                    if (response.body().size() > 0) {
                        binding.errorView.setVisibility(View.GONE);
                        empDetailsList.addAll(response.body());
                        employeeListAdapter.notifyDataSetChanged();

                    } else {
                        binding.errorView.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onFailure(Call<List<RpEmpDetails>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
            }
        });
    }

    private void initRecyclerView() {
        employeeListAdapter = new EmployeeListAdapter(this, empDetailsList);
        binding.rvEmployeeList.setAdapter(employeeListAdapter);
        binding.rvEmployeeList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }


}