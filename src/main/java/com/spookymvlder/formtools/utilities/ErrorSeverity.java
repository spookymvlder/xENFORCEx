package com.spookymvlder.formtools.utilities;

public enum ErrorSeverity {
    NONE(0),                   //For empty error log
    NOTIFICATION(1),           //FYI this could be better
    WARNING(2),                //This may cause unintended behavior
    ERROR(3),                  //This will cause issues, but can continue current phase of processing
    FATAL(4);                 //Unable to continue until resolved


    private final int severityLevel;

    ErrorSeverity(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    }