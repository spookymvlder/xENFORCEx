package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;

import java.util.ArrayList;
import java.util.List;

public class FieldPropertyValidator {

    public static List<ErrorEntry> validateWithErrors(FieldProperties field)  {
        List<ErrorEntry> errors = new ArrayList<>();
        errors.addAll(validateAttributes(field));
        errors.addAll(validateRanges(field));
        errors.addAll(validateRepeatOccurrences(field));

        return errors;
    }

    public static List<ErrorEntry> validateAttributes(FieldProperties field) {
        List<ErrorEntry> errors = new ArrayList<>();
        //Unless default to no or assume if attribute name is populated it is an attribute?
        if (field.getAttribute() == null && field.getAttributeName() != null) {
            errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"isAttribute is required when attributeName is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
        }
        if (field.getAttributeName() == null && field.getAttribute() != null) {
            errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"attributeName is required when isAttribute is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
        }
        return errors;
    }


    public static List<ErrorEntry> validateRanges(FieldProperties field) {
        List<ErrorEntry> errors = new ArrayList<>();
        boolean ranges = (field.getRangeStart() != null) || (field.getRangeEnd() != null);
        if (ranges && field.getRangeInclusive() == null) {
            errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"rangeInclusive is required if rangeStart or rangeEnd are set.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
        }

        if (field.getRangeStart() != null && field.getRangeEnd() != null) {
            if (field.getRangeStart() > field.getRangeEnd()) {
                errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"rangeStart must be less than or equal to rangeEnd.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
            }
            if (field.getRangeStart().equals(field.getRangeEnd()) && Boolean.FALSE.equals(field.getRangeInclusive())) {
                errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"No valid values for ranges.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
            }
        }
        return errors;
    }

    public static List<ErrorEntry> validateRepeatOccurrences(FieldProperties field) {
        List<ErrorEntry> errors = new ArrayList<>();
        //Quit early if field is not repeated.
        if (field.getFieldRepeated().equals('F')) {
            return errors;
        }
        if (field.getMinOccurs() != null && field.getMaxOccurs() != null) {
            if (field.getMinOccurs() > field.getMaxOccurs()) {
                errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs must be less than or equal to maxOccurs.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
            }
        }
        if (field.getMaxOccurs() != null && field.getMaxOccurs() < 2) {
            errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"maxOccurs, if populated, must be greater than 1.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
        }
        if (field.getMinOccurs() != null && field.getMinOccurs() < 0) {
            errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs, if populated, must be at least 0.", ErrorSeverity.ERROR, field.getFieldId(), field.getAttributeName()));
        }
        return errors;
    }
}
