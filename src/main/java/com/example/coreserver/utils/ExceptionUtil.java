package com.example.coreserver.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author lord
 * @date 2025/4/4
 * @description
 */
public class ExceptionUtil {

    public static String getTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        StringBuffer buffer = stringWriter.getBuffer();
        return buffer.toString();
    }

}