package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.Objects;

public class FieldPropertyValidationError extends ErrorEntry {
    private final String fieldId;
    private final String fieldName;

    public FieldPropertyValidationError(ErrorSource source, String message, ErrorSeverity severity, String fieldId, String fieldName) {
        super(source, message, severity);
        this.fieldId = fieldId;
        this.fieldName = fieldName;
    }


    @Override
    public String getFormattedMessage() {
        return String.format("Field ID %s (%s) - %s: %s", fieldId, fieldName, severity, message);
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FieldPropertyValidationError that = (FieldPropertyValidationError) o;
        return Objects.equals(getFieldId(), that.getFieldId()) && Objects.equals(getFieldName(), that.getFieldName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFieldId(), getFieldName());
    }
}
