package com.spookymvlder.formtools.datatypes.fields;

import com.spookymvlder.formtools.datatypes.FieldProperty;

import java.util.ArrayList;
import java.util.List;

public abstract class FormField implements FormStructure {



    protected String displayName; //Used to map import settings to field type.
    protected FieldProperty property;
    protected List<FormField> children = new ArrayList<FormField>();
    //To allow root to reference formInstance
    protected FormStructure parent;
    protected String resolvedPath;
    protected String label;


    public FormField(String displayName, FieldProperty property, FormStructure parent) {
        this.displayName = displayName;
        this.property = property;
        this.parent = parent;
    }

    public FormField(String displayName, FieldProperty property) {
        this.displayName = displayName;
        this.property = property;
    }

    public FormField(FieldProperty property, String resolvedPath, FormStructure parent) {
        this.property = property;
        this.resolvedPath = resolvedPath;
        this.parent = parent;
    }
    public FormField(FieldProperty property, String resolvedPath) {
        this.property = property;
        this.resolvedPath = resolvedPath;
    }

    public FormField(String displayName, FieldProperty property, String resolvedPath, FormStructure parent) {
        this.displayName = displayName;
        this.property = property;
        this.resolvedPath = resolvedPath;
        this.parent = parent;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getResolvedPath() {
        return resolvedPath;
    }

    public FieldProperty getProperty() {
        return property;
    }

    public List<FormField> getChildren() {
        return children;
    }

    //TODO ensure no circular reference
    public void addChild(FormField child) {
        this.children.add(child);
    }

    public FormStructure getParent() {
        return parent;
    }

    //TODO ensure no circular reference
    public void setParent(FormStructure parent) {
        this.parent = parent;
    }

    public void setResolvedPath(String resolvedPath) {
        this.resolvedPath = resolvedPath;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }



    public boolean isComposite() {
        return false;
    }
}
