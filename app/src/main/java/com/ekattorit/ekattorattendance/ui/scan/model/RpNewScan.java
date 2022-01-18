package com.ekattorit.ekattorattendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpNewScan {

    @SerializedName("first_scan")
    private String firstScan;

    @SerializedName("last_scan")
    private Object lastScan;

    @SerializedName("shift")
    private String shift;

    @SerializedName("emp_name")
    private String empName;

    @SerializedName("supervisor_name")
    private String supervisorName;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("attendance_date")
    private String attendanceDate;

    @SerializedName("employee")
    private String employee;

    @SerializedName("user_img")
    private Object userImg;

    @SerializedName("word_no")
    private int wordNo;

    @SerializedName("updated_at")
    private String updatedAt;

    @SerializedName("id")
    private int id;

    @SerializedName("supervisor_phone")
    private String supervisorPhone;

    @SerializedName("user")
    private int user;

    @SerializedName("scan_by")
    private int scan_by;

    public int getScan_by() {
        return scan_by;
    }

    public String getFirstScan() {
        return firstScan;
    }

    public Object getLastScan() {
        return lastScan;
    }

    public String getShift() {
        return shift;
    }

    public String getEmpName() {
        return empName;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }

    public String getEmployee() {
        return employee;
    }

    public Object getUserImg() {
        return userImg;
    }

    public int getWordNo() {
        return wordNo;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public int getId() {
        return id;
    }

    public String getSupervisorPhone() {
        return supervisorPhone;
    }

    public int getUser() {
        return user;
    }
}