package com.spookymvlder.formtools.datatypes;

public class FormField {
    private String value;
    private String fKey;

    private String fieldLongName; //Used to map import settings to field type.

    public FormField(String value, String fKey, String fieldLongName) {
        this.value = value;
        this.fKey = fKey;
        this.fieldLongName = fieldLongName;
    }

    public String getValue() {
        return value;
    }

    public String getfKey() {
        return fKey;
    }

    public String getFieldLongName() {
        return fieldLongName;
    }
}
