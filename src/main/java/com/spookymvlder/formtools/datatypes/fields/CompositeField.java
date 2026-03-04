package com.spookymvlder.formtools.datatypes.fields;

import com.spookymvlder.formtools.datatypes.FieldData;
import com.spookymvlder.formtools.datatypes.FieldProperty;

import java.util.Map;

public class CompositeField extends EntryField {
    private Map<String, FieldData> constituents;

    public CompositeField(String displayName, FieldProperty property, FormStructure parent, Map<String, FieldData> constituents) {
        super(displayName, property, parent);
        this.constituents = constituents;
    }


    public CompositeField(String displayName, FieldProperty property, String resolvedPath, FormStructure parent, Map<String, FieldData> constituents) {
        super(displayName, property, resolvedPath, parent);
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
