package com.ekattorit.ekattorattendance.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class ScanItem {

	@SerializedName("address")
	private String address;

	@SerializedName("scan_status")
	private boolean scanStatus;

	@SerializedName("business")
	private String business;

	@SerializedName("first_scan")
	private String firstScan;

	@SerializedName("last_scan")
	private String lastScan;

	@SerializedName("latitude")
	private String latitude;

	@SerializedName("shift")
	private String shift;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("employee")
	private Employee employee;

	@SerializedName("attendance_date")
	private String attendanceDate;

	@SerializedName("attendance_count")
	private String attendanceCount;

	@SerializedName("scan_img")
	private String scanImg;

	@SerializedName("word_no")
	private int wordNo;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("id")
	private int id;

	@SerializedName("user")
	private int user;

	@SerializedName("longitude")
	private String longitude;

	public String getAddress(){
		return address;
	}

	public boolean isScanStatus(){
		return scanStatus;
	}

	public String getBusiness(){
		return business;
	}

	public String getFirstScan(){
		return firstScan;
	}

	public String getLastScan(){
		return lastScan;
	}

	public String getLatitude(){
		return latitude;
	}

	public String getShift(){
		return shift;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public Employee getEmployee(){
		return employee;
	}

	public String getAttendanceDate(){
		return attendanceDate;
	}

	public String getAttendanceCount(){
		return attendanceCount;
	}

	public String getScanImg(){
		return scanImg;
	}

	public int getWordNo(){
		return wordNo;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public int getId(){
		return id;
	}

	public int getUser(){
		return user;
	}

	public String getLongitude(){
		return longitude;
	}
}