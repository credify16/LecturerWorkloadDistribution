package com.example.credify.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Lecturer implements Parcelable {
    private String LecturerID;
    private String LecturerName;
    private String password;
    private String Position;
    private String LecturerRole;
    private Double NormalBTSA;
    private Double NormalCredit;
    private String EmploymentType;
    private String DepartmentID;
    private String ProgrammeID;
    private String Email;
    private String auth_user_id;

    // Hardening: Explicit Workload Metadata
    public enum WorkloadScope { ANNUAL, SEMESTER }
    public enum WorkloadCategory { STANDARD, KP, KJ }

    private WorkloadScope targetScope = WorkloadScope.ANNUAL; // Default
    private WorkloadCategory workloadCategory = WorkloadCategory.STANDARD; // Default

    public Lecturer() {}

    public Lecturer(String LecturerID, String LecturerName, String password, String LecturerRole) {
        this.LecturerID = LecturerID;
        this.LecturerName = LecturerName;
        this.password = password;
        this.LecturerRole = LecturerRole;
    }

    protected Lecturer(Parcel in) {
        LecturerID = in.readString();
        LecturerName = in.readString();
        password = in.readString();
        Position = in.readString();
        LecturerRole = in.readString();
        if (in.readByte() == 0) {
            NormalBTSA = null;
        } else {
            NormalBTSA = in.readDouble();
        }
        if (in.readByte() == 0) {
            NormalCredit = null;
        } else {
            NormalCredit = in.readDouble();
        }
        EmploymentType = in.readString();
        DepartmentID = in.readString();
        ProgrammeID = in.readString();
        Email = in.readString();
        auth_user_id = in.readString();
        String scopeStr = in.readString();
        targetScope = scopeStr != null ? WorkloadScope.valueOf(scopeStr) : WorkloadScope.ANNUAL;
        String catStr = in.readString();
        workloadCategory = catStr != null ? WorkloadCategory.valueOf(catStr) : WorkloadCategory.STANDARD;
    }

    public static final Creator<Lecturer> CREATOR = new Creator<Lecturer>() {
        @Override
        public Lecturer createFromParcel(Parcel in) {
            return new Lecturer(in);
        }

        @Override
        public Lecturer[] newArray(int size) {
            return new Lecturer[size];
        }
    };

    public String getLecturerID() { return LecturerID; }
    public void setLecturerID(String LecturerID) { this.LecturerID = LecturerID; }
    
    // Alias for compatibility
    public String getId() { return LecturerID; }
    public void setId(String id) { this.LecturerID = id; }

    public String getLecturerName() { return LecturerName; }
    public void setLecturerName(String LecturerName) { this.LecturerName = LecturerName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPosition() { return Position; }
    public void setPosition(String Position) { this.Position = Position; }

    public String getLecturerRole() { return LecturerRole; }
    public void setLecturerRole(String LecturerRole) { this.LecturerRole = LecturerRole; }

    public Double getNormalBTSA() { return NormalBTSA; }
    public void setNormalBTSA(Double NormalBTSA) { this.NormalBTSA = NormalBTSA; }

    public Double getNormalCredit() { return NormalCredit; }
    public void setNormalCredit(Double NormalCredit) { this.NormalCredit = NormalCredit; }

    public String getEmploymentType() { return EmploymentType; }
    public void setEmploymentType(String EmploymentType) { this.EmploymentType = EmploymentType; }

    public String getDepartmentID() { return DepartmentID; }
    public void setDepartmentID(String DepartmentID) { this.DepartmentID = DepartmentID; }

    public String getProgrammeID() { return ProgrammeID; }
    public void setProgrammeID(String ProgrammeID) { this.ProgrammeID = ProgrammeID; }

    public String getEmail() { return Email; }
    public void setEmail(String Email) { this.Email = Email; }

    public String getAuth_user_id() { return auth_user_id; }
    public void setAuth_user_id(String auth_user_id) { this.auth_user_id = auth_user_id; }

    public WorkloadScope getTargetScope() { return targetScope; }
    public void setTargetScope(WorkloadScope targetScope) { this.targetScope = targetScope; }

    public WorkloadCategory getWorkloadCategory() { return workloadCategory; }
    public void setWorkloadCategory(WorkloadCategory workloadCategory) { this.workloadCategory = workloadCategory; }

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(LecturerID);
        dest.writeString(LecturerName);
        dest.writeString(password);
        dest.writeString(Position);
        dest.writeString(LecturerRole);
        if (NormalBTSA == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(NormalBTSA);
        }
        if (NormalCredit == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(NormalCredit);
        }
        dest.writeString(EmploymentType);
        dest.writeString(DepartmentID);
        dest.writeString(ProgrammeID);
        dest.writeString(Email);
        dest.writeString(auth_user_id);
        dest.writeString(targetScope != null ? targetScope.name() : WorkloadScope.ANNUAL.name());
        dest.writeString(workloadCategory != null ? workloadCategory.name() : WorkloadCategory.STANDARD.name());
    }
}
