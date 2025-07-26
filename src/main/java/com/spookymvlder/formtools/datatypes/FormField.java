package com.spookymvlder.formtools.datatypes;

import java.util.List;

public abstract class FormField<T> implements FormStructure {



    protected String displayName; //Used to map import settings to field type.
    protected FieldProperty property;
    protected List<FormField<?>> children;
    //To allow root to reference formInstance
    protected FormStructure parent;
    protected String retrievedPath;
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

    public String getDisplayName() {
        return displayName;
    }

    public String getRetrievedPath() {
        return retrievedPath;
    }

    public FieldProperty getProperty() {
        return property;
    }

    public List<FormField<?>> getChildren() {
        return children;
    }

    public void addChild(FormField<?> child) {
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


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public abstract T getValue();
}
