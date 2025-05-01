package com.spookymvlder.formtools.datatypes;

import java.util.List;
import java.util.Map;

public class FormSection {
    private String longName;
    private Map<String, FormField> fields;
    private List<FormSection> subSections;
    private List<FormField> repeatFields;
    private boolean restructured;


    public FormSection(String longName) {
        this.longName = longName;
    }

    public String getLongName() {
        return longName;
    }

    public Map<String, FormField> getFields() {
        if(!restructured) {
            this.restructureRepeatFields();
        }
        return fields;
    }

    public void addFields(String fieldName, FormField field) {
        if(fields.containsKey(fieldName)) {
            this.repeatFields.add(field);
        } else {
            this.fields.put(fieldName, field);
        }
    }

    public void restructureRepeatFields() {
        repeatFields.stream().map(FormField::getFieldLongName)
                .forEach(fieldName -> {
                    if(fields.containsKey(fieldName)) {
                        repeatFields.addFirst(fields.get(fieldName));
                        fields.remove(fieldName);
                    }
                });
        this.restructured = true;
    }

    public List<FormSection> getSubSections() {
        return subSections;
    }

    public void setSubSections(List<FormSection> subSections) {
        this.subSections = subSections;
    }

    public List<FormField> getRepeatFields() {
        if(!restructured) {
            this.restructureRepeatFields();
        }
        return repeatFields;
    }

    public boolean isRestructured() {
        return restructured;
    }

}
