package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.Usage;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldCardinality;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ValidateFormExtract {

    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateForm(List<FormInstance> instances, FieldProperty rootProperty) {
        Map<String, FieldProperty> propertyMap = buildPropertyMap(rootProperty);
        Map<String, List<String>> schemaPaths = buildSchemaPaths(rootProperty);
        for (FormInstance instance : instances) {
            ExtractValidatorContext context = new ExtractValidatorContext();
            context.setRootProperty(rootProperty);
            context.setStartField(instance.getStartField());
            context.setPropertyMap(propertyMap);
            context.setSchemaPaths(schemaPaths);
            context.setFormMap(buildFormMap(instance));
            context.setMissingFields(findMissingFields(context));

            List<Runnable> validationSteps = List.of(
                    () -> validateCardinality(context),
                    () -> validateFieldValues(context),
                    () -> validateRequiredFields(context)
            );
            for (Runnable step : validationSteps) {
                step.run();
                if (errors.checkCurrentLogOverThreshold()) {
                    System.out.println("Breaking due to errors!");
                    break;
                }
            }
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

    private static Map<String, List<String>> buildSchemaPaths(FieldProperty root) {
        Map<String, List<String>> map = new HashMap<>();
        if (root == null) {
            return map;
        }
        accumulateSchemaPath(root, new ArrayList<>(), map);
        return map;
    }

    private static void accumulateSchemaPath(FieldProperty node, List<String> ancestorPath, Map<String, List<String>> map) {
        List<String> currentPath = new ArrayList<>(ancestorPath);
        currentPath.add(node.getFieldId());
        map.put(node.getFieldId(), currentPath);
        for (FieldProperty child : node.getRelation().getChildFields()) {
            accumulateSchemaPath(child, currentPath, map);
        }
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

    private static void validateRequiredFields(ExtractValidatorContext context) {
        FormField start = context.getStartField();
        if (start == null) {
            return;
        }
        walkRequiredFields(start, context.getRootProperty(), context);
    }

    private static void walkRequiredFields(FormField formNode, FieldProperty propertyNode,
                                           ExtractValidatorContext context) {
        for (FieldProperty childProperty : propertyNode.getRelation().getChildFields()) {
            Usage usage = childProperty.getCardinality().getRequired();
            if (usage == null || usage == Usage.O) {
                continue;
            }
            boolean conditionMet = !usage.isConditional()
                    || ConditionalRequirementEvaluator.isConditionRequired(formNode, childProperty, context);
            if (!conditionMet) {
                continue;
            }

            List<FormField> matches = new ArrayList<>();
            for (FormField childField : formNode.getChildren()) {
                if (childProperty.getFieldId().equals(childField.getProperty().getFieldId())) {
                    matches.add(childField);
                }
            }
            //TODO new error type
            if (matches.isEmpty()) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "FieldProperty " + childProperty.getFieldId() + " is required, but missing from FormField " +
                        formNode.getProperty().getFieldId() + "'s list of children.", ErrorSeverity.ERROR));
            } else if (usage.requiresPopulated()) {
                for (FormField match : matches) {
                    if (!ConditionalRequirementEvaluator.isPopulated(match)) {
                        errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                                "FieldProperty " + childProperty.getFieldId() + " is required and must be populated, " +
                                "but is present-but-empty under FormField " +
                                formNode.getProperty().getFieldId() + ".", ErrorSeverity.ERROR));
                    }
                }
            }
        }
        for (FormField childField : formNode.getChildren()) {
            walkRequiredFields(childField, childField.getProperty(), context);
        }
    }

    private static void validateCardinality(ExtractValidatorContext context) {
        FormField start = context.getStartField();
        if (start == null) {
            return;
        }
        walkCardinality(start, context.getRootProperty());
    }

    private static void walkCardinality(FormField formNode, FieldProperty propertyNode) {
        Map<String, Integer> childCounts = new HashMap<>();
        for (FormField childField : formNode.getChildren()) {
            FieldProperty childProperty = childField.getProperty();
            if (childProperty == null) {
                continue;
            }
            childCounts.merge(childProperty.getFieldId(), 1, Integer::sum);
        }

        for (FieldProperty childProperty : propertyNode.getRelation().getChildFields()) {
            FieldCardinality cardinality = childProperty.getCardinality();
            int count = childCounts.getOrDefault(childProperty.getFieldId(), 0);

            if (cardinality.getMinOccurs() != null && count < cardinality.getMinOccurs()) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "FieldProperty " + childProperty.getFieldId() + " appears " + count +
                                " time(s) under FormField " + formNode.getProperty().getFieldId() +
                                ", below the minimum of " + cardinality.getMinOccurs() + ".",
                        ErrorSeverity.ERROR));
            }
            if (cardinality.getMaxOccurs() != null && count > cardinality.getMaxOccurs()) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "FieldProperty " + childProperty.getFieldId() + " appears " + count +
                                " time(s) under FormField " + formNode.getProperty().getFieldId() +
                                ", above the maximum of " + cardinality.getMaxOccurs() + ".",
                        ErrorSeverity.ERROR));
            }
        }

        for (FormField childField : formNode.getChildren()) {
            walkCardinality(childField, childField.getProperty());
        }
    }

    private static void validateFieldValues(ExtractValidatorContext context) {
        FormField start = context.getStartField();
        if (start == null) {
            return;
        }
        walkFieldValues(start);
    }

    private static void walkFieldValues(FormField formNode) {
        SingleFormFieldValidator.validateWithErrors(formNode);
        for (FormField childField : formNode.getChildren()) {
            walkFieldValues(childField);
        }
    }
}
