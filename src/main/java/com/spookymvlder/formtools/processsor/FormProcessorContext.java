package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FormField;

public class FormProcessorContext {
    private FieldProperty rootProperty;
    private FormField startField;

    public FieldProperty getRootProperty() {
        return rootProperty;
    }

    public void setRootProperty(FieldProperty rootProperty) {
        this.rootProperty = rootProperty;
    }

    public FormField getStartField() {
        return startField;
    }

    public void setStartField(FormField startField) {
        this.startField = startField;
    }
}
