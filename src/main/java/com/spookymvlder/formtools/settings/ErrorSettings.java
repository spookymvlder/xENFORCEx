package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ErrorSettings {
    private static ErrorSettings instance;
    private Integer logCount;
    private Map<Integer, List<ErrorEntry>> errorLogs;
    private Map<Integer, ErrorSeverity> logSeverity;
    private ErrorSeverity errorThreshold;
    private Integer currentLogId;

    private ErrorSettings() {
        this.logCount = 0;
        this.currentLogId = null;
        // FATAL, not ERROR, so a stage boundary only halts when continuing would be meaningless
        // (unreadable input, no single root/anchor, duplicate fieldId, unresolvable parentId, cycle).
        // ERROR is per-field and independent by contract, so later stages still run and report,
        // letting one pass surface every fixable problem instead of just the first stage's worth.
        this.errorThreshold = ErrorSeverity.FATAL;
        this.errorLogs = new HashMap<>();
        this.logSeverity = new HashMap<>();
    }

    public static ErrorSettings getInstance() {
        if (instance == null) {
            instance = new ErrorSettings();
        }
        return instance;
    }

    public Integer getLogCount() {
        return this.logCount;
    }

    public ErrorSeverity getErrorThreshold() {
        return this.errorThreshold;
    }

    public void updateErrorThreshold(ErrorSeverity severity) {
        this.errorThreshold = severity;
        for (Integer logNumber : this.errorLogs.keySet()) {
            // Reset severity of log so recompute happens.
            this.logSeverity.put(logNumber, ErrorSeverity.NONE);
            for (ErrorEntry errorEntry : this.errorLogs.get(logNumber)) {
                updateLogSeverity(logNumber, errorEntry);
            }
        }
    }

    public void startNewLog() {
        this.logCount++;
        int curLog = this.logCount - 1;
        this.errorLogs.put(curLog, new ArrayList<>());
        this.logSeverity.put(curLog, ErrorSeverity.NONE);
        this.currentLogId = curLog;
        addToCurrentLog(new ErrorEntry(
                ErrorSource.ERROR_LOG_SETUP,
                "New log started at " + LocalDateTime.now() + ".",
                ErrorSeverity.NONE));
    }

    public boolean logExists(Integer logId) {
        return errorLogs.containsKey(logId);
    }

    public List<ErrorEntry> getErrorLog(Integer logNumber) throws IndexOutOfBoundsException {
        if (logExists(logNumber)) {
            return errorLogs.get(logNumber);
        }
        throw new IndexOutOfBoundsException("Log number " + logNumber + " not found. Unable to return log.");
    }

    public List<ErrorEntry> getCurrentLog() {
        return errorLogs.get(this.currentLogId);
    }


    private void addToLog(Integer logNumber, ErrorEntry errorEntry) throws IndexOutOfBoundsException {
        if (!logExists(logNumber)) {
            throw new IndexOutOfBoundsException("Log number " + logNumber + " not found. Unable to add error to log.");
        }
        this.updateLogSeverity(logNumber, errorEntry);
        // Compute if absent present to make it slightly easier to update, no intention to create new log.
        this.errorLogs.computeIfAbsent(logNumber, k -> new ArrayList<>()).add(errorEntry);
    }

    public void addToCurrentLog(ErrorEntry errorEntry) {
        addToLog(this.currentLogId, errorEntry);
    }


    public ErrorSeverity getSeverityOfLog(Integer logNumber) throws IndexOutOfBoundsException {
        if (logExists(logNumber)) {
            return this.logSeverity.get(logNumber);
        }
        throw new IndexOutOfBoundsException("Log number " + logNumber + " not found. Unable to provide severity.");
    }

    public ErrorSeverity getSeverityOfCurrentLog() {

        return getSeverityOfLog(this.currentLogId);
    }

    private void updateLogSeverity(int logNumber, ErrorEntry errorEntry) throws IndexOutOfBoundsException {
        if  (!logExists(logNumber)) {
            throw new IndexOutOfBoundsException("Log number " + logNumber + " not found. Unable to set severity.");
        }
        if (errorEntry.getSeverity().isGreaterThan(getSeverityOfLog(logNumber))) {
            this.logSeverity.put(logNumber, errorEntry.getSeverity());
        }
    }


    public boolean checkLogOverThreshold(int logNumber) throws IndexOutOfBoundsException {
        if (!logExists(logNumber)) {
            throw new IndexOutOfBoundsException("Log number " + logNumber + " not found. Unable to check against threshold.");
        }
        return getSeverityOfLog(logNumber).isAtLeast(getErrorThreshold());
    }

    /**
     * Checks if the severity of the current error log is at least the system specified threshold.
     * @return - true if severity of log >= threshold severity, as boolean.
     */
    public boolean checkCurrentLogOverThreshold(){
        return checkLogOverThreshold(this.currentLogId);
    }



    public void printErrorLogs() {
        for  (Integer logNumber : this.errorLogs.keySet()) {
            printErrorLog(logNumber);
        }
    }

    public void printErrorLog(int logNumber) {
        if (!logExists(logNumber)) {
            return;
        }
        System.out.println("Error log " + logNumber + " size: " + this.errorLogs.get(logNumber).size());
        for (ErrorEntry errorEntry : this.errorLogs.get(logNumber)) {
            System.out.println(errorEntry);
        }
    }

    public void printCurrentLog() {
        printErrorLog(this.currentLogId);
    }
}
