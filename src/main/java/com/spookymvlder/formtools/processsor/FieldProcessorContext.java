package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.FieldProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldProcessorContext {
    Map<String, FieldProperties> idToField = new HashMap<>();
    List<FieldProperties> skippableFields = new ArrayList<>();
    List<FieldProperties> inactiveFields = new ArrayList<>();
    //List<String> fieldNames = new ArrayList<>();
    FieldProperties root = null;
    List<FieldProperties> activeFields = new ArrayList<>();
    List<FieldProperties> allFields = new ArrayList<>();
    List<FieldProperties> rootList = new ArrayList<>();
    List<String> duplicateNames = new ArrayList<>();


    public List<FieldProperties> getAllFields() {
        return allFields;
    }

    public void setAllFields(List<FieldProperties> allFields) {
        this.allFields = allFields;
    }

    public Map<String, FieldProperties> getIdToField() {
        return idToField;
    }

    public void setIdToField(Map<String, FieldProperties> idToField) {
        this.idToField = idToField;
    }

    public FieldProperties getField(String id) {
        return idToField.get(id);
    }
    public void appendIdToField(String id, FieldProperties field) {
        idToField.put(id, field);
    }

    public void removeIdFromMap(String id) {
        idToField.remove(id);
    }

    public List<FieldProperties> getSkippableFields() {
        return skippableFields;
    }

    public void setSkippableFields(List<FieldProperties> skippableFields) {
        this.skippableFields = skippableFields;
    }

    public void appendSkippableFields(FieldProperties field) {
        this.skippableFields.add(field);
    }

    public List<FieldProperties> getInactiveFields() {
        return inactiveFields;
    }

    public void setInactiveFields(List<FieldProperties> inactiveFields) {
        this.inactiveFields = inactiveFields;
    }

    public void appendInactiveIds(FieldProperties field) {
        this.inactiveFields.add(field);
    }

    public FieldProperties getRoot() {
        return root;
    }

    public void setRoot(FieldProperties root) {
        this.root = root;
    }

    public List<FieldProperties> getActiveFields() {
        return activeFields;
    }

    public void setActiveFields(List<FieldProperties> activeFields) {
        this.activeFields = activeFields;
    }

    public void appendActiveFields(FieldProperties field) {
        this.activeFields.add(field);
    }

    public void removeFromActiveFields(FieldProperties field) {
        this.activeFields.remove(field);
    }

    public List<FieldProperties> getRootList() {
        return rootList;
    }

    public void setRootList(List<FieldProperties> rootList) {
        this.rootList = rootList;
    }

    public List<String> getDuplicateNames() {
        return duplicateNames;
    }

    public void setDuplicateNames(List<String> duplicateNames) {
        this.duplicateNames = duplicateNames;
    }
}
