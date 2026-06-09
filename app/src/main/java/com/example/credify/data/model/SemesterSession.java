package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SemesterSession implements Parcelable {
    private String SemSessionID;
    private Integer Year;
    private String Semester;
    private String Session;

    public SemesterSession() {}

    public SemesterSession(String SemSessionID, Integer Year, String Semester, String Session) {
        this.SemSessionID = SemSessionID;
        this.Year = Year;
        this.Semester = Semester;
        this.Session = Session;
    }

    protected SemesterSession(Parcel in) {
        SemSessionID = in.readString();
        if (in.readByte() == 0) {
            Year = null;
        } else {
            Year = in.readInt();
        }
        Semester = in.readString();
        Session = in.readString();
    }

    public static final Creator<SemesterSession> CREATOR = new Creator<SemesterSession>() {
        @Override
        public SemesterSession createFromParcel(Parcel in) { return new SemesterSession(in); }
        @Override
        public SemesterSession[] newArray(int size) { return new SemesterSession[size]; }
    };

    public String getSemSessionID() { return SemSessionID; }
    public void setSemSessionID(String SemSessionID) { this.SemSessionID = SemSessionID; }

    public Integer getYear() { return Year; }
    public void setYear(Integer Year) { this.Year = Year; }

    public String getSemester() { return Semester; }
    public void setSemester(String Semester) { this.Semester = Semester; }

    public String getSession() { return Session; }
    public void setSession(String Session) { this.Session = Session; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SemSessionID);
        if (Year == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(Year);
        }
        dest.writeString(Semester);
        dest.writeString(Session);
    }

    @Override
    public String toString() {
        return Session + " " + Year + " (" + Semester + ")";
    }
}
