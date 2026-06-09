package com.example.credify.utils;

import com.example.credify.data.model.Lecturer;
import java.util.HashMap;
import java.util.Map;

/**
 * FINAL HARDENING: Centralized Institutional Mapping.
 * This class translates free-text positions into explicit workload categories and scopes.
 */
public class InstitutionalPolicy {

    private static final Map<String, Lecturer.WorkloadCategory> roleMap = new HashMap<>();

    static {
        // Explicit Role Mappings
        roleMap.put("KETUA JABATAN", Lecturer.WorkloadCategory.KJ);
        roleMap.put("PENGURUS BESAR", Lecturer.WorkloadCategory.KJ);
        roleMap.put("DIRECTOR", Lecturer.WorkloadCategory.KJ);
        roleMap.put("HEAD", Lecturer.WorkloadCategory.KJ);
        roleMap.put("CEO", Lecturer.WorkloadCategory.KJ);
        
        roleMap.put("KETUA PROGRAM", Lecturer.WorkloadCategory.KP);
        roleMap.put("PENYELARAS", Lecturer.WorkloadCategory.KP);
        roleMap.put("COORDINATOR", Lecturer.WorkloadCategory.KP);
    }

    /**
     * Maps a lecturer's raw position to a hardened WorkloadCategory.
     */
    public static void applyWorkloadMetadata(Lecturer l) {
        if (l == null) return;
        
        String position = (l.getPosition() != null) ? l.getPosition().toUpperCase() : "";
        
        // 1. Determine Category (Role)
        Lecturer.WorkloadCategory category = Lecturer.WorkloadCategory.STANDARD;
        for (Map.Entry<String, Lecturer.WorkloadCategory> entry : roleMap.entrySet()) {
            if (position.contains(entry.getKey())) {
                category = entry.getValue();
                break;
            }
        }
        l.setWorkloadCategory(category);

        // 2. Determine Scope (Unit)
        // Hardened Rule: STANDARD lecturers use ANNUAL benchmarks, Leadership roles use SEMESTER overrides.
        if (category == Lecturer.WorkloadCategory.STANDARD) {
            l.setTargetScope(Lecturer.WorkloadScope.ANNUAL);
        } else {
            l.setTargetScope(Lecturer.WorkloadScope.SEMESTER);
        }
    }
}
