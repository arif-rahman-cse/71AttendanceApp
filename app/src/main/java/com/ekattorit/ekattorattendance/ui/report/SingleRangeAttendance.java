package com.ekattorit.ekattorattendance.ui.report;

import static java.util.Locale.US;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.databinding.ActivitySingleRangeAttendanceBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.report.adapter.SingleEmployeeAttendanceAdapter;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendance;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.utils.AppConfig;
import com.ekattorit.ekattorattendance.utils.AppProgressBar;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SingleRangeAttendance extends AppCompatActivity {
    private static final String TAG = "SingleRangeAttendance";
    private ActivitySingleRangeAttendanceBinding binding;
    private UserCredentialPreference userCredentialPreference;

    private ArrayList<String> employeeList = null;
    private List<Map<String, Object>> employeeMap = new ArrayList<>();

    final Calendar sCalendar = Calendar.getInstance();
    final Calendar eCalendar = Calendar.getInstance();
    final Calendar cCalendar = Calendar.getInstance();
    private String startDate, endDate;

    private List<RpAttendance> attendanceList = new ArrayList<>();
    private SingleEmployeeAttendanceAdapter singleEmployeeAttendanceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_range_attendance);
        binding = DataBindingUtil.setContentView(SingleRangeAttendance.this, R.layout.activity_single_range_attendance);
        binding.toolbar.title.setText(getString(R.string.single_employee_attendance_status));
        userCredentialPreference = UserCredentialPreference.getPrefarences(SingleRangeAttendance.this);

        String currentDate = java.text.DateFormat.getDateInstance().format(Calendar.getInstance().getTime());
        binding.tvStartDate.setText(currentDate);
        binding.tvEndDate.setText(currentDate);

        formatDate();


        initRecyclerView();
        getEmployee(userCredentialPreference.getUserId());

        binding.toolbar.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.btnViewReport.setOnClickListener(view -> {

            if (employeeList != null && binding.spEmployee.getSelectedItemPosition() > 0) {
                Log.d(TAG, "onCreate: Ready..");

                if (!isDateAfter(endDate)) {
                    AppProgressBar.showMessageProgress(SingleRangeAttendance.this, "Data Loading...");
                    getSingleEmployeeReport(employeeMap.get(binding.spEmployee.getSelectedItemPosition()).get("EmployeeId").toString());

                } else {
                    Snackbar.make(binding.mainView, getString(R.string.date_validation), Snackbar.LENGTH_LONG).show();
                }


            } else {
                Log.d(TAG, "onCreate: Still Loading Shift and word");
                Snackbar.make(binding.mainView, getString(R.string.select_employee), Snackbar.LENGTH_LONG).show();
            }

        });

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

        binding.btnGeneratePdf.setOnClickListener(v -> {
            Snackbar.make(binding.mainView, "Under Development", BaseTransientBottomBar.LENGTH_LONG).show();
        });


    }


    public static boolean isDateAfter(String selectedDate) {
        try {
            String myFormatString = "yyyy-MM-dd"; // for example
            SimpleDateFormat df = new SimpleDateFormat(myFormatString);

            if (df.parse(selectedDate).getTime() > System.currentTimeMillis())
                return true;
            else
                return false;
        } catch (Exception e) {

            return false;
        }
    }

    private void formatDate() {

        int year = cCalendar.get(Calendar.YEAR);
        int month = cCalendar.get(Calendar.MONTH) + 1;
        int day = cCalendar.get(Calendar.DAY_OF_MONTH);

        startDate = year + "-" + month + "-" + day;
        endDate = year + "-" + month + "-" + day;

    }


    private void initRecyclerView() {
        singleEmployeeAttendanceAdapter = new SingleEmployeeAttendanceAdapter(this, attendanceList);
        binding.rvSingleAttendance.setAdapter(singleEmployeeAttendanceAdapter);
        binding.rvSingleAttendance.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    }


    private void getSingleEmployeeReport(String employeeId) {
        Log.d(TAG, "getSingleEmployeeReport: Employee Id: "+ employeeId);
        Call<List<RpAttendance>> attendanceCall = RetrofitClient
                .getInstance()
                .getApi()
                .getSingleEmployeeAttendance(startDate, endDate, userCredentialPreference.getUserId(), employeeId);

        attendanceCall.enqueue(new Callback<List<RpAttendance>>() {
            @Override
            public void onResponse(Call<List<RpAttendance>> call, Response<List<RpAttendance>> response) {
                Log.d(TAG, "onResponse: " + response.code());
                AppProgressBar.hideMessageProgress();
                if (response.isSuccessful() && response.code() == 200) {
                    attendanceList.clear();

                    if (response.body().size() > 0) {
                        int present = 0, absent = 0;
                        binding.errorView.setVisibility(View.GONE);
                        attendanceList.addAll(response.body());
                        singleEmployeeAttendanceAdapter.notifyDataSetChanged();

                        for (RpAttendance attendance : attendanceList) {
                            if (attendance.getStatus().equals("P")) {
                                present = present + 1;
                            } else {
                                absent = absent + 1;
                            }
                        }
                        binding.tvPresent.setText(MessageFormat.format("Present: {0}", present));
                        binding.tvAbsent.setText(MessageFormat.format("Absent: {0}", absent));

                    } else {
                        binding.errorView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RpAttendance>> call, Throwable t) {
                AppProgressBar.hideMessageProgress();
            }
        });
    }

    private void getEmployee(int userId) {

        Call<List<RpEmpDetails>> rpShift = RetrofitClient.getInstance().getApi().getEmployee(userId);
        rpShift.enqueue(new Callback<List<RpEmpDetails>>() {
            @Override
            public void onResponse(Call<List<RpEmpDetails>> call, Response<List<RpEmpDetails>> response) {
                Log.d(TAG, "onResponse: Code Word:" + response.code());
                if (response.isSuccessful() && response.code() == 200) {

                    employeeMap = new ArrayList<>();
                    employeeList = new ArrayList<>();
                    Map<String, Object> map = new HashMap<>();
                    map.put("EmployeeId", "-- SELECT EMPLOYEE --");
                    employeeMap.add(map);
                    employeeList.add("-- SELECT EMPLOYEE --");

                    if (!response.body().isEmpty()) {

                        for (int i = 0; i < response.body().size(); i++) {
                            Log.d(TAG, "onResponse: WordNo: " + response.body().get(i).getWordNo());
                            map = new HashMap<>();
                            map.put("EmployeeId", response.body().get(i).getEmpId());
                            employeeMap.add(map);
                            employeeList.add(response.body().get(i).getEmpId() + " - " + response.body().get(i).getEmpName());

                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(SingleRangeAttendance.this, android.R.layout.simple_spinner_item, employeeList);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spEmployee.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RpEmpDetails>> call, Throwable t) {
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
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