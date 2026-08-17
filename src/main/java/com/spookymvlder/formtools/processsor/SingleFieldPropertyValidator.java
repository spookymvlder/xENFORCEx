package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.TypedPredicateFactory;
import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldCardinality;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldIdentity;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldValueRestriction;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.time.LocalDate;

//TODO add validation that name is unique
public class SingleFieldPropertyValidator {
    static final ErrorSettings errors = ErrorSettings.getInstance();
    //TODO store to error settings and return highest error level for field.

    public static void validateWithErrors(FieldProperty field)  {
        validateAttributes(field);
        validateRanges(field);
        validateRepeatOccurrences(field);
        validateActiveRange(field);
        validateGroupingType(field);
        validateEnumerations(field);
    }

    public static void validateEnumerations(FieldProperty field) {
        FieldValueRestriction value = field.getValueRestriction();
        boolean hasEnumerations = value.getEnumerations() != null && !value.getEnumerations().isEmpty();
        boolean isEnumerationType = value.getDataType() == FieldDataType.ENUMERATION;

        if (isEnumerationType && !hasEnumerations) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "FieldProperty with dataType ENUMERATION must declare at least one entry in the enumeration list.",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
        if (!isEnumerationType && hasEnumerations) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "The enumerations column is only valid when dataType is ENUMERATION. " +
                            "Use ENUMERATION (or the future CODED_VALUE / CUSTOM types) for restricted value sets.",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

    public static void validateAttributes(FieldProperty field) {
        FieldValueRestriction value = field.getValueRestriction();
        //If there is a name for the attribute and isAttribute is not true
        if (!StringUtils.isNullOrEmpty(value.getAttributeName()) && !value.getAttribute()) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"isAttribute is required when attributeName is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
        //If there is not a name for the attribute and isAttribute is true
        if (StringUtils.isNullOrEmpty(value.getAttributeName()) && value.getAttribute()) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"attributeName is required when isAttribute is set.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }

    }


    public static void validateRanges(FieldProperty field) {
        FieldValueRestriction value = field.getValueRestriction();
        FieldDataType type = value.getDataType();
        String rangeStart = value.getRangeStart();
        String rangeEnd = value.getRangeEnd();

        if (rangeStart == null && rangeEnd == null) {
            return;
        }

        if (type == null || !TypedPredicateFactory.supportsRanges(type)) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "rangeStart/rangeEnd are only valid for ordered data types (numeric, DATE, TIME, " +
                            "DATETIME, GYEAR, GYEARMONTH, GMONTH).",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            return;
        }

        if (rangeStart != null && !TypedPredicateFactory.isParseable(rangeStart, type)) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "rangeStart value " + rangeStart + " cannot be parsed as a " + type + ".",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            return;
        }
        if (rangeEnd != null && !TypedPredicateFactory.isParseable(rangeEnd, type)) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    "rangeEnd value " + rangeEnd + " cannot be parsed as a " + type + ".",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            return;
        }

        if (rangeStart != null && rangeEnd != null) {
            Integer cmp = TypedPredicateFactory.compareValues(rangeStart, rangeEnd, type);
            if (cmp != null) {
                if (cmp > 0) {
                    errors.addToCurrentLog(new FieldPropertyValidationError(
                            ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                            "rangeStart must be less than or equal to rangeEnd.",
                            ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
                } else if (cmp == 0) {
                    errors.addToCurrentLog(new FieldPropertyValidationError(
                            ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                            "rangeStart equals rangeEnd; no valid values in range.",
                            ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
                }
            }
        }
    }



    public static void validateRepeatOccurrences(FieldProperty field) {
        FieldCardinality cardinality = field.getCardinality();
        //Quit early if field is not repeated.
        if (cardinality.getFieldRepeated().equals(ConditionalBool.FALSE)) {
            return;
        }
        if (cardinality.getMinOccurs() != null && cardinality.getMaxOccurs() != null) {
            if (cardinality.getMinOccurs() > cardinality.getMaxOccurs()) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs must be less than or equal to maxOccurs.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
        }
        if (cardinality.getMaxOccurs() != null && cardinality.getMaxOccurs() < 2) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"maxOccurs, if populated, must be greater than 1.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
        if (cardinality.getMinOccurs() != null && cardinality.getMinOccurs() < 0) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"minOccurs, if populated, must be at least 0.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

    public static void validateActiveRange(FieldProperty field) {
        FieldIdentity identity = field.getIdentity();
        if (identity.getActive() && identity.isExpired(LocalDate.now().minusDays(1))) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field is listed as active, but has an expiration date set in the past.", ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
        }
        if (identity.getActive() && identity.expiresInFuture(LocalDate.now())) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field property has not yet expired, but will be inactivated in the future.", ErrorSeverity.NOTIFICATION, field.getFieldId(), field.getFieldName()));
        }
        if (!identity.getActive() && identity.expiresInFuture(LocalDate.now())) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION, "Field is inactive, but expires in the future. Field was not treated as active by import", ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
        }
    }

    //TODO update with data types when enum built out.
    //TODO consider composite field error.
    public static void validateGroupingType(FieldProperty field) {
        if ((field.getRelation().getGroupingType() == GroupingType.CONTAINER
                || field.getRelation().getGroupingType() == GroupingType.COMPOSITE)
                && field.getValueRestriction().getDataType() != null) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    field.getRelation().getGroupingType() + " field associated with a data type",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

}

