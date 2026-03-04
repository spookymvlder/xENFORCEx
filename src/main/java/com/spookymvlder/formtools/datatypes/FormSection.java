package com.spookymvlder.formtools.datatypes;

import com.spookymvlder.formtools.datatypes.fields.FormField;

import java.util.*;

public class FormSection {
    private String longName;
    private Map<String, List<FormField>> fieldsByName = new LinkedHashMap<>();
    private List<FormField> fieldAppearanceOrder = new ArrayList<>();
    private List<FormSection> subSections = new ArrayList<>();



    public FormSection(String longName) {
        this.longName = longName;
    }

    public String getLongName() {
        return this.longName;
    }

    public void addField(String fieldName, FormField field) {
        this.fieldsByName.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(field);
        this.fieldAppearanceOrder.add(field);
    }

    //TODO evaluate if necessary
    public Map<String, List<FormField>> getAllFields() {
        return this.fieldsByName;
    }

    //TODO consider returning default empty list?
    public List<FormField> getFields(String fieldName) {
        return this.fieldsByName.get(fieldName);
    }

    public Optional<FormField> getFirstField(String fieldName) {
        List<FormField> list = this.fieldsByName.get(fieldName);
        return (list != null && !list.isEmpty()) ? Optional.of(list.getFirst()) : Optional.empty();
    }

    public List<FormField> getFieldAppearanceOrder() {
        return this.fieldAppearanceOrder;
    }

    public List<FormSection> getSubSections() {
        return this.subSections;
    }

    public void setSubSections(List<FormSection> subSections) {
        this.subSections = subSections;
    }

    public int getFieldCount() {
        return this.fieldAppearanceOrder.size();
    }

    //If there is a list for this field for this field name, return the size, else return 1.
    public int getFieldCountByName(String fieldName) {
        return this.fieldsByName.get(fieldName) != null ? this.fieldsByName.get(fieldName).size() : 0;
    }


}
