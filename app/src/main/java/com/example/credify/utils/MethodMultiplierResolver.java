package com.example.credify.utils;

/**
 * Resolves institutional weighting factors based on Teaching Method.
 */
public class MethodMultiplierResolver {

    public static double resolveA(String method) {
        return BTSAConfig.getNilaiBeban(method);
    }

    public static double resolveB(String method) {
        return BTSAConfig.getUnitMultiplier(method);
    }
}
