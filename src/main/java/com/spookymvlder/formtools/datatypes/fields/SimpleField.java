package com.spookymvlder.formtools.datatypes.fields;

import com.spookymvlder.formtools.datatypes.FieldData;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;

public class SimpleField extends EntryField {
    private FieldData value;
    //private int position;

    public SimpleField(String displayName, FieldProperty property, FormStructure parent, FieldData value) {
        super(displayName, property, parent);
        this.value = value;
    }

    public SimpleField(String displayName, FieldProperty property, FieldData value) {
        super(displayName, property);
        this.value = value;
    }

    public SimpleField(String displayName, FieldProperty property, String resolvedPath, FormStructure parent, FieldData value) {
        super(displayName, property, resolvedPath, parent);
        this.value = value;
    }



    /*public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }*/

    @Override
    public FieldData getValue() {
        return value;
    }




}
