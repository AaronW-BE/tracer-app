package vip.fastgo.tracer.utils;

import android.util.Log;

public class LoggerUtil {
    public static final String TAG = "TraceR";
    public static void log(String msg) {
        Log.e(TAG, msg);
    }

    public static void log(int i) {
        log(String.valueOf(i));
    }

    public static void log(String ...msgs) {
        StringBuilder stringBuilder = new StringBuilder();
        for ( String msg : msgs ) {
            stringBuilder.append(msg).append(" ");
        }
    }
}
