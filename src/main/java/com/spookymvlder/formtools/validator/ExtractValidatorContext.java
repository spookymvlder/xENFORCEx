package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FormField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtractValidatorContext {
    Map<String, FieldProperty> propertyMap = new HashMap<>();
    Map<String, List<FormField>> formMap = new HashMap<>();
    Map<String, List<String>> attributeMap = new HashMap<>();
    List<String> missingFields = new ArrayList<>();

    public Map<String, FieldProperty> getPropertyMap() {
        return propertyMap;
    }

    public FieldProperty getPropertyForFieldId(String fieldId) {
        return propertyMap.get(fieldId);
    }

    public void setPropertyMap(Map<String, FieldProperty> propertyMap) {
        this.propertyMap = propertyMap;
    }



    public Map<String, List<FormField>> getFormMap() {
        return formMap;
    }

    public boolean isFieldInMap(String fieldId) {
        return formMap.containsKey(fieldId);
    }

    public List<FormField> getFormForFieldId(String fieldId) {
        return formMap.get(fieldId);
    }

    public void setFormMap(Map<String, List<FormField>> formMap) {
        this.formMap = formMap;
    }

    public void setAttributeMap(Map<String, List<String>> attributeMap) {
        this.attributeMap = attributeMap;
    }


    public List<String> getMissingFields() {
        return this.missingFields;
    }

    public void setMissingFields(List<String> missingFields) {
        this.missingFields = missingFields;
    }

}
