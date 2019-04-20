package com.shy.wechatsell.util;

/**
 * @author Haiyu
 * @date 2018/10/26 13:51
 */
public class MathUtil {

    private static final Double MONEY_RANGE = 0.01;

    public static Boolean equals(Double d1, Double d2) {
        Double diff = Math.abs(d1-d2);
        return diff < MONEY_RANGE;
    }
}
