package com.ekattorit.ekattorattendance.ui.report.model;

import com.google.gson.annotations.SerializedName;

public class RpWord{

	@SerializedName("word_no")
	private int wordNo;

	@SerializedName("emp_id")
	private String empId;

	public int getWordNo(){
		return wordNo;
	}

	public String getEmpId(){
		return empId;
	}
}