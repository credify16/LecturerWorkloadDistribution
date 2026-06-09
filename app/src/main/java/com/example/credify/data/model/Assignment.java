package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Assignment implements Parcelable {
    private String AssignmentID;
    private String LecturerID;
    private String CourseCode;
    private String SectionID;
    private Double LoadPercentage;
    private String Type;

    public Assignment() {}

    public Assignment(String AssignmentID, String LecturerID, String CourseCode, String SectionID, Double LoadPercentage) {
        this.AssignmentID = AssignmentID;
        this.LecturerID = LecturerID;
        this.CourseCode = CourseCode;
        this.SectionID = SectionID;
        this.LoadPercentage = LoadPercentage;
    }

    public Assignment(String AssignmentID, String LecturerID, String CourseCode, String SectionID, Double LoadPercentage, String Type) {
        this.AssignmentID = AssignmentID;
        this.LecturerID = LecturerID;
        this.CourseCode = CourseCode;
        this.SectionID = SectionID;
        this.LoadPercentage = LoadPercentage;
        this.Type = Type;
    }

    protected Assignment(Parcel in) {
        AssignmentID = in.readString();
        LecturerID = in.readString();
        CourseCode = in.readString();
        SectionID = in.readString();
        if (in.readByte() == 0) {
            LoadPercentage = null;
        } else {
            LoadPercentage = in.readDouble();
        }
        Type = in.readString();
    }

    public static final Creator<Assignment> CREATOR = new Creator<Assignment>() {
        @Override
        public Assignment createFromParcel(Parcel in) { return new Assignment(in); }
        @Override
        public Assignment[] newArray(int size) { return new Assignment[size]; }
    };

    public String getAssignmentID() { return AssignmentID; }
    public void setAssignmentID(String AssignmentID) { this.AssignmentID = AssignmentID; }

    public String getLecturerID() { return LecturerID; }
    public void setLecturerID(String LecturerID) { this.LecturerID = LecturerID; }

    public String getCourseCode() { return CourseCode; }
    public void setCourseCode(String CourseCode) { this.CourseCode = CourseCode; }

    public String getSectionID() { return SectionID; }
    public void setSectionID(String SectionID) { this.SectionID = SectionID; }

    public Double getLoadPercentage() { return LoadPercentage; }
    public void setLoadPercentage(Double LoadPercentage) { this.LoadPercentage = LoadPercentage; }

    public String getType() { return Type; }
    public void setType(String Type) { this.Type = Type; }

    // Aliases for compatibility with existing code
    public String getId() { return AssignmentID; }
    public String getLecturerId() { return LecturerID; }
    public String getCourseId() { return CourseCode; }
    public String getSectionId() { return SectionID; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(AssignmentID);
        dest.writeString(LecturerID);
        dest.writeString(CourseCode);
        dest.writeString(SectionID);
        if (LoadPercentage == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(LoadPercentage);
        }
        dest.writeString(Type);
    }
}
