package com.spookymvlder.formtools.datatypes;

public class SimpleField extends FormField<FieldData> {
    private FieldData value;
    private int position;

    public SimpleField(String displayName, FieldProperty property, FormStructure parent, FieldData value) {
        super(displayName, property, parent);
        this.value = value;
    }

    public SimpleField(String displayName, FieldProperty property, FieldData value) {
        super(displayName, property);
        this.value = value;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public FieldData getValue() {
        return value;
    }


}
