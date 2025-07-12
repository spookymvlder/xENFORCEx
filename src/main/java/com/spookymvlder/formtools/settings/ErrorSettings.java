package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.ErrorEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ErrorSettings {
    private static ErrorSettings instance;
    private Integer logCount;
    private Map<Integer, List<ErrorEntry>> errorLogs;

    private ErrorSettings() {
        this.logCount = 0;
        this.errorLogs = new HashMap<>();
    }

    public static ErrorSettings getInstance() {
        if (instance == null) {
            instance = new ErrorSettings();
        }
        return instance;
    }

    public Integer getLogCount() {
        return logCount;
    }

    public void startNewLog() {
        logCount++;
    }

    public List<ErrorEntry> getErrorLog(Integer logNumber) {
        return errorLogs.get(logNumber);
    }

    public void addErrorLog(Integer logNumber, ErrorEntry errorEntry) {
        this.errorLogs.computeIfAbsent(logNumber, k -> new ArrayList<>()).add(errorEntry);
    }

    public void addToCurrentLog(ErrorEntry errorEntry) {
        addErrorLog(this.logCount, errorEntry);
    }
}
