package com.shy.wechatsell.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Haiyu
 * @date 2018/10/25 22:38
 */
public class JsonUtil {
    public static String toJson(Object object) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        return gson.toJson(object);
    }
}
