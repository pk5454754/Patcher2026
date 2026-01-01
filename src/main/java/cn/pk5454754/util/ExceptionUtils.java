package cn.pk5454754.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
    public static String getStructuredErrorString(Exception e) {
        final StringWriter sw = new StringWriter(1024);
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static String getStructuredErrorString(Throwable t) {
        final StringWriter sw = new StringWriter(1024);
        final PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
