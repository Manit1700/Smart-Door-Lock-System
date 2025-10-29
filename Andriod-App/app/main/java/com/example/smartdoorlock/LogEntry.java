package com.example.smartdoorlock;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LogEntry {
    private String action;
    private String timestamp;

    public LogEntry(String action) {
        this.action = action;
        this.timestamp = new SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault())
                .format(new Date());
    }

    @Override
    public String toString() {
        return timestamp + ": " + action;
    }
}