package com.ekattorit.ekattorattendance.ui.scan.model;

import com.google.gson.annotations.SerializedName;

public class RpEmpDetails{

	@SerializedName("address")
	private String address;

	@SerializedName("business")
	private String business;

	@SerializedName("join_date")
	private String joinDate;

	@SerializedName("emp_name")
	private String empName;

	@SerializedName("mobile")
	private String mobile;

	@SerializedName("nid")
	private String nid;

	@SerializedName("created_at")
	private String createdAt;

	@SerializedName("emp_father_name")
	private String empFatherName;

	@SerializedName("employee_img")
	private String employeeImg;

	@SerializedName("word_no")
	private String wordNo;

	@SerializedName("updated_at")
	private String updatedAt;

	@SerializedName("resign_date")
	private Object resignDate;

	@SerializedName("location")
	private Object location;

	@SerializedName("designation")
	private String designation;

	@SerializedName("user")
	private int user;

	@SerializedName("emp_id")
	private String empId;

	@SerializedName("status")
	private boolean status;

	@SerializedName("attendance_type")
	private boolean attendanceType;


	@SerializedName("emp_name_en")
	private String emp_name_en;

	@SerializedName("blood_group")
	private String blood_group;

	@SerializedName("is_face_added")
	private boolean is_face_added;

	@SerializedName("is_finger_added")
	private boolean is_finger_added;

	@SerializedName("gender")
	private String gender;

	@SerializedName("date_of_birth")
	private String date_of_birth;

	@SerializedName("proxy_supervisor")
	private int proxy_supervisor;



	public String getAddress(){
		return address;
	}

	public String getBusiness(){
		return business;
	}

	public String getJoinDate(){
		return joinDate;
	}

	public String getEmpName(){
		return empName;
	}

	public String getMobile(){
		return mobile;
	}

	public String getNid(){
		return nid;
	}

	public String getCreatedAt(){
		return createdAt;
	}

	public String getEmpFatherName(){
		return empFatherName;
	}

	public String getEmployeeImg(){
		return employeeImg;
	}

	public String getWordNo(){
		return wordNo;
	}

	public String getUpdatedAt(){
		return updatedAt;
	}

	public Object getResignDate(){
		return resignDate;
	}

	public Object getLocation(){
		return location;
	}

	public String getDesignation(){
		return designation;
	}

	public int getUser(){
		return user;
	}

	public String getEmpId(){
		return empId;
	}

	public boolean isStatus(){
		return status;
	}

	public boolean isAttendanceType(){
		return attendanceType;
	}

    public String getEmp_name_en() {
        return emp_name_en;
    }

    public String getBlood_group() {
        return blood_group;
    }

    public boolean isIs_face_added() {
        return is_face_added;
    }

    public boolean isIs_finger_added() {
        return is_finger_added;
    }

    public String getGender() {
        return gender;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public int getProxy_supervisor() {
        return proxy_supervisor;
    }
}