package com.example.credify.utils;

import com.example.credify.data.model.*;
import java.util.ArrayList;
import java.util.List;

public class WorkloadCalculator {

    /**
     * Calculates Credits for a single assignment.
     * Hardened: Handles LI (Method M) as student-based credits (0.4 per student).
     */
    public static double calculateCourseCredits(Course course, double loadPercentage) {
        if (course == null) return 0.0;
        String method = (course.getMethod() != null) ? course.getMethod().toUpperCase() : "";

        if ("M".equals(method)) {
            // LI uses loadPercentage as Student Count directly (0.4 per student)
            return loadPercentage * 0.4;
        } else if ("R".equals(method)) {
            // Report is usually 0 credits in the combined package
            return 0.0;
        }

        return (course.getCreditValue() != null ? course.getCreditValue() : 0.0) * (loadPercentage / 100.0);
    }

    /**
     * Calculates BTSA for a single course assignment using REBUILT institutional pipeline.
     */
    public static double calculateCourseBTSA(Course course, Section section, double loadPercentage) {
        return BTSACalculator.calculate(course, section, loadPercentage);
    }

    /**
     * Evaluates StudentAmount which might be a formula (e.g., "=6+2").
     */
    public static int evaluateStudentAmount(Section section) {
        if (section == null || section.getStudentAmount() == null) return 0;
        
        String raw = section.getStudentAmount().trim();
        if (raw.isEmpty()) return 0;

        if (raw.startsWith("=")) {
            raw = raw.substring(1);
        }

        // Basic formula evaluator for addition (e.g. 6+2)
        if (raw.contains("+")) {
            String[] parts = raw.split("\\+");
            int total = 0;
            for (String part : parts) {
                try {
                    total += Integer.parseInt(part.trim());
                } catch (NumberFormatException ignored) {}
            }
            return total;
        }

        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Adjusts Normal BTSA and Normal Credit based on Lecturer's Category.
     * Refactored: Only applies defaults if values are null or 0.0 to allow manual edits.
     */
    public static void applyPositionAdjustments(Lecturer lecturer) {
        if (lecturer == null) return;

        // Initialize metadata from position mapping (centralized policy)
        InstitutionalPolicy.applyWorkloadMetadata(lecturer);

        Lecturer.WorkloadCategory category = lecturer.getWorkloadCategory();

        if (category != Lecturer.WorkloadCategory.STANDARD) {
            // Only apply institutional defaults if the current values are not yet set
            if ((lecturer.getNormalBTSA() == null || lecturer.getNormalBTSA() == 0.0) &&
                (lecturer.getNormalCredit() == null || lecturer.getNormalCredit() == 0.0)) {
                
                double targetBtsa;
                double targetCredit;
                
                if (category == Lecturer.WorkloadCategory.KJ) {
                    targetBtsa = 300.0;
                    targetCredit = 6.0;
                } else {
                    // KP Category (Coordinator / Penyelaras)
                    targetBtsa = 600.0;
                    targetCredit = 12.0;
                }
                
                lecturer.setNormalBTSA(targetBtsa);
                lecturer.setNormalCredit(targetCredit);
            }
        }
    }

    /**
     * Retrieves the institutional BTSA target for a lecturer.
     * Refactored: Semester-level normalization removed as per new design.
     */
    public static double getTargetBTSA(Lecturer l) {
        if (l == null) return 0.0;
        applyPositionAdjustments(l); // Ensure metadata is applied
        
        double value = l.getNormalBTSA() != null ? l.getNormalBTSA() : 0.0;

        // NEW: If value is still 0, provide sane defaults
        if (value == 0.0) {
            Lecturer.WorkloadCategory category = l.getWorkloadCategory();
            if (category == Lecturer.WorkloadCategory.STANDARD) {
                value = 900.0; // Annual default for standard
            } else if (category == Lecturer.WorkloadCategory.KJ) {
                value = 300.0;  // Semester default
            } else if (category == Lecturer.WorkloadCategory.KP) {
                value = 600.0; // Semester default
            }
        }
        
        android.util.Log.d("BTSA_DEBUG", String.format(
            "Lecturer: %s | Base Value: %.1f", 
            l.getLecturerName(), value));
            
        return value;
    }

    /**
     * Retrieves the institutional Credit target for a lecturer.
     * Refactored: Semester-level normalization removed as per new design.
     */
    public static double getTargetCredit(Lecturer l) {
        if (l == null) return 0.0;
        applyPositionAdjustments(l);
        
        double value = l.getNormalCredit() != null ? l.getNormalCredit() : 0.0;

        // NEW: If value is still 0, provide sane defaults
        if (value == 0.0) {
            Lecturer.WorkloadCategory category = l.getWorkloadCategory();
            if (category == Lecturer.WorkloadCategory.STANDARD) {
                value = 18.0; // Annual default for standard
            } else if (category == Lecturer.WorkloadCategory.KJ) {
                value = 6.0;  // Semester default
            } else if (category == Lecturer.WorkloadCategory.KP) {
                value = 12.0; // Semester default
            }
        }

        android.util.Log.d("CREDIT_DEBUG", String.format(
            "Lecturer: %s | Base Value: %.1f", 
            l.getLecturerName(), value));

        return value;
    }

    public static double calculateSemesterCredits(List<AssignmentDetail> details) {
        double total = 0;
        for (AssignmentDetail detail : details) {
            if (detail.getCourse() != null && detail.getAssignment() != null) {
                // Skip part-time assignments
                if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;
                
                total += calculateCourseCredits(detail.getCourse(), detail.getAssignment().getLoadPercentage());
            }
        }
        return total;
    }

    public static double calculateSemesterBTSA(List<AssignmentDetail> details) {
        double total = 0;
        for (AssignmentDetail detail : details) {
            if (detail.getCourse() != null && detail.getSection() != null && detail.getAssignment() != null) {
                // Skip part-time assignments
                if ("Part-time".equalsIgnoreCase(detail.getAssignment().getType())) continue;

                total += calculateCourseBTSA(detail.getCourse(), detail.getSection(), detail.getAssignment().getLoadPercentage());
            }
        }
        return total;
    }

    /**
     * Calculates warnings for a lecturer's workload.
     * Refactored to only evaluate warnings in ANNUAL scope if provided.
     */
    public static List<String> calculateWarnings(double totalBtsa, double normalBtsa, double totalCredits, double normalCredits, String employmentType, List<AssignmentDetail> details, boolean isAnnual) {
        List<String> warnings = new ArrayList<>();
        boolean isPartTime = "Part-time".equalsIgnoreCase(employmentType);

        if (isAnnual && !isPartTime) {
            double btsaDiff = totalBtsa - normalBtsa;
            double creditDiff = totalCredits - normalCredits;

            if (btsaDiff > 0.05 || creditDiff > 0.05) {
                warnings.add("Overload Warning!");
            } else if (btsaDiff < -0.05 || creditDiff < -0.05) {
                warnings.add("Underload Warning!");
            }
        }

        if (totalCredits == 0) {
            warnings.add("No teaching this semester!");
        }

        for (AssignmentDetail detail : details) {
            if (detail.getSection() != null) {
                int students = evaluateStudentAmount(detail.getSection());
                if (students > 60) {
                    warnings.add("High Student Warning (>60) in Section " + detail.getSection().getSectionNumber());
                }
            }
        }

        return warnings;
    }
}
