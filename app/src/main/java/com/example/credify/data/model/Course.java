package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Course implements Parcelable {
    private String CourseCode;
    private String CourseName;
    private String Method;
    private Double CreditValue;
    private Double WeeklyHour;
    private String ProgrammeID;

    public Course() {}

    public Course(String CourseCode, String CourseName, String Method, Double CreditValue, Double WeeklyHour, String ProgrammeID) {
        this.CourseCode = CourseCode;
        this.CourseName = CourseName;
        this.Method = Method;
        this.CreditValue = CreditValue;
        this.WeeklyHour = WeeklyHour;
        this.ProgrammeID = ProgrammeID;
    }

    protected Course(Parcel in) {
        CourseCode = in.readString();
        CourseName = in.readString();
        Method = in.readString();
        if (in.readByte() == 0) CreditValue = null; else CreditValue = in.readDouble();
        if (in.readByte() == 0) WeeklyHour = null; else WeeklyHour = in.readDouble();
        ProgrammeID = in.readString();
    }

    public static final Creator<Course> CREATOR = new Creator<Course>() {
        @Override
        public Course createFromParcel(Parcel in) { return new Course(in); }
        @Override
        public Course[] newArray(int size) { return new Course[size]; }
    };

    public String getCourseCode() { return CourseCode; }
    public void setCourseCode(String CourseCode) { this.CourseCode = CourseCode; }
    public String getCourseName() { return CourseName; }
    public void setCourseName(String CourseName) { this.CourseName = CourseName; }
    public String getMethod() { return Method; }
    public void setMethod(String Method) { this.Method = Method; }
    public Double getCreditValue() { return CreditValue; }
    public void setCreditValue(Double CreditValue) { this.CreditValue = CreditValue; }
    public Double getWeeklyHour() { return WeeklyHour; }
    public void setWeeklyHour(Double WeeklyHour) { this.WeeklyHour = WeeklyHour; }
    public String getProgrammeID() { return ProgrammeID; }
    public void setProgrammeID(String ProgrammeID) { this.ProgrammeID = ProgrammeID; }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CourseCode);
        dest.writeString(CourseName);
        dest.writeString(Method);
        if (CreditValue == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(CreditValue); }
        if (WeeklyHour == null) dest.writeByte((byte) 0); else { dest.writeByte((byte) 1); dest.writeDouble(WeeklyHour); }
        dest.writeString(ProgrammeID);
    }
}
