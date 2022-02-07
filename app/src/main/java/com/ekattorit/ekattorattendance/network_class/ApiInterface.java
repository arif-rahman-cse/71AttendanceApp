package com.ekattorit.ekattorattendance.network_class;

import com.ekattorit.ekattorattendance.ui.home.model.RpRecentScan;
import com.ekattorit.ekattorattendance.ui.login.model.RpLogin;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendance;
import com.ekattorit.ekattorattendance.ui.report.model.RpAttendanceSummary;
import com.ekattorit.ekattorattendance.ui.report.model.RpShift;
import com.ekattorit.ekattorattendance.ui.report.model.RpWord;
import com.ekattorit.ekattorattendance.ui.scan.model.RpEmpDetails;
import com.ekattorit.ekattorattendance.ui.scan.model.RpNewScan;
import com.ekattorit.ekattorattendance.ui.scan.model.RpNewScan2;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @FormUrlEncoded
    @POST("account/login/")
    Call<RpLogin> login(
            @Field("username") String phoneNumber,
            @Field("password") String password
    );

    @GET("employee/details/{employee_id}")
    Call<RpEmpDetails> getEmpDetails(
            @Path("employee_id") String empId
    );


    @GET("employee/list/{user_id}")
    Call<List<RpEmpDetails>> getEmployee(
            @Path("user_id") int userId
    );

    @GET("recent_attendance/list")
    Call<RpRecentScan> getRecentScan(
            @Query("scan_by") int scan_by
    );

    @GET("shift/list")
    Call<List<RpShift>> getShift();

    @GET("current/shift")
    Call<RpShift> getCurrentShift();

    @GET("word/list/")
    Call<List<RpWord>> getWord(
            @Query("user") int supervisor_id
    );

    @GET("report/par_day_attendance/")
    Call<List<RpAttendance>> getPerDayAttendance(
            @Query("xdate") String date,
            @Query("user_id") int user_id
    );

    @GET("report/par_day_attendance")
    Call<List<RpAttendance>> getPerDayAttendanceByShift(
            @Query("user_id") int supervisor_id,
            @Query("shift_id") String shift
    );


    @Multipart
    @POST("attendance/new")
    Call<RpNewScan2> addNewAttendance(
            //@Part("business") String business_id,
            //@Part("user") int superVisor,
            //@Part("word_no") String wordNo,
            @Part("employee") String employeeId,
            @Part("latitude") double latitude,
            @Part("longitude") double longitude,
            //@Part("attendance_count") double attendance_count,
            @Part("address") String address,
            //@Part("scan_status") boolean scan_status,
            //@Part MultipartBody.Part employee_img,
            @Part("scan_by") int scan_by

    );

    @FormUrlEncoded
    @PUT("attendance/update")
    Call<RpNewScan> updateAttendance(
            @Field("employee") String employeeId,
            @Field("shift") String shiftName,
            @Field("attendance_date") String date

    );

    @GET("report/range_attendance_single/{start_date}/{end_date}/{user_id}/{emp_id}")
    Call<List<RpAttendance>> getSingleEmployeeAttendance(
            @Path("start_date") String start_date,
            @Path("end_date") String end_date,
            @Path("user_id") int user_id,
            @Path("emp_id") String emp_id
    );


    @GET("report/attendance_summery/")
    Call<RpAttendanceSummary> getAttendanceSummary(
            @Query("user_id") int supervisor_id,
            @Query("start_date") String start_date,
            @Query("end_date") String end_date
    );


}
