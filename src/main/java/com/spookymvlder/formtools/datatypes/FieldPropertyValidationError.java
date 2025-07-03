package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.utilities.ErrorSeverity;

public class FieldPropertyValidationError {
    private final String fieldId;
    private final String fieldName;
    private final String message;
    private final ErrorSeverity severity;

    public FieldPropertyValidationError(String fieldId, String fieldName, String message, ErrorSeverity severity) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.message = message;
        this.severity = severity;
    }

    public String getFormattedMessage() {
        return String.format("Field ID %s (%s) - %s: %s", fieldId, fieldName, severity, message);
    }

    public String getFieldId() {
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
