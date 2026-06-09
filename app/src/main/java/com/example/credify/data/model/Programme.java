package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Programme implements Parcelable {
    private String ProgrammeID;
    private String ProgrammeName;

    public Programme() {}

    public Programme(String ProgrammeID, String ProgrammeName) {
        this.ProgrammeID = ProgrammeID;
        this.ProgrammeName = ProgrammeName;
    }

    protected Programme(Parcel in) {
        ProgrammeID = in.readString();
        ProgrammeName = in.readString();
    }

    public static final Creator<Programme> CREATOR = new Creator<Programme>() {
        @Override
        public Programme createFromParcel(Parcel in) { return new Programme(in); }
        @Override
        public Programme[] newArray(int size) { return new Programme[size]; }
    };

    public String getProgrammeID() { return ProgrammeID; }
    public void setProgrammeID(String ProgrammeID) { this.ProgrammeID = ProgrammeID; }
    public String getProgrammeName() { return ProgrammeName; }
    public void setProgrammeName(String ProgrammeName) { this.ProgrammeName = ProgrammeName; }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ProgrammeID);
        dest.writeString(ProgrammeName);
    }
}
