package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
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

    }

    public static void validateAttributes(FieldProperty field) {
        FieldValueRestriction value = field.getValue();
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
        FieldValueRestriction value = field.getValue();
        if (value.getRangeStart() != null && value.getRangeEnd() != null) {
            if (value.getRangeStart() > value.getRangeEnd()) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"rangeStart must be less than or equal to rangeEnd.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
            if (value.getRangeStart().equals(value.getRangeEnd())) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,"No valid values for ranges.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
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
                && field.getValue().getDataType() != null) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_FIELD_PROPERTY_VALIDATION,
                    field.getRelation().getGroupingType() + " field associated with a data type",
                    ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

}

