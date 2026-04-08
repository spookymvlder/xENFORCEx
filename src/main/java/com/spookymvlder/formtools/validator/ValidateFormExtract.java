package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNumeric;

public class ValidateFormExtract {

    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateForm(List<FormInstance> instances, FieldProperty rootProperty) {
        Map<String, FieldProperty> propertyMap = buildPropertyMap(rootProperty);
        for (FormInstance instance : instances) {
            // Build a context so no need to pass around a bunch of hash maps.
            ExtractValidatorContext context = new ExtractValidatorContext();

            context.setPropertyMap(propertyMap);
            context.setFormMap(buildFormMap(instance));
            // For each field, run all rules from field property, use map to lookup related fields.
            context.setMissingFields(findMissingFields(context));

            // Make sure that we're not missing any nodes.
            validateRequiredFields(context.getFormForFieldId(rootProperty.getFieldId()).getFirst(), rootProperty);

        }
    }

    private static Map<String, FieldProperty> buildPropertyMap(FieldProperty root) {
        Map<String, FieldProperty> map = new HashMap<>();
        if (root == null) {
            return map;
        }

        Deque<FieldProperty> stack = new ArrayDeque<>();
        stack.push(root);

        while (!stack.isEmpty()) {
            FieldProperty property = stack.pop();

            if (property.getFieldId() != null) {
                map.put(property.getFieldId(), property);
            }

            for (FieldProperty child : property.getRelation().getChildFields()) {
                stack.push(child);
            }
        }

        return map;
    }

    private static Map<String, List<FormField>> buildFormMap(FormInstance instance) {
        Map<String, List<FormField>> map = new HashMap<>();
        FormField startField = instance.getStartField();
        if (startField == null) {
            return map;
        }

        Deque<FormField> stack = new ArrayDeque<>();
        stack.push(startField);

        while (!stack.isEmpty()) {
            FormField field = stack.pop();

            if (field.getProperty() != null) {
                String fieldId = field.getProperty().getFieldId();
                map.computeIfAbsent(fieldId, k -> new ArrayList<>()).add(field);
            }

            for (FormField child : field.getChildren()) {
                stack.push(child);
            }
        }

        return map;
    }

    private static List<String> findMissingFields(ExtractValidatorContext context) {
        Map<String, FieldProperty> map = context.getPropertyMap();
        List<String> missingFields = new ArrayList<>();
        for (String propertyId : map.keySet()) {
            if (!context.isFieldInMap(propertyId)) {
                missingFields.add(propertyId);
            }
        }
        return missingFields;
    }

//    private static List<String> findRequiredChildren(ExtractValidatorContext context) {
//        List<String> missingFields = context.getMissingFields();
//        for (String missingField : missingFields) {
//            FieldProperty field = context.getPropertyForFieldId(missingField);
//            //if (field.getCardinality().getRequired() == ConditionalBool.TRUE && )
//        }
//    }

    // TODO add conditional requirement check
    private static void validateRequiredFields(FormField formNode, FieldProperty propertyNode) {
        for (FieldProperty childProperty : propertyNode.getRelation().getChildFields()) {
            if (childProperty.getCardinality().getRequired() == ConditionalBool.TRUE) {
                boolean found = false;
                for (FormField childField : formNode.getChildren()) {
                    if (childProperty.getFieldId().equals(childField.getProperty().getFieldId())) {
                        found = true;
                        break;
                    }
                }
                //TODO new error type
                if (!found) {
                    errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                            "FieldProperty " + childProperty.getFieldId() + " is required, but missisng from FormField " +
                            formNode.getProperty().getFieldId() + "'s list of children.", ErrorSeverity.ERROR));
                }
            }
        }
        for (FormField childField : formNode.getChildren()) {
            validateRequiredFields(childField, childField.getProperty());
        }
    }

    private static void validateFieldValue(SimpleField formField, FieldProperty property) {
        FieldData value = formField.getValue();
        if (value.getDataType().isNumeric()) {
            validateNumericFieldValue(value, property);
        }

    }

    private static void validateNumericFieldValue(FieldData formValue, FieldProperty property) {
        try {
            Double value = Double.parseDouble(formValue.getValue());
            validateNumericRange(value, property);
        } catch (NumberFormatException e) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                            "that cannot be converted to a numeric data type, despite being a " +
                            property.getValueRestriction().getDataType() + " data type.",
                    ErrorSeverity.ERROR));
        }
    }

    private static void validateNumericRange(Double value, FieldProperty property) {
        Double rangeStart = (property.getValueRestriction().getRangeStart() != null)  ? Double.parseDouble(property.getValueRestriction().getRangeStart()) : Double.NEGATIVE_INFINITY;
        Double rangeEnd = (property.getValueRestriction().getRangeEnd()) != null ? Double.parseDouble(property.getValueRestriction().getRangeEnd()) : Double.POSITIVE_INFINITY;

        if ((property.getValueRestriction().getRangeStartInclusive() && value < rangeStart) ||
                (!property.getValueRestriction().getRangeStartInclusive() && value <= rangeStart)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "A form field for FieldProperty " + property.getFieldId() + " has been assigned a value " +
                            "that is less than the minimum range accepted for the field.",
                    ErrorSeverity.ERROR));
        }

        if ((property.getValueRestriction().getRangeEndInclusive() && value > rangeEnd) ||
                (!property.getValueRestriction().getRangeEndInclusive() && value >= rangeEnd)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                    "FieldProperty " + property.getFieldId() + " has been assigned a value that is greater than " +
                            "the maximum range accepted for the field.", ErrorSeverity.ERROR));
        }


    }
}