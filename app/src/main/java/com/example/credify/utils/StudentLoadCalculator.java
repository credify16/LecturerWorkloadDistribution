package com.example.credify.utils;

/**
 * Handles Component D, E, and F: Excess Student Workload Scaling.
 */
public class StudentLoadCalculator {

    /**
     * Calculates Kiraan Beban Jam Pertemuan 2 (Component F).
     */
    public static double calculateExcess(int studentCount, double baseLoad, String method) {
        // Institutional rule: scaling typically applies only to lecture-style methods (B/K)
        if (!"B".equalsIgnoreCase(method) && !"K".equalsIgnoreCase(method)) {
            return 0.0;
        }

        if (studentCount <= BTSAConfig.BASE_STUDENT_LIMIT) {
            return 0.0;
        }

        // Component D: Lebihan Pelajar
        int excessStudents = studentCount - BTSAConfig.BASE_STUDENT_LIMIT;

        // Component F: Progressive scaling
        // Formula: (Excess / Threshold) * Weight * BaseLoad
        return ((double) excessStudents / BTSAConfig.BASE_STUDENT_LIMIT) * BTSAConfig.EXCESS_STUDENT_WEIGHT * baseLoad;
    }
}
