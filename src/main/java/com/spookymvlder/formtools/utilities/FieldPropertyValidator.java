package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;

import java.util.ArrayList;
import java.util.List;

public class FieldPropertyValidator {

    public static List<FieldPropertyValidationError> validateWithErrors(FieldProperties field)  {
        List<FieldPropertyValidationError> errors = new ArrayList<>();
        errors.addAll(validateAttributes(field));
        errors.addAll(validateRanges(field));

        return errors;
    }

    public static List<FieldPropertyValidationError> validateAttributes(FieldProperties field) {
        List<FieldPropertyValidationError> errors = new ArrayList<>();
        //Unless default to no or assume if attribute name is populated it is an attribute?
        if (field.getAttribute() == null && field.getAttributeName() != null) {
            errors.add(new FieldPropertyValidationError(field.getFieldId(), field.getAttributeName(), "isAttribute is required when attributeName is set.", ErrorSeverity.WARNING));
        }
        if (field.getAttributeName() == null && field.getAttribute() != null) {
            errors.add(new FieldPropertyValidationError(field.getFieldId(), field.getAttributeName(), "attributeName is required when isAttribute is set.", ErrorSeverity.ERROR));
        }
        return errors;
    }


    public static List<FieldPropertyValidationError> validateRanges(FieldProperties field) {
        List<FieldPropertyValidationError> errors = new ArrayList<>();
        boolean ranges = (field.getRangeStart() != null) || (field.getRangeEnd() != null);
        if (ranges && field.getRangeInclusive() == null) {
            errors.add(new FieldPropertyValidationError(field.getFieldId(), field.getAttributeName(), "rangeInclusive is required if rangeStart or rangeEnd are set.", ErrorSeverity.ERROR));
        }

        if (field.getRangeStart() != null && field.getRangeEnd() != null) {
            if (field.getRangeStart() > field.getRangeEnd()) {
                errors.add(new FieldPropertyValidationError(field.getFieldId(), field.getAttributeName(), "rangeStart must be less than or equal to rangeEnd.", ErrorSeverity.ERROR));
            }
            if (field.getRangeStart().equals(field.getRangeEnd()) && Boolean.FALSE.equals(field.getRangeInclusive())) {
                errors.add(new FieldPropertyValidationError(field.getFieldId(), field.getAttributeName(), "No valid values for ranges.", ErrorSeverity.ERROR));
            }
        }
        return errors;
    }
}
