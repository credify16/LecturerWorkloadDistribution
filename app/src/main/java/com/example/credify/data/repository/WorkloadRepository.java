package com.example.credify.data.repository;

import android.util.Log;
import com.example.credify.data.model.*;
import com.example.credify.data.remote.SupabaseAssignment;
import com.example.credify.utils.WorkloadCalculator;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import kotlin.coroutines.EmptyCoroutineContext;
import kotlinx.coroutines.BuildersKt;

public class WorkloadRepository {

    public CompletableFuture<List<AssignmentDetail>> getAllAssignmentDetails() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String data = BuildersKt.runBlocking(
                        EmptyCoroutineContext.INSTANCE,
                        (scope, cont) -> SupabaseAssignment.INSTANCE.getAssignmentsWithDetails(cont)
                );
                return parseAssignmentDetails(data);
            } catch (Exception e) {
                Log.e("WorkloadRepository", "Error getting assignment details", e);
                return new ArrayList<>();
            }
        });
    }

    private List<AssignmentDetail> parseAssignmentDetails(String data) throws Exception {
        JSONArray array = new JSONArray(data);
        List<AssignmentDetail> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            AssignmentDetail detail = new AssignmentDetail();

            // Parse Assignment
            Assignment a = new Assignment();
            a.setAssignmentID(obj.optString("AssignmentID"));
            a.setLecturerID(obj.optString("LecturerID"));
            a.setCourseCode(obj.optString("CourseCode"));
            a.setSectionID(obj.optString("SectionID"));
            a.setLoadPercentage(obj.optDouble("LoadPercentage", 100.0));
            a.setType(obj.has("Type") ? obj.optString("Type") : obj.optString("type"));
            detail.setAssignment(a);

            // Parse Course
            JSONObject cObj = null;
            if (obj.has("Course") && !obj.isNull("Course")) {
                cObj = obj.getJSONObject("Course");
            } else if (obj.has("course") && !obj.isNull("course")) {
                cObj = obj.getJSONObject("course");
            }

            if (cObj != null) {
                Course c = new Course();
                c.setCourseCode(cObj.has("CourseCode") ? cObj.optString("CourseCode") : cObj.optString("coursecode"));
                c.setCourseName(cObj.has("CourseName") ? cObj.optString("CourseName") : cObj.optString("coursename"));
                c.setMethod(cObj.has("Method") ? cObj.optString("Method") : cObj.optString("method"));
                c.setCreditValue(cObj.has("CreditValue") ? cObj.optDouble("CreditValue", 0.0) : cObj.optDouble("creditvalue", 0.0));
                // Handle rename SSP -> WeeklyHour
                c.setWeeklyHour(cObj.has("WeeklyHour") ? cObj.optDouble("WeeklyHour") : (cObj.has("weeklyhour") ? cObj.optDouble("weeklyhour") : cObj.optDouble("SSP", 0.0)));
                detail.setCourse(c);
            }

            // Parse Section
            JSONObject sObj = null;
            if (obj.has("Section") && !obj.isNull("Section")) {
                sObj = obj.getJSONObject("Section");
            } else if (obj.has("section") && !obj.isNull("section")) {
                sObj = obj.getJSONObject("section");
            }

            if (sObj != null) {
                Section s = new Section();
                s.setSectionID(sObj.has("SectionID") ? sObj.optString("SectionID") : sObj.optString("sectionid"));
                s.setSectionNumber(sObj.has("SectionNumber") ? sObj.optString("SectionNumber") : sObj.optString("sectionnumber"));
                s.setStudentAmount(sObj.has("StudentAmount") ? sObj.optString("StudentAmount", "0") : sObj.optString("studentamount", "0"));
                s.setSemSessionID(sObj.has("SemSessionID") ? sObj.optString("SemSessionID") : sObj.optString("semsessionid"));
                detail.setSection(s);

                // Parse SemesterSession
                JSONObject ssObj = null;
                if (sObj.has("Semester_Session") && !sObj.isNull("Semester_Session")) {
                    ssObj = sObj.getJSONObject("Semester_Session");
                } else if (sObj.has("semester_session") && !sObj.isNull("semester_session")) {
                    ssObj = sObj.getJSONObject("semester_session");
                }

                if (ssObj != null) {
                    SemesterSession ss = new SemesterSession();
                    ss.setSemSessionID(ssObj.has("SemSessionID") ? ssObj.optString("SemSessionID") : ssObj.optString("semsessionid"));
                    ss.setYear(ssObj.has("Year") ? ssObj.optInt("Year") : ssObj.optInt("year"));
                    ss.setSemester(ssObj.has("Semester") ? ssObj.optString("Semester") : ssObj.optString("semester"));
                    ss.setSession(ssObj.has("Session") ? ssObj.optString("Session") : ssObj.optString("session"));
                    detail.setSemesterSession(ss);
                }
            }

            // Parse Lecturer
            JSONObject lObj = null;
            if (obj.has("Lecturer") && !obj.isNull("Lecturer")) {
                lObj = obj.getJSONObject("Lecturer");
            } else if (obj.has("lecturer") && !obj.isNull("lecturer")) {
                lObj = obj.getJSONObject("lecturer");
            }

            if (lObj != null) {
                Lecturer l = new Lecturer();
                l.setLecturerID(lObj.has("LecturerID") ? lObj.optString("LecturerID") : lObj.optString("lecturerid"));
                l.setLecturerName(lObj.has("LecturerName") ? lObj.optString("LecturerName") : lObj.optString("lecturername"));
                l.setPosition(lObj.has("Position") ? lObj.optString("Position") : lObj.optString("position"));
                l.setNormalBTSA(lObj.has("NormalBTSA") ? lObj.optDouble("NormalBTSA", 0.0) : lObj.optDouble("normalbtsa", 0.0));
                l.setNormalCredit(lObj.has("NormalCredit") ? lObj.optDouble("NormalCredit", 0.0) : lObj.optDouble("normalcredit", 0.0));
                l.setEmploymentType(lObj.has("EmploymentType") ? lObj.optString("EmploymentType") : lObj.optString("employmenttype"));
                
                WorkloadCalculator.applyPositionAdjustments(l);
                detail.setLecturer(l);
            }

            list.add(detail);
        }
        return list;
    }
}
