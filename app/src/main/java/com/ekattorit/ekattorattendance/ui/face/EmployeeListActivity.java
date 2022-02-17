package com.ekattorit.ekattorattendance.ui.face;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.SimilarityClassifier;
import com.ekattorit.ekattorattendance.databinding.ActivityEmployeeListBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.face.adpter.EmployeeListAdapter;
import com.ekattorit.ekattorattendance.ui.face.model.RpUpFace;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.utils.EmployeeFacePreference;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
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
    private EmployeeFacePreference employeeFacePreference;

    private List<RpEmpDetails> empDetailsList = new ArrayList<>();
    private EmployeeListAdapter employeeListAdapter;

    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    int OUTPUT_SIZE = 192; //Output size of model

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_list);
        binding = DataBindingUtil.setContentView(EmployeeListActivity.this, R.layout.activity_employee_list);
        userCredentialPreference = UserCredentialPreference.getPreferences(EmployeeListActivity.this);
        employeeFacePreference = EmployeeFacePreference.getPreferences(EmployeeListActivity.this);
        binding.title.setText(getString(R.string.employee_list));

        if (!userCredentialPreference.getIsFaceRemovePermission()) {
            binding.removeFaceBtn.setImageResource(R.drawable.ic_menu);
        }

        initRecyclerView();
        getEmployee(userCredentialPreference.getUserId());

        binding.backButton.setOnClickListener(view -> {
            onBackPressed();
        });

        binding.addNewFaceBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, EmployeeCardScanActivity.class);
            finish();
            startActivity(intent);
        });

        binding.removeFaceBtn.setOnClickListener(view -> {
            registered = readFromSP();
            updatenameListview(userCredentialPreference.getIsFaceRemovePermission());

        });


        binding.ivUploadFace.setOnClickListener(view -> {
            String jsonString = new Gson().toJson(readFromSP());
            if (jsonString != null && !jsonString.isEmpty()) {
                uploadFace(jsonString, true);
            }

        });
    }


    private void uploadFace(String jsonString, boolean isUpload) {
        Log.d(TAG, "uploadFace: JSON face: " + jsonString);

        Call<RpUpFace> newScanCall = RetrofitClient
                .getInstance()
                .getApi()
                .uploadFace(userCredentialPreference.getUserId(), jsonString);

        newScanCall.enqueue(new Callback<RpUpFace>() {
            @Override
            public void onResponse(Call<RpUpFace> call, Response<RpUpFace> response) {
                Log.d(TAG, "onResponse: " + response.code());
                //Log.d(TAG, "onResponse: data: "+ response.body().toString());

                if (response.code() == 200) {
                    employeeFacePreference.setEmpFace(jsonString);
                    Snackbar snackbar;
                    if (isUpload){
                        snackbar = Snackbar.make(binding.mainView, "ফেইস আপলোড  সফল হয়েছে", Snackbar.LENGTH_SHORT);
                        View view = snackbar.getView();
                        view.setBackgroundColor(getResources().getColor(R.color.green_deep));
                    }else {
                        snackbar = Snackbar.make(binding.mainView, "ফেইস আপডেট সফল হয়েছে", Snackbar.LENGTH_SHORT);
                        View view = snackbar.getView();
                        view.setBackgroundColor(getResources().getColor(R.color.green_deep));
                    }
                    snackbar.show();

                } else {
                    try {
                        Log.d(TAG, "onResponse: Error: " + response.errorBody().string());
                        Toast.makeText(EmployeeListActivity.this, " কিছু একটা সমস্যা হয়েছে " + response.errorBody().string(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<RpUpFace> call, Throwable t) {
                Toast.makeText(EmployeeListActivity.this, " কিছু একটা সমস্যা হয়েছে " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onFailure: Error: " + t.getMessage());
            }
        });

    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP() {
        String json = employeeFacePreference.getEmpFace();
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {};
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());

        //During type conversion and save/load procedure,format changes(eg float converted to double).
        //So embeddings need to be extracted from it in required format(eg.double to float).
        for (Map.Entry<String, SimilarityClassifier.Recognition> entry : retrievedMap.entrySet()) {
            float[][] output = new float[1][OUTPUT_SIZE];
            ArrayList arrayList = (ArrayList) entry.getValue().getExtra();
            arrayList = (ArrayList) arrayList.get(0);
            for (int counter = 0; counter < arrayList.size(); counter++) {
                output[0][counter] = ((Double) arrayList.get(counter)).floatValue();
            }
            entry.getValue().setExtra(output);

        }
        //Toast.makeText(this, "Recognitions Loaded", Toast.LENGTH_SHORT).show();
        return retrievedMap;
    }

    private void updatenameListview(boolean isFaceRemovePermission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeListActivity.this);
        if (registered.isEmpty()) {

            builder.setTitle("কোন ফেইস যোগ করা হয়নি!");
            builder.setView(R.layout.no_data_found);
            builder.setPositiveButton("ঠিক আছে", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            builder.setTitle("যাচাইকৃত কর্মী তালিকা");
            //Log.d(TAG, "updatenameListview: " + registered.toString());
            // add a checkbox list
            String[] names = new String[registered.size()];
            String[] employees = new String[registered.size()];
            boolean[] checkedItems = new boolean[registered.size()];
            int i = 0;
            for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {
                names[i] = entry.getKey();
                String emp = entry.getValue().toString();
                String empName = emp.substring(emp.indexOf("]") + 1, emp.indexOf('('));
                employees[i] = entry.getKey() + "\n" + empName;
                checkedItems[i] = false;
                i = i + 1;

            }

            builder.setMultiChoiceItems(employees, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);

            if (isFaceRemovePermission) {
                builder.setPositiveButton("রিমুভ", (dialog, which) -> {
                    int count = 0;
                    for (int i1 = 0; i1 < checkedItems.length; i1++) {
                        Log.d(TAG, "updatenameListview: fff: " + checkedItems[i1]);
                        if (checkedItems[i1]) {
                            registered.remove(names[i1]);
                            count += 1;
                        }

                    }

                    if (count > 0) {
                        String jsonString = new Gson().toJson(registered);
                        uploadFace(jsonString, false);
                    } else {
                        Snackbar snackbar = Snackbar.make(binding.mainView, "ফেইস রিমুভ করতে আইডি নির্বাচন করুন", Snackbar.LENGTH_LONG);
                        View view = snackbar.getView();
                        view.setBackgroundColor(getResources().getColor(R.color.yellow));
                        snackbar.show();
                    }


                });
            } else {
                Log.d(TAG, "updatenameListview: remove permission does not allowed");
            }

            builder.setNegativeButton("বাতিল", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
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