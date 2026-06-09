package com.example.credify.utils;

public class FormulaRule {
    public final double a;
    public final double divisor;
    public final double threshold;
    public final double extraMultiplier;
    public final boolean useWeeklyHour;

    public FormulaRule(double a, double divisor, double threshold, double extraMultiplier, boolean useWeeklyHour) {
        this.a = a;
        this.divisor = divisor;
        this.threshold = threshold;
        this.extraMultiplier = extraMultiplier;
        this.useWeeklyHour = useWeeklyHour;
    }
}
