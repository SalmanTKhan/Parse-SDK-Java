package android.util;


public class Log {

    private static String getStackTraceString(Exception e) {
        e.printStackTrace();
        return null;
    }

    public static void w(String logTag, String message) {
        System.out.println(logTag + ": " + message);
    }

    public static void e(String logTag, String message, Exception error) {
        w(logTag, message);
        getStackTraceString(error);
    }
}
