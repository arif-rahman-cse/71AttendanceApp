package com.ekattorit.ekattorattendance.ui.face;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.ekattorit.ekattorattendance.R;
import com.ekattorit.ekattorattendance.SimilarityClassifier;
import com.ekattorit.ekattorattendance.databinding.ActivityEmployeeListBinding;
import com.ekattorit.ekattorattendance.retrofit.RetrofitClient;
import com.ekattorit.ekattorattendance.ui.face.adpter.EmployeeListAdapter;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.utils.UserCredentialPreference;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

    private HashMap<String, SimilarityClassifier.Recognition> registered = new HashMap<>(); //saved Faces
    int OUTPUT_SIZE = 192; //Output size of model

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
            Intent intent = new Intent(this, EmployeeCardScanActivity.class);
            finish();
            startActivity(intent);
        });

        binding.removeFaceBtn.setOnClickListener(view -> {
            registered = readFromSP(); //Load saved faces from memory when app starts
            updatenameListview();
        });
    }

    //Load Faces from Shared Preferences.Json String to Recognition object
    private HashMap<String, SimilarityClassifier.Recognition> readFromSP() {
        SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
        String defValue = new Gson().toJson(new HashMap<String, SimilarityClassifier.Recognition>());
        String json = sharedPreferences.getString("map", defValue);
        TypeToken<HashMap<String, SimilarityClassifier.Recognition>> token = new TypeToken<HashMap<String, SimilarityClassifier.Recognition>>() {
        };
        HashMap<String, SimilarityClassifier.Recognition> retrievedMap = new Gson().fromJson(json, token.getType());
        // System.out.println("Output map"+retrievedMap.toString());

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

    private void updatenameListview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(EmployeeListActivity.this);
        if (registered.isEmpty()) {
            builder.setTitle("এখনও কোন ফেইস যোগ করা হয়নি!!");
            builder.setView(R.layout.no_data_found);
            builder.setPositiveButton("ঠিক আছে", null);
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            builder.setTitle("ফেইস রিমুভ করতে আইডি নির্বাচন করুন");
            Log.d(TAG, "updatenameListview: " + registered.toString());
            // add a checkbox list
            String[] names = new String[registered.size()];
            boolean[] checkedItems = new boolean[registered.size()];
            int i = 0;
            for (Map.Entry<String, SimilarityClassifier.Recognition> entry : registered.entrySet()) {
                names[i] = entry.getKey();
                checkedItems[i] = false;
                i = i + 1;

            }

            builder.setMultiChoiceItems(names, checkedItems, (dialog, which, isChecked) -> checkedItems[which] = isChecked);


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
                    SharedPreferences sharedPreferences = getSharedPreferences("HashMap", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("map", jsonString);
                    editor.apply();
                    //Toast.makeText(this, "Recognitions Updated", Toast.LENGTH_SHORT).show();
                    Snackbar.make(binding.mainView, "ফেইস আপডেট সফল হয়েছে", Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar snackbar = Snackbar.make(binding.mainView, "ফেইস রিমুভ করতে আইডি নির্বাচন করুন", Snackbar.LENGTH_LONG);
                    View view = snackbar.getView();
                    view.setBackgroundColor(getResources().getColor(R.color.yellow));
                    snackbar.show();
                }


            });
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