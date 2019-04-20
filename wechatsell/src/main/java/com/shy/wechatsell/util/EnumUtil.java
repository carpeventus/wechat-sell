package com.shy.wechatsell.util;

import com.shy.wechatsell.enums.CodeEnum;

/**
 * @author Haiyu
 * @date 2018/10/27 9:42
 */
public class EnumUtil {
    public static <T extends CodeEnum> T getByCode(Integer code, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            if (each.getCode().equals(code)) {
                return each;
            }
        }
        return null;
    }
}
