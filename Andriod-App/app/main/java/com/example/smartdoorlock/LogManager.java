package com.example.smartdoorlock;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LogManager {
    private static final String PREFS_NAME = "ActivityLogsPrefs";
    private static final String LOGS_KEY = "activityLogs";

    public static void saveLogs(Context context, List<LogEntry> logs) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String jsonLogs = gson.toJson(logs);
        editor.putString(LOGS_KEY, jsonLogs);
        editor.apply();
    }

    public static List<LogEntry> loadLogs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String jsonLogs = prefs.getString(LOGS_KEY, null);

        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<LogEntry>>(){}.getType();

        List<LogEntry> logs = gson.fromJson(jsonLogs, type);
        return logs != null ? logs : new ArrayList<>();
    }

    public static void clearLogs(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(LOGS_KEY).apply();
    }
}