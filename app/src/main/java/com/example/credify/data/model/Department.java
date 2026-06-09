package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Department implements Parcelable {
    private String DepartmentID;
    private String DepartmentName;

    public Department() {}

    public Department(String DepartmentID, String DepartmentName) {
        this.DepartmentID = DepartmentID;
        this.DepartmentName = DepartmentName;
    }

    protected Department(Parcel in) {
        DepartmentID = in.readString();
        DepartmentName = in.readString();
    }

    public static final Creator<Department> CREATOR = new Creator<Department>() {
        @Override
        public Department createFromParcel(Parcel in) { return new Department(in); }
        @Override
        public Department[] newArray(int size) { return new Department[size]; }
    };

    public String getDepartmentID() { return DepartmentID; }
    public void setDepartmentID(String DepartmentID) { this.DepartmentID = DepartmentID; }
    public String getDepartmentName() { return DepartmentName; }
    public void setDepartmentName(String DepartmentName) { this.DepartmentName = DepartmentName; }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(DepartmentID);
        dest.writeString(DepartmentName);
    }
}
