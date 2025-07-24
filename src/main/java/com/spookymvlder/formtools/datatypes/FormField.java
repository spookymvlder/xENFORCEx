package com.spookymvlder.formtools.datatypes;

import java.util.List;

public class FormField implements FormStructure {
    private String value;


    private String fieldLongName; //Used to map import settings to field type.
    private int sectionOrderIndex;
    private int repeatOrderIndex;
    private FieldProperties properties;
    private List<FormField> children;
    //To allow root to reference formInstance
    private FormStructure parent;
    private String relativePath = null;

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

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }
}
