package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.FormStructure;

import java.util.ArrayList;
import java.util.List;

//TODO make abstract form piece and inherit.
public class FormInstance implements FormStructure {
    private String formName;
    private FormField startField;
    private String metadata;
    private List<FormField> children = new ArrayList<FormField>();


    public FormInstance(String formName, FormField startField) {
        this.formName = formName;
        this.startField = startField;
    }

    public String getFormName() {
        return formName;
    }

    public FormField getStartField() {
        return startField;
    }

    public List<FormField> getChildren() {
        return children;
    }

    public void setChildren(List<FormField> children) {
        this.children = children;
    }

    public void addChild(FormField child) {
        children.add(child);
    }
}
