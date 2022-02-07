package com.ekattorit.ekattorattendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class Data{

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

	@SerializedName("shift")
	private Object shift;

	@SerializedName("latitude")
	private String latitude;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("attendance_date")
	private String attendanceDate;

	@SerializedName("employee")
	private String employee;

	@SerializedName("attendance_count")
	private String attendanceCount;

	@SerializedName("scan_img")
	private Object scanImg;

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

	@SerializedName("scan_by")
	private int scanBy;

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

	public Object getShift(){
		return shift;
	}

	public String getLatitude(){
		return latitude;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getAttendanceDate(){
		return attendanceDate;
	}

	public String getEmployee(){
		return employee;
	}

	public String getAttendanceCount(){
		return attendanceCount;
	}

	public Object getScanImg(){
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

	public int getScanBy(){
		return scanBy;
	}
}