package com.example.credify.utils;

/**
 * Data model for BTSA calculation audit.
 * Captures all institutional stages A-F.
 */
public class BtsaBreakdown {
    public String courseCode;
    public String method;
    public int students;
    public double weeklyHours;
    public double nilaiBeban; // Stage A
    public double unitMultiplier; // Stage B
    public double baseLoad; // Stage C
    public int excessStudents; // Stage D
    public double excessWeight; // Stage E
    public double excessLoad; // Stage F
    public double loadPercentage;
    public double finalBtsa;

    public BtsaBreakdown() {}
}
