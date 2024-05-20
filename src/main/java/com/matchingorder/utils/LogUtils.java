package com.matchingorder.utils;

import quickfix.Message;

public class LogUtils {
    public static String formatFixMessageLog(Message message) {
        String data = message.toString();
        data = data.replace("\u0001","|");
        return data;
    }
}
