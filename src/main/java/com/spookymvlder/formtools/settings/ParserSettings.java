package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.utilities.FieldPropertyConverter;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParserSettings {

    private static ParserSettings instance;



    //Where the parsing should begin.
    //TODO double check this vs form root and see if there's a difference.
    private String formStart;
    //TODO convert to linked hash set
    //Section header to all fields associated with that section.
    private Map<String, List<String>> formSectionToFields;
    //Maps field name to filepath.
    private Map<String, String> formFieldToPath;
    //Order of form sections, starting with first child node, navigating its children, then moving on to  the next. Depth first searching.
    //Without this, section navigation is random because stored as set.
    private List<String> sectionOrder;
    //Helps with repeating fields.
    private String foreignKey;
    //Section to its subsections
    private Map<String, List<String>> subsections;
    //Sections that are children of the form instance node. Can probably just get first and figure it out. Assumes instance has no data
    private List<String> topLevelSections;
    //Path of sections
    private Map<String,String> sectionNameToPath;
    private String dateFormat;
    private Map<String,String> fieldIdToName;
    //TODO replace fieldIdToName with idToField.
    private Map<String, FieldProperties> idToField;

    private ParserSettings() {
        this.formStart = null;
        this.formSectionToFields = null;
        this.formFieldToPath = null;
        this.sectionOrder = null;
        this.subsections = null;
        this.topLevelSections = null;
        this.sectionNameToPath = null;
        this.dateFormat = "dd-MM-yyyy";
        this.fieldIdToName = new HashMap<>();
        this.idToField = new HashMap<>();
    }

    public Map<String, String> getFieldIdToName() {
        return fieldIdToName;
    }

    public void addToFieldIdToName(String fieldId, String fieldName) {
        this.fieldIdToName.put(fieldId, fieldName);
    }

    //TODO null handle
    public FieldProperties getFieldFromId(String fieldId) {
        return idToField.get(fieldId);
    }

    public void addToFieldFromId(String fieldId, FieldProperties field) {
        this.idToField.put(fieldId, field);
    }

    public String getFieldNameFromId(String fieldId) {
        return this.fieldIdToName.get(fieldId);
    }

//TODO - ensure that sectionOrder, formSectionToFields, and formFieldToPath all line up

    public static ParserSettings getInstance() {
        if (instance == null) {
            instance = new ParserSettings();
        }
        return instance;
    }

    public void setConverters() {
        FieldPropertyConverter.CustomDateConverter.setFormatter(DateTimeFormatter.ofPattern(this.dateFormat));
    }

    public String getFormStart() {
        return formStart;
    }

    public void setFormStart(String formStart) {
        this.formStart = formStart;
    }

    public Map<String, List<String>> getFormSectionToFields() {
        return formSectionToFields;
    }

    public void appendFormSectionToFields(String parent, String fieldName) {
        this.formSectionToFields.computeIfAbsent(parent, k -> new ArrayList<>()).add(fieldName);
    }

    public void setFormSectionToFields(Map<String, List<String>> formSectionToFields) {
        this.formSectionToFields = formSectionToFields;
    }

    public List<String> getFormFieldsForSection(String section) {
        return this.formSectionToFields.getOrDefault(section, new ArrayList<>());
    }

    public Map<String, String> getFormFieldToPath() {
        return formFieldToPath;
    }

    public void setFormFieldToPath(Map<String, String> formFieldToPath) {
        this.formFieldToPath = formFieldToPath;
    }

    public List<String> getSectionOrder() {
        return sectionOrder;
    }

    public void setSectionOrder(List<String> sectionOrder) {
        this.sectionOrder = sectionOrder;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public void setForeignKey(String foreignKey) {
        this.foreignKey = foreignKey;
    }

    public List<String> getSubsectionsOf(String section) {
        return this.subsections.get(section);
    }

    public void setSubsections(Map<String, List<String>> subsections) {
        this.subsections = subsections;
    }

    public List<String> getTopLevelSections(){
        return this.topLevelSections;
    }

    //TODO add protections?
    public void setTopLevelSections(List<String> rootSections) {
        this.topLevelSections = rootSections;
    }

    public Map<String, String> getSectionNameToPath() {
        return this.sectionNameToPath;
    }

    public void setSectionNameToPath(Map<String, String> sectionNameToPath) {
        this.sectionNameToPath = sectionNameToPath;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }
}
