package com.example.credify.utils;

import java.util.HashMap;
import java.util.Map;

public class FormulaConfig {
    private static final Map<String, FormulaRule> rules = new HashMap<>();

    static {
        // B: Lecture + Lab/Practical (Biasa)
        rules.put("B", new FormulaRule(3.5, 14.0, 40.0, 0.5, true));
        // K: Lecture Only (Kuliah/Khas)
        rules.put("K", new FormulaRule(3.5, 14.0, 40.0, 0.5, true));
        // P: Project
        rules.put("P", new FormulaRule(1.0, 1.0, 999.0, 0.0, false));
        // M: Industrial Training
        rules.put("M", new FormulaRule(8.0, 1.0, 999.0, 0.0, false));
        // R: Report
        rules.put("R", new FormulaRule(2.0, 1.0, 999.0, 0.0, false));
    }

    public static FormulaRule getRule(String method) {
        if (method == null) return rules.get("B");
        FormulaRule rule = rules.get(method.toUpperCase());
        return rule != null ? rule : rules.get("B"); // Fallback to B
    }
}
