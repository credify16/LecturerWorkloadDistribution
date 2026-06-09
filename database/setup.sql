-- Credify Database Setup Script
-- Copy and paste this into the Supabase SQL Editor (https://supabase.com/dashboard/project/_/sql)

-- 1. Create Tables
CREATE TABLE "Department" (
    "DepartmentID" TEXT PRIMARY KEY,
    "DepartmentName" TEXT NOT NULL
);

CREATE TABLE "Programme" (
    "ProgrammeID" TEXT PRIMARY KEY,
    "ProgrammeName" TEXT NOT NULL
);

CREATE TABLE "Campus" (
    "CampusID" TEXT PRIMARY KEY,
    "CampusName" TEXT
);

CREATE TABLE "Semester_Session" (
    "SemSessionID" TEXT PRIMARY KEY,
    "Year" INTEGER NOT NULL,
    "Semester" TEXT NOT NULL,
    "Session" TEXT NOT NULL
);

CREATE TABLE "Admin" (
    "adminId" TEXT PRIMARY KEY,
    "adminName" TEXT NOT NULL,
    "email" TEXT UNIQUE NOT NULL,
    "password" TEXT
);

CREATE TABLE "Lecturer" (
    "LecturerID" TEXT PRIMARY KEY,
    "LecturerName" TEXT NOT NULL,
    "Password" TEXT,
    "Position" TEXT,
    "Role" TEXT,
    "NormalBTSA" DOUBLE PRECISION DEFAULT 0.0,
    "NormalCredit" DOUBLE PRECISION DEFAULT 0.0,
    "EmploymentType" TEXT,
    "DeptID" TEXT REFERENCES "Department"("DepartmentID"),
    "Email" TEXT UNIQUE,
    "ProgrammeID" TEXT REFERENCES "Programme"("ProgrammeID")
);

CREATE TABLE "Course" (
    "CourseCode" TEXT PRIMARY KEY,
    "CourseName" TEXT NOT NULL,
    "Method" TEXT, -- 'K', 'P', 'M', 'R', etc.
    "CreditValue" DOUBLE PRECISION,
    "WeeklyHour" DOUBLE PRECISION,
    "ProgrammeID" TEXT REFERENCES "Programme"("ProgrammeID")
);

CREATE TABLE "Section" (
    "SectionID" TEXT PRIMARY KEY,
    "SectionNumber" TEXT NOT NULL,
    "CampusID" TEXT REFERENCES "Campus"("CampusID"),
    "StudentAmount" INTEGER DEFAULT 0,
    "ProgrammeID" TEXT REFERENCES "Programme"("ProgrammeID"),
    "SemSessionID" TEXT REFERENCES "Semester_Session"("SemSessionID")
);

CREATE TABLE "Assignment" (
    "AssignmentID" TEXT PRIMARY KEY,
    "LecturerID" TEXT REFERENCES "Lecturer"("LecturerID") ON DELETE CASCADE,
    "CourseCode" TEXT REFERENCES "Course"("CourseCode") ON DELETE CASCADE,
    "SectionID" TEXT REFERENCES "Section"("SectionID") ON DELETE CASCADE,
    "LoadPercentage" DOUBLE PRECISION DEFAULT 100.0,
    "Type" TEXT
);

-- 2. Create the Summary View for the App
CREATE OR REPLACE VIEW lecturer_workload_summary AS
WITH assignment_details AS (
    SELECT
        a."LecturerID",
        l."LecturerName",
        l."NormalBTSA",
        l."NormalCredit",
        l."Position",
        l."EmploymentType",
        a."CourseCode",
        c."CreditValue",
        c."Method",
        s."StudentAmount",
        a."LoadPercentage",
        ss."Semester",
        ss."Session",
        ss."Year",
        (c."CreditValue" * (a."LoadPercentage" / 100.0)) as calculated_credits,
        CASE
            WHEN c."Method" = 'P' THEN 1.0
            WHEN c."Method" = 'M' THEN 8.0
            WHEN c."Method" = 'R' THEN 2.0
            ELSE 3.5
        END as A_factor,
        CASE
            WHEN c."Method" IN ('P', 'M', 'R') THEN s."StudentAmount"
            ELSE s."StudentAmount" / 14.0
        END as B_factor,
        CASE
            WHEN c."Method" = 'K' THEN 3.0
            WHEN c."Method" IN ('P', 'M', 'R') THEN 1.0
            ELSE 3.5
        END as C_factor,
        CASE
            WHEN s."StudentAmount" > 40 THEN (s."StudentAmount" - 40) * 0.5
            ELSE 0
        END as extra_btsa
    FROM "Assignment" a
    JOIN "Lecturer" l ON a."LecturerID" = l."LecturerID"
    JOIN "Course" c ON a."CourseCode" = c."CourseCode"
    JOIN "Section" s ON a."SectionID" = s."SectionID"
    JOIN "Semester_Session" ss ON s."SemSessionID" = ss."SemSessionID"
),
btsa_per_course AS (
    SELECT
        *,
        ((A_factor * B_factor * C_factor) + extra_btsa) * (LoadPercentage / 100.0) as calculated_btsa
    FROM assignment_details
)
SELECT
    "LecturerID",
    "LecturerName",
    "Session",
    SUM(calculated_credits) as total_credits,
    SUM(calculated_btsa) as total_btsa,
    "NormalCredit",
    "NormalBTSA",
    SUM(calculated_credits) - "NormalCredit" as credit_difference,
    SUM(calculated_btsa) - "NormalBTSA" as btsa_difference,
    CASE
        WHEN SUM(calculated_btsa) - "NormalBTSA" > 50 THEN 'Overload Warning'
        WHEN SUM(calculated_btsa) - "NormalBTSA" < -50 THEN 'Underload Warning'
        ELSE 'Normal'
    END as status_warning
FROM btsa_per_course
GROUP BY "LecturerID", "LecturerName", "Session", "NormalCredit", "NormalBTSA";
