package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Admin implements Parcelable {
    private String AdminID;
    private String AdminName;
    private String Password;
    private String Email;
    private String auth_user_id;

    public Admin() {}

    public Admin(String AdminID, String AdminName, String Password, String Email, String auth_user_id) {
        this.AdminID = AdminID;
        this.AdminName = AdminName;
        this.Password = Password;
        this.Email = Email;
        this.auth_user_id = auth_user_id;
    }

    protected Admin(Parcel in) {
        AdminID = in.readString();
        AdminName = in.readString();
        Password = in.readString();
        Email = in.readString();
        auth_user_id = in.readString();
    }

    public static final Creator<Admin> CREATOR = new Creator<Admin>() {
        @Override
        public Admin createFromParcel(Parcel in) { return new Admin(in); }
        @Override
        public Admin[] newArray(int size) { return new Admin[size]; }
    };

    public String getAdminID() { return AdminID; }
    public void setAdminID(String AdminID) { this.AdminID = AdminID; }
    public String getAdminName() { return AdminName; }
    public void setAdminName(String AdminName) { this.AdminName = AdminName; }
    public String getPassword() { return Password; }
    public void setPassword(String Password) { this.Password = Password; }
    public String getEmail() { return Email; }
    public void setEmail(String Email) { this.Email = Email; }
    public String getAuth_user_id() { return auth_user_id; }
    public void setAuth_user_id(String auth_user_id) { this.auth_user_id = auth_user_id; }

    @Override public int describeContents() { return 0; }
    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(AdminID);
        dest.writeString(AdminName);
        dest.writeString(Password);
        dest.writeString(Email);
        dest.writeString(auth_user_id);
    }
}
