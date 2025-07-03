package com.spookymvlder.formtools.utilities;

public class ValidationError {
    private final int fieldId;
    private final String fieldName;
    private final String message;
    private final ErrorSeverity severity;

    public ValidationError(int fieldId, String fieldName, String message, ErrorSeverity severity) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.message = message;
        this.severity = severity;
    }

    public String getFormattedMessage() {
        return String.format("Field ID %d (%s) - %s: %s", fieldId, fieldName, severity, message);
    }

    public int getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getMessage() {
        return message;
    }

    public ErrorSeverity getSeverity() {
        return severity;
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }
}
