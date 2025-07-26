package com.spookymvlder.formtools.datatypes;

public class SimpleField extends FormField<FieldData> {
    private FieldData value;

    public SimpleField(String displayName, FieldProperty property, FormStructure parent, FieldData value) {
        super(displayName, property, parent);
        this.value = value;
    }

    public SimpleField(String displayName, FieldProperty property, FieldData value) {
        super(displayName, property);
        this.value = value;
    }

    @Override
    public FieldData getValue() {
        return value;
    }


}
