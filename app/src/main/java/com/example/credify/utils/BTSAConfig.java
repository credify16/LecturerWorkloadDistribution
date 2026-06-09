package com.example.credify.utils;

/**
 * Institutional Constants for BTSA Workload Engine.
 * Based on verified spreadsheet components A/B/C/D/E/F.
 */
public class BTSAConfig {

    // Number of academic weeks in a semester (Component B/C multiplier)
    public static final double SEMESTER_WEEKS = 14.0;

    // Base student capacity for standard sections (Component D Threshold)
    public static final int BASE_STUDENT_LIMIT = 40;

    // Progressive scaling factor for excess students (Component E Multiplier)
    public static final double EXCESS_STUDENT_WEIGHT = 0.5;

    // Default Institutional Multiplier (Component A - Nilai Beban)
    public static final double DEFAULT_NILAI_BEBAN = 3.5;

    /**
     * Component A: Nilai Beban by Method
     */
    public static double getNilaiBeban(String method) {
        switch (method) {
            case "B": return 3.5; // Biasa (Lecture + Lab)
            case "K": return 3.5; // Kuliah (Lecture Only)
            case "M": return 8.0; // Industrial Training
            case "P": return 1.0; // Project
            case "R": return 2.0; // Report
            default: return 3.5;
        }
    }

    /**
     * Component B: Unit Multiplier
     */
    public static double getUnitMultiplier(String method) {
        // Institutional standard is 1.0 for B/K. 
        // Specialized methods may use different coefficients.
        return 1.0;
    }
}
