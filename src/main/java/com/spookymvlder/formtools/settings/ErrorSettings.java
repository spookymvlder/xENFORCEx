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
    private Map<Integer, Integer> logSeverity;
    private Integer errorThreshold;

    private ErrorSettings() {
        this.logCount = 0;
        this.errorThreshold = ErrorSeverity.ERROR.getSeverityLevel();
        this.errorLogs = new HashMap<>();
        this.logSeverity = new HashMap<>();
        this.logSeverity.put(0, ErrorSeverity.NONE.getSeverityLevel());
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
        //TODO make this safer.
        errorLogs.put(logCount, new ArrayList<>());
    }

    public List<ErrorEntry> getErrorLog(Integer logNumber) {
        if (logNumber < 1 || logNumber > logCount) {
            return new ArrayList<>();
        }
        return errorLogs.get(logNumber);
    }

    //TODO consider forcing use of currentLog for everything else will need to worry about logNumber < logCount
    public void addErrorToLog(Integer logNumber, ErrorEntry errorEntry) {
        this.updateLogSeverity(logNumber, errorEntry);
        this.errorLogs.computeIfAbsent(logNumber, k -> new ArrayList<>()).add(errorEntry);
    }

    public void addToCurrentLog(ErrorEntry errorEntry) {
        addErrorToLog(this.logCount, errorEntry);
    }

    public List<ErrorEntry> getCurrentLog() {
        return errorLogs.get(this.logCount);
    }

    public int getSeverityOfLog(Integer logNumber) {
        return this.logSeverity.get(logNumber);
    }

    public int getSeverityOfCurrentLog() {

        return getSeverityOfLog(this.logCount - 1);
    }

    private void updateLogSeverity(int logNumber, ErrorEntry errorEntry) {
        int severity = errorEntry.getSeverity().getSeverityLevel();
        if (this.logSeverity.get(logNumber) == null || severity > this.logSeverity.get(logNumber)) {
            this.logSeverity.put(logNumber, severity);
        }
    }

    public Integer getErrorThreshold() {
        return errorThreshold;
    }

    public void setErrorThreshold(Integer errorThreshold) {
        this.errorThreshold = errorThreshold;
    }

    public void setErrorThreshold(ErrorSeverity severity) {
        this.errorThreshold = severity.getSeverityLevel();
    }
}
