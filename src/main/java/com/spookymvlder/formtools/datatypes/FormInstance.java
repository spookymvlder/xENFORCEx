package com.spookymvlder.formtools.datatypes;

import java.util.List;

//TODO make abstract form piece and inherit.
public class FormInstance implements FormStructure {
    private String longName;
    private FormField startField;
    private String metadata;


    public FormInstance(String longName, FormField startField) {
        this.longName = longName;
        this.startField = startField;
    }

    public String getLongName() {
        return longName;
    }

    public FormField getStartField() {
        return startField;
    }

    public List<FormField<?>> getChildren() {
        return children;
    }

    public void setChildren(List<FormField<?>> children) {
        this.children = children;
    }

    public void addChild(FormField<?> child) {
        children.add(child);
    }
}
