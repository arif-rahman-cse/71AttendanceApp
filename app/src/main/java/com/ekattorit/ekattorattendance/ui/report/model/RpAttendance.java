package com.ekattorit.ekattorattendance.ui.report.model;

import com.google.gson.annotations.SerializedName;

public class RpAttendance{

	@SerializedName("first_scan")
	private String firstScan;

	@SerializedName("xyear")
	private int xyear;

	@SerializedName("last_scan")
	private String lastScan;

	@SerializedName("shift")
	private String shift;

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("last_name")
	private String lastName;

	@SerializedName("xmonthname")
	private String xmonthname;

	@SerializedName("xdate")
	private String xdate;

	@SerializedName("xmonth")
	private int xmonth;

	@SerializedName("word_no")
	private int wordNo;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("user_id")
	private int userId;

	@SerializedName("xday")
	private String xday;

	@SerializedName("is_offday")
	private String isOffday;

	@SerializedName("first_name")
	private String firstName;

	@SerializedName("emp_id")
	private String empId;

	@SerializedName("status")
	private String status;

	public String getFirstScan(){
		return firstScan;
	}

	public int getXyear(){
		return xyear;
	}

	public String getLastScan(){
		return lastScan;
	}

	public String getShift(){
		return shift;
	}

	public String getEmpName(){
		return empName;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getLastName(){
		return lastName;
	}

	public String getXmonthname(){
		return xmonthname;
	}

	public String getXdate(){
		return xdate;
	}

	public int getXmonth(){
		return xmonth;
	}

	public int getWordNo(){
		return wordNo;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public int getUserId(){
		return userId;
	}

	public String getXday(){
		return xday;
	}

	public String getIsOffday(){
		return isOffday;
	}

	public String getFirstName(){
		return firstName;
	}

	public String getEmpId(){
		return empId;
	}

	public String getStatus(){
		return status;
	}
}