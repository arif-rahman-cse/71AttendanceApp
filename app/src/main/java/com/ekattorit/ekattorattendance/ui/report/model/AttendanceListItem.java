package com.ekattorit.ekattorattendance.ui.report.model;

import com.google.gson.annotations.SerializedName;

public class AttendanceListItem{

	@SerializedName("total_present")
	private double totalPresent;

	@SerializedName("word_no")
	private int wordNo;

	@SerializedName("working_day")
	private int workingDay;

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("total_absent")
	private double totalAbsent;

	@SerializedName("emp_id")
	private String empId;

	public double getTotalPresent(){
		return totalPresent;
	}

	public int getWordNo(){
		return wordNo;
	}

	public int getWorkingDay(){
		return workingDay;
	}

	public String getEmpName(){
		return empName;
	}

	public double getTotalAbsent(){
		return totalAbsent;
	}

	public String getEmpId(){
		return empId;
	}
}