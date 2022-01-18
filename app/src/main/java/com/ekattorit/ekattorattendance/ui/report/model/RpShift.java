package com.ekattorit.ekattorattendance.ui.report.model;

import com.google.gson.annotations.SerializedName;

public class RpShift{

	@SerializedName("start_time")
	private String startTime;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("end_time")
	private String endTime;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("shift_name")
	private String shiftName;

	@SerializedName("status")
	private boolean status;

	public String getStartTime(){
		return startTime;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public String getEndTime(){
		return endTime;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getShiftName(){
		return shiftName;
	}

	public boolean isStatus(){
		return status;
	}
}