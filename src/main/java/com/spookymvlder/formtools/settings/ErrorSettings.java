package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.utilities.ErrorSeverity;

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

    public List<ErrorEntry> getCurrentLog() {
        return errorLogs.get(this.logCount);
    }

    public int getSeverityOfLog(Integer logNumber) {
        int level = 0;
        for (ErrorEntry error : getErrorLog(logNumber)) {
            if (error.getSeverity().getSeverityLevel() > level) {
                level = error.getSeverity().getSeverityLevel();
            }
        }
        return level;
    }

    public int getSeverityOfCurrentLog() {
        return getSeverityOfLog(this.logCount);
    }
}
