package com.ekattorit.ekattorattendance.ui.report;

import static java.util.Locale.US;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivityAttendanceSummaryBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.report.adapter.AttendanceSummaryAdapter;
import com.ekattorit.ekattorattendance.ui.report.model.AttendanceListItem;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendanceSummary;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AttendanceSummary extends AppCompatActivity {
    private static final String TAG = "AttendanceSummary";
    private ActivityAttendanceSummaryBinding binding;
    private UserCredentialPreference userCredentialPreference;
    private List<AttendanceListItem> attendanceSummary = new ArrayList<>();
    private AttendanceSummaryAdapter attendanceSummaryAdapter;
    final Calendar sCalendar = Calendar.getInstance();
    final Calendar eCalendar = Calendar.getInstance();
    final Calendar cCalendar = Calendar.getInstance();
    private String startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_summary);
        binding = DataBindingUtil.setContentView(AttendanceSummary.this, R.layout.activity_attendance_summary);
        userCredentialPreference = UserCredentialPreference.getPrefarences(AttendanceSummary.this);
        binding.toolbar.title.setText(getString(R.string.attendance_summary));

        String currentDate = java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        binding.tvStartDate.setText(currentDate);
        binding.tvEndDate.setText(currentDate);
        formatDate();

        initRecyclerView();


        binding.tvStartDate.setOnClickListener(view -> {

            final DatePickerDialog.OnDateSetListener sDate = (view1, year, monthOfYear, dayOfMonth) -> {
                sCalendar.set(Calendar.YEAR, year);
                sCalendar.set(Calendar.MONTH, monthOfYear);
                sCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDate(AppConfig.START_DATE);
            };
            new DatePickerDialog(
                    this,
                    sDate,
                    sCalendar.get(Calendar.YEAR),
                    sCalendar.get(Calendar.MONTH),
                    sCalendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        binding.tvEndDate.setOnClickListener(view -> {
            final DatePickerDialog.OnDateSetListener sDate = (view1, year, monthOfYear, dayOfMonth) -> {
                eCalendar.set(Calendar.YEAR, year);
                eCalendar.set(Calendar.MONTH, monthOfYear);
                eCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                setDate(AppConfig.END_DATE);
            };
            new DatePickerDialog(
                    this,
                    sDate,
                    eCalendar.get(Calendar.YEAR),
                    eCalendar.get(Calendar.MONTH),
                    eCalendar.get(Calendar.DAY_OF_MONTH))
                    .show();
        });

        binding.btnViewReport.setOnClickListener(view -> {
            AppProgressBar.showMessageProgress(AttendanceSummary.this, "Data Loading...");
            getAttendanceSummary();
        });

        binding.toolbar.backButton.setOnClickListener(view -> {
            onBackPressed();
        });
    }

    private void initRecyclerView() {
        attendanceSummaryAdapter = new AttendanceSummaryAdapter(this, attendanceSummary);
        binding.rvSingleAttendance.setAdapter(attendanceSummaryAdapter);
        binding.rvSingleAttendance.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }


    private void formatDate() {
        int year = cCalendar.get(Calendar.YEAR);
        int month = cCalendar.get(Calendar.MONTH) + 1;
        int day = cCalendar.get(Calendar.DAY_OF_MONTH);

        startDate = year + "-" + month + "-" + day;
        endDate = year + "-" + month + "-" + day;

    }


    private void getAttendanceSummary() {
        Call<RpAttendanceSummary> attendanceCall = RetrofitClient
                .getInstance()
                .getApi()
                .getAttendanceSummary(userCredentialPreference.getUserId(),startDate, endDate);

        attendanceCall.enqueue(new Callback<RpAttendanceSummary>() {
            @Override
            public void onResponse(Call<RpAttendanceSummary> call, Response<RpAttendanceSummary> response) {
                Log.d(TAG, "onResponse: " + response.code());
                AppProgressBar.hideMessageProgress();
                if (response.isSuccessful() && response.code() == 200) {
                    attendanceSummary.clear();

                    if (response.body().getAttendanceList().size() > 0) {
                        binding.errorView.setVisibility(View.GONE);
                        attendanceSummary.addAll(response.body().getAttendanceList());
                        attendanceSummaryAdapter.notifyDataSetChanged();

                    } else {
                        binding.errorView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<RpAttendanceSummary> call, Throwable t) {
                AppProgressBar.hideMessageProgress();
            }
        });
    }

    private void setDate(String date) {
        String myFormat = "dd-MMM-yyyy"; //Date Format
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, US);

        if (date.equals(AppConfig.START_DATE)) {
            binding.tvStartDate.setText(sdf.format(sCalendar.getTime()));

            int year = sCalendar.get(Calendar.YEAR);
            int month = sCalendar.get(Calendar.MONTH) + 1;
            int day = sCalendar.get(Calendar.DAY_OF_MONTH);

            startDate = year + "-" + month + "-" + day;

            // Log.d(TAG, "setDate: Start Date: " + startDate);

        } else {
            binding.tvEndDate.setText(sdf.format(eCalendar.getTime()));

            int year = eCalendar.get(Calendar.YEAR);
            int month = eCalendar.get(Calendar.MONTH) + 1;
            int day = eCalendar.get(Calendar.DAY_OF_MONTH);

            endDate = year + "-" + month + "-" + day;
            //Log.d(TAG, "setDate: End Date: " + endDate);

        }

    }
}