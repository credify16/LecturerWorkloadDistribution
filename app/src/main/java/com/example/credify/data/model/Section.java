package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Section implements Parcelable {
    private String SectionID;
    private String SectionNumber;
    private String CampusID;
    private String StudentAmount; // Changed to String to support formulas
    private String ProgrammeID;
    private String SemSessionID;

    public Section() {}

    public Section(String SectionID, String SectionNumber, String CampusID, String StudentAmount, String ProgrammeID, String SemSessionID) {
        this.SectionID = SectionID;
        this.SectionNumber = SectionNumber;
        this.CampusID = CampusID;
        this.StudentAmount = StudentAmount;
        this.ProgrammeID = ProgrammeID;
        this.SemSessionID = SemSessionID;
    }

    protected Section(Parcel in) {
        SectionID = in.readString();
        SectionNumber = in.readString();
        CampusID = in.readString();
        StudentAmount = in.readString();
        ProgrammeID = in.readString();
        SemSessionID = in.readString();
    }

    public static final Creator<Section> CREATOR = new Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel in) { return new Section(in); }
        @Override
        public Section[] newArray(int size) { return new Section[size]; }
    };

    public String getSectionID() { return SectionID; }
    public void setSectionID(String SectionID) { this.SectionID = SectionID; }
    
    public String getId() { return SectionID; }

    public String getSectionNumber() { return SectionNumber; }
    public void setSectionNumber(String SectionNumber) { this.SectionNumber = SectionNumber; }

    public String getCampusID() { return CampusID; }
    public void setCampusID(String CampusID) { this.CampusID = CampusID; }

    public String getStudentAmount() { return StudentAmount; }
    public void setStudentAmount(String StudentAmount) { this.StudentAmount = StudentAmount; }

    public String getProgrammeID() { return ProgrammeID; }
    public void setProgrammeID(String ProgrammeID) { this.ProgrammeID = ProgrammeID; }

    public String getSemSessionID() { return SemSessionID; }
    public void setSemSessionID(String SemSessionID) { this.SemSessionID = SemSessionID; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SectionID);
        dest.writeString(SectionNumber);
        dest.writeString(CampusID);
        dest.writeString(StudentAmount);
        dest.writeString(ProgrammeID);
        dest.writeString(SemSessionID);
    }
}
