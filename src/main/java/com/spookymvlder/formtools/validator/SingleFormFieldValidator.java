package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldValueRestriction;
import com.spookymvlder.formtools.datatypes.fields.CompositeField;
import com.spookymvlder.formtools.datatypes.fields.ContainerField;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class SingleFormFieldValidator {
    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateWithErrors(FormField field) {
        if (field instanceof SimpleField simpleField) {
            validateSimpleField(simpleField);
        } else if (field instanceof CompositeField) {
            //TODO composite/constituent value validation
        } else if (field instanceof ContainerField) {
            // No value to validate.
        }
    }

    public static void validateSimpleField(SimpleField field) {
        FieldProperty property = field.getProperty();
        FieldData data = field.getValue();
        if (data == null) {
            return;
        }
        String value = data.getValue();
        // Empty values are handled by required-field checks; skip value rules.
        if (StringUtils.isNullOrEmpty(value)) {
            return;
        }

        FieldValueRestriction restriction = property.getValueRestriction();
        FieldDataType dataType = restriction.getDataType();

        if (dataType != null) {
            if (dataType.isNumeric()) {
                validateNumeric(value, property);
            } else if (dataType == FieldDataType.BOOLEAN) {
                validateBoolean(value, property);
            } else if (dataType == FieldDataType.DATE) {
                validateDate(value, property);
            } else if (dataType == FieldDataType.TIME) {
                validateTime(value, property);
            } else if (dataType == FieldDataType.DATETIME) {
                validateDateTime(value, property);
            } else if (dataType == FieldDataType.CHARACTER) {
                validateCharacter(value, property);
            }
        }

        validateLength(value, property);
        validateEnumeration(value, property);
        validateRegex(value, property);
    }

    //TODO support ranges on ordered non-numeric types (e.g. GMONTH March-August, DATE, TIME).
    private static void validateNumeric(String value, FieldProperty property) {
        try {
            double parsed = Double.parseDouble(value);
            validateNumericRange(parsed, property);
        } catch (NumberFormatException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                            "that cannot be converted to a numeric data type, despite being a " +
                            property.getValueRestriction().getDataType() + " data type.",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateNumericRange(double value, FieldProperty property) {
        FieldValueRestriction restriction = property.getValueRestriction();
        double rangeStart = restriction.getRangeStart() != null
                ? Double.parseDouble(restriction.getRangeStart()) : Double.NEGATIVE_INFINITY;
        double rangeEnd = restriction.getRangeEnd() != null
                ? Double.parseDouble(restriction.getRangeEnd()) : Double.POSITIVE_INFINITY;

        if ((restriction.getRangeStartInclusive() && value < rangeStart) ||
                (!restriction.getRangeStartInclusive() && value <= rangeStart)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                            "that is less than the minimum range accepted for the field.",
                    ErrorSeverity.ERROR));
        }

        if ((restriction.getRangeEndInclusive() && value > rangeEnd) ||
                (!restriction.getRangeEndInclusive() && value >= rangeEnd)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that is greater than " +
                            "the maximum range accepted for the field.",
                    ErrorSeverity.ERROR));
        }
    }

    //TODO confirm accepted boolean input formats once decided in import spec.
    private static void validateBoolean(String value, FieldProperty property) {
        String normalized = value.trim().toLowerCase();
        if (!normalized.equals("true") && !normalized.equals("false")
                && !normalized.equals("t") && !normalized.equals("f")
                && !normalized.equals("1") && !normalized.equals("0")) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that " +
                            "cannot be parsed as a boolean.",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateDate(String value, FieldProperty property) {
        try {
            LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that " +
                            "cannot be parsed as a date (expected ISO-8601 yyyy-MM-dd).",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateTime(String value, FieldProperty property) {
        try {
            LocalTime.parse(value);
        } catch (DateTimeParseException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that " +
                            "cannot be parsed as a time (expected ISO-8601 HH:mm[:ss]).",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateDateTime(String value, FieldProperty property) {
        try {
            LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that " +
                            "cannot be parsed as a datetime (expected ISO-8601 yyyy-MM-ddTHH:mm[:ss]).",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateCharacter(String value, FieldProperty property) {
        if (value.length() != 1) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " is typed as CHARACTER but has been " +
                            "assigned a value of length " + value.length() + ".",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateLength(String value, FieldProperty property) {
        FieldValueRestriction restriction = property.getValueRestriction();
        Integer minLength = restriction.getMinLength();
        Integer maxLength = restriction.getMaxLength();

        if (minLength != null && value.length() < minLength) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that is " +
                            "shorter than the minimum length of " + minLength + ".",
                    ErrorSeverity.ERROR));
        }
        if (maxLength != null && value.length() > maxLength) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that is " +
                            "longer than the maximum length of " + maxLength + ".",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateEnumeration(String value, FieldProperty property) {
        List<String> enumerations = property.getValueRestriction().getEnumerations();
        if (enumerations == null || enumerations.isEmpty()) {
            return;
        }
        if (!enumerations.contains(value)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that does " +
                            "not match any of the enumerated allowed values.",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateRegex(String value, FieldProperty property) {
        String pattern = property.getValueRestriction().getRegexPattern();
        if (StringUtils.isNullOrEmpty(pattern)) {
            return;
        }
        try {
            if (!value.matches(pattern)) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "FieldProperty " + property.getFieldId() + " has been assigned a value that " +
                                "does not match the required pattern.",
                        ErrorSeverity.ERROR));
            }
        } catch (java.util.regex.PatternSyntaxException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has an invalid regex pattern: " +
                            e.getMessage(),
                    ErrorSeverity.ERROR));
        }
    }
}
