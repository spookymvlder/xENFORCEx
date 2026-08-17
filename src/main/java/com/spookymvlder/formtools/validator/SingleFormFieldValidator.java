package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.TypedPredicateFactory;
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
                validateOrderedValue(value, property, "numeric value");
            } else if (dataType == FieldDataType.BOOLEAN) {
                validateBoolean(value, property);
            } else if (dataType == FieldDataType.DATE) {
                validateOrderedValue(value, property, "ISO-8601 date (yyyy-MM-dd)");
            } else if (dataType == FieldDataType.TIME) {
                validateOrderedValue(value, property, "ISO-8601 time (HH:mm[:ss])");
            } else if (dataType == FieldDataType.DATETIME) {
                validateOrderedValue(value, property, "ISO-8601 datetime (yyyy-MM-ddTHH:mm[:ss])");
            } else if (dataType == FieldDataType.GYEAR) {
                validateOrderedValue(value, property, "ISO-8601 year (yyyy)");
            } else if (dataType == FieldDataType.GYEARMONTH) {
                validateOrderedValue(value, property, "ISO-8601 year-month (yyyy-MM)");
            } else if (dataType == FieldDataType.GMONTH) {
                validateOrderedValue(value, property, "month (1-12, #N index, or month name)");
            } else if (dataType == FieldDataType.GDAY) {
                validateOrderedValue(value, property, "day of month (---DD or 1-31)");
            } else if (dataType == FieldDataType.GMONTHDAY) {
                validateOrderedValue(value, property, "ISO-8601 month-day (--MM-DD)");
            } else if (dataType == FieldDataType.DURATION) {
                validateOrderedValue(value, property, "ISO-8601 duration (PnDTnHnMn.nS)");
            } else if (dataType == FieldDataType.CHARACTER) {
                validateCharacter(value, property);
            }
        }

        validateLength(value, property);
        validateEnumeration(value, property);
        validateRegex(value, property);
    }

    private static void validateOrderedValue(String value, FieldProperty property, String formatHint) {
        FieldDataType dataType = property.getValueRestriction().getDataType();
        if (!TypedPredicateFactory.isParseable(value, dataType)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                            "that cannot be parsed as a " + formatHint + ".",
                    ErrorSeverity.ERROR));
            return;
        }
        validateValueRange(value, property);
    }

    private static void validateValueRange(String value, FieldProperty property) {
        FieldValueRestriction restriction = property.getValueRestriction();
        FieldDataType dataType = restriction.getDataType();
        String rangeStart = restriction.getRangeStart();
        String rangeEnd = restriction.getRangeEnd();
        if (rangeStart == null && rangeEnd == null) {
            return;
        }

        if (rangeStart != null) {
            Integer cmp = TypedPredicateFactory.compareValues(value, rangeStart, dataType);
            if (cmp != null) {
                boolean inclusive = restriction.getRangeStartInclusive();
                if (inclusive ? cmp < 0 : cmp <= 0) {
                    errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                            "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                                    "that is less than the minimum range accepted for the field.",
                            ErrorSeverity.ERROR));
                }
            }
        }
        if (rangeEnd != null) {
            Integer cmp = TypedPredicateFactory.compareValues(value, rangeEnd, dataType);
            if (cmp != null) {
                boolean inclusive = restriction.getRangeEndInclusive();
                if (inclusive ? cmp > 0 : cmp >= 0) {
                    errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                            "FieldProperty " + property.getFieldId() + " has been assigned a value that is greater than " +
                                    "the maximum range accepted for the field.",
                            ErrorSeverity.ERROR));
                }
            }
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
        if (property.getValueRestriction().getDataType() != FieldDataType.ENUMERATION) {
            return;
        }
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
