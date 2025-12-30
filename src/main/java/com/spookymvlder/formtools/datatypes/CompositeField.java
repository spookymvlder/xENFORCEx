package com.spookymvlder.formtools.datatypes;

import java.util.HashMap;
import java.util.Map;

public class CompositeField extends FormField {
    private Map<String, FieldData> constituents;

    public CompositeField(String displayName, FieldProperty property, FormStructure parent, Map<String, FieldData> constituents) {
        super(displayName, property, parent);
        this.constituents = constituents;
    }


    public CompositeField(String displayName, FieldProperty property, String retrievedPath, FormStructure parent, Map<String, FieldData> constituents) {
        super(displayName, property, retrievedPath, parent);
        this.constituents = constituents;
    }

    @Override
    public Map<String, FieldData> getValue() {
        return constituents;
    }

    public FieldData getConstituent(String key) {
        return constituents.get(key);
    }

}
