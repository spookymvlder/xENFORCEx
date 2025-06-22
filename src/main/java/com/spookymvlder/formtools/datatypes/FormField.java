package com.spookymvlder.formtools.datatypes;

public class FormField {
    private String value;
    //TODO move foreignKey to a metadata class if necessary.
    //private String fKey;

    private String fieldLongName; //Used to map import settings to field type.
    private int sectionOrderIndex;
    private int repeatOrderIndex;

    public FormField(String value, String fieldLongName, int sectionOrderIndex, int repeatOrderIndex) {
        this.value = value;
        this.fieldLongName = fieldLongName;
        this.sectionOrderIndex = sectionOrderIndex;
        this.repeatOrderIndex = repeatOrderIndex;
    }

    public String getValue() {
        return value;
    }

    public String getFieldLongName() {
        return fieldLongName;
    }

    public int getSectionOrderIndex() {
        return sectionOrderIndex;
    }

    public int getRepeatOrderIndex() {
        return repeatOrderIndex;
    }
}
