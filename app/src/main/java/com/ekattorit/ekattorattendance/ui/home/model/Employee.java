package com.ekattorit.ekattorattendance.ui.home.model;

import com.google.gson.annotations.SerializedName;

public class Employee{

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("emp_id")
	private String empId;

	public String getEmpName(){
		return empName;
	}

	public String getEmpId(){
		return empId;
	}
}