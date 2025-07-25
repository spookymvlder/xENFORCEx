package com.spookymvlder.formtools.datatypes;

import java.util.HashMap;
import java.util.List;

public class FormField implements FormStructure {
    private List<FieldData> data;

    //TODO deprecate
    private String value;


    private String fieldLongName; //Used to map import settings to field type.
    private int sectionOrderIndex;
    private int repeatOrderIndex;
    private FieldProperty property;
    private List<FormField> children;
    //To allow root to reference formInstance
    private FormStructure parent;
    private String retrievedPath;
    private HashMap<String, String> constituents;
    private String label;

    public FormField(String value, String fieldLongName, int sectionOrderIndex, int repeatOrderIndex, FieldProperty property) {
        this.value = value;
        this.fieldLongName = fieldLongName;
        this.sectionOrderIndex = sectionOrderIndex;
        this.repeatOrderIndex = repeatOrderIndex;
        this.property = property;
    }

    public FormField(List<FieldData> data, String fieldLongName,  FieldProperty property) {
        this.data = data;
        this.fieldLongName = fieldLongName;
        this.property = property;

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

    public String getRetrievedPath() {
        return retrievedPath;
    }

    public FieldProperty getProperty() {
        return property;
    }

    public List<FormField> getChildren() {
        return children;
    }

    public void addChild(FormField child) {
        this.children.add(child);
    }

    public FormStructure getParent() {
        return parent;
    }

    public void setParent(FormStructure parent) {
        this.parent = parent;
    }

    public void setRetrievedPath(String retrievedPath) {
        this.retrievedPath = retrievedPath;
    }

    public HashMap<String, String> getConstituents() {
        return constituents;
    }

    public void setConstituents(HashMap<String, String> constituents) {
        this.constituents = constituents;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
