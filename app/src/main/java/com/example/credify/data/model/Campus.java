package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Campus implements Parcelable {
    private String CampusID;
    private String CampusName;

    public Campus() {}

    public Campus(String CampusID, String CampusName) {
        this.CampusID = CampusID;
        this.CampusName = CampusName;
    }

    protected Campus(Parcel in) {
        CampusID = in.readString();
        CampusName = in.readString();
    }

    public static final Creator<Campus> CREATOR = new Creator<Campus>() {
        @Override
        public Campus createFromParcel(Parcel in) { return new Campus(in); }
        @Override
        public Campus[] newArray(int size) { return new Campus[size]; }
    };

    public String getCampusID() { return CampusID; }
    public void setCampusID(String CampusID) { this.CampusID = CampusID; }
    public String getCampusName() { return CampusName; }
    public void setCampusName(String CampusName) { this.CampusName = CampusName; }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(CampusID);
        dest.writeString(CampusName);
    }
}
