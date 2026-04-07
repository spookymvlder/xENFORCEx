package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.settings.ErrorSettings;

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
        for (FormInstance instance : instances) {
            Map<String, List<FormField>> instanceMap = buildFormMap(instance);

            // For each field, run all rules from field property, use map to lookup related fields.

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


}