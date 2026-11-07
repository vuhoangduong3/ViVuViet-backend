package com.example.Vi_vu_Viet.Util;

import java.util.Map;
import java.util.ResourceBundle;

public class ApplicationCode {

    public static final int SUCCESS = 0;
    public static final int UNKNOWN_ERROR = 1;
    public static final int USER_DEACTIVATE = 2;
    public static final int USER_EXITS = 3;
    public static final int INPUT_INVALID = 4;
    public static final int INVALID_PASSWORD = 5;
    public static final int INVALID_TOKEN = 6;

    private static final Map<Integer, String> MESSAGE_KEYS = Map.of(
            SUCCESS, "SUCCESS",
            UNKNOWN_ERROR, "UNKNOWN_ERROR",
            USER_DEACTIVATE, "USER_DEACTIVATE",
            USER_EXITS, "USER_EXITS",
            INPUT_INVALID, "INPUT_INVALID",
            INVALID_PASSWORD, "INVALID_PASSWORD",
            INVALID_TOKEN, "INVALID_TOKEN"
    );

    public static String getMessage(int code) {
        String key = MESSAGE_KEYS.get(code);

        if (key == null) {
            return "";
        }

        return getBundle().getString(key);
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("language_vi");
    }
}