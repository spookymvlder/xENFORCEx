package com.spookymvlder.formtools.datatypes.fields;

import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;

public abstract class EntryField extends FormField{

    public EntryField(String displayName, FieldProperty property, FormStructure parent) {
        super(displayName, property, parent);
    }



    public EntryField(String displayName, FieldProperty property, String retrievedPath, FormStructure parent) {
        super(displayName, property, retrievedPath, parent);
    }

    public EntryField(String displayName, FieldProperty property) {
        super(displayName, property);
    }

    public abstract Object getValue();
}
