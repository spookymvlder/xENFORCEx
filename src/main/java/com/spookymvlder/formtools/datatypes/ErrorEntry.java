package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.Objects;

public class ErrorEntry {
    protected final ErrorSource source;
    protected final String message;
    protected final ErrorSeverity severity;


    public ErrorEntry(ErrorSource source, String message, ErrorSeverity severity) {
        this.source = source;
        this.message = message;
        this.severity = severity;
    }

    public ErrorSource getSource() {
        return source;
    }

    public String getMessage() {
        return message;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    public String getFormattedMessage() {
        return String.format("%s - %s: %s", source, severity, message);
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ErrorEntry that = (ErrorEntry) o;
        return Objects.equals(getMessage(), that.getMessage()) && getSeverity() == that.getSeverity();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMessage(), getSeverity());
    }
}
