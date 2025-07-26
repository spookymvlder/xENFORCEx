package com.spookymvlder.formtools.datatypes;

import java.util.HashMap;
import java.util.Map;

public class CompositeField extends FormField<Map<String, FieldData>> {
    private Map<String, FieldData> constituents = new HashMap<>();

    public CompositeField(String displayName, FieldProperty property, FormStructure parent, Map<String, FieldData> constituents) {
        super(displayName, property, parent);
        this.constituents = constituents;
    }

    @Override
    public Map<String, FieldData> getValue() {
        return constituents;
    }

    public FieldData getConstituent(String name) {
        return constituents.get(name);
    }
}
