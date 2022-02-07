package com.ekattorit.ekattorattendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpNewScan2{

	@SerializedName("data")
	private Data data;

	@SerializedName("employee_name")
	private String employeeName;

	public Data getData(){
		return data;
	}

	public String getEmployeeName(){
		return employeeName;
	}
}