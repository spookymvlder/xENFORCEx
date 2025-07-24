package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.time.LocalDate;

//TODO add validation that name is unique
public class SingleFieldPropertyValidator {
    static final ErrorSettings errors = ErrorSettings.getInstance();
    //TODO store to error settings and return highest error level for field.

    public static void validateWithErrors(FieldProperties field)  {
        validateAttributes(field);
        validateRanges(field);
        validateRepeatOccurrences(field);
        validateActiveRange(field);
        validateGroupingType(field);

    }

    public static void validateAttributes(FieldProperties field) {
        //Unless default to no or assume if attribute name is populated it is an attribute?
        if (field.getAttribute() == null && field.getAttributeName() != null) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"isAttribute is required when attributeName is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
        if (field.getAttributeName() == null && field.getAttribute() != null) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"attributeName is required when isAttribute is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }

    }


    public static void validateRanges(FieldProperties field) {
        boolean ranges = (field.getRangeStart() != null) || (field.getRangeEnd() != null);
        if (ranges && field.getRangeInclusive() == null) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"rangeInclusive is required if rangeStart or rangeEnd are set.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }

        if (field.getRangeStart() != null && field.getRangeEnd() != null) {
            if (field.getRangeStart() > field.getRangeEnd()) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"rangeStart must be less than or equal to rangeEnd.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
            if (field.getRangeStart().equals(field.getRangeEnd()) && Boolean.FALSE.equals(field.getRangeInclusive())) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"No valid values for ranges.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
        }
    }

    public static void validateRepeatOccurrences(FieldProperties field) {

        //Quit early if field is not repeated.
        if (field.getFieldRepeated().equals('F')) {
            return;
        }
        if (field.getMinOccurs() != null && field.getMaxOccurs() != null) {
            if (field.getMinOccurs() > field.getMaxOccurs()) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs must be less than or equal to maxOccurs.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
        }
        if (field.getMaxOccurs() != null && field.getMaxOccurs() < 2) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"maxOccurs, if populated, must be greater than 1.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
        if (field.getMinOccurs() != null && field.getMinOccurs() < 0) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs, if populated, must be at least 0.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

    public static void validateActiveRange(FieldProperties field) {

        if (field.getActive() && field.getExpireDate().isAfter(LocalDate.now().minusDays(1))) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field is listed as active, but has an expiration date set in the past.", ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
        }
        if (field.getActive() && field.getExpireDate().isBefore(LocalDate.now())) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field property has not yet expired, but will be inactivated in the future.", ErrorSeverity.NOTIFICATION, field.getFieldId(), field.getFieldName()));
        }
        if (!field.getActive() && field.getExpireDate().isAfter(LocalDate.now())) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field is inactive, but expires in the future. Field was not treated as active by import", ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
        }
    }

    //TODO update with data types when enum built out.
    public static void validateGroupingType(FieldProperties field) {
        if (field.getGroupingType() == GroupingType.COMPOSITE
        && !field.getDataType().equals("label")) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "Composite field not associated with a non-value data type", ErrorSeverity.ERROR,
                    field.getFieldId(), field.getFieldName()));
        }
    }
}

