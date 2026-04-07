package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldPropertyRow;
import com.spookymvlder.formtools.datatypes.fieldproperties.ResolvedFieldPropertyRow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FieldProcessorContext {
    Map<String, FieldProperty> idToField = new HashMap<>();
    List<FieldProperty> skippableFields = new ArrayList<>();
    List<FieldProperty> inactiveFields = new ArrayList<>();
    //List<String> fieldNames = new ArrayList<>();
    FieldProperty root = null;
    List<FieldProperty> activeFields = new ArrayList<>();
    List<FieldProperty> allFields = new ArrayList<>();
    List<FieldPropertyRow> allRows = new ArrayList<>();



    List<ResolvedFieldPropertyRow> allResolvedRows = new ArrayList<>();
    List<FieldProperty> rootList = new ArrayList<>();
    List<String> duplicateNames = new ArrayList<>();
    List<FieldProperty> anchorList = new ArrayList<>();
    FieldProperty anchor = null;


    public List<FieldProperty> getAllFields() {
        return allFields;
    }

    public List<ResolvedFieldPropertyRow> getAllResolvedRows() {
        return allResolvedRows;
    }

    public void setAllResolvedRows(List<ResolvedFieldPropertyRow> allResolvedRows) {
        this.allResolvedRows = allResolvedRows;
    }

    public void setAllFields(List<FieldProperty> allFields) {
        this.allFields = allFields;
    }

    public List<FieldPropertyRow> getAllRows() {
        return allRows;
    }
    public void setAllRows(List<FieldPropertyRow> allRows) {
        this.allRows = allRows;
    }

    public Map<String, FieldProperty> getIdToField() {
        return idToField;
    }

    public void setIdToField(Map<String, FieldProperty> idToField) {
        this.idToField = idToField;
    }

    public FieldProperty getField(String id) {
        return idToField.get(id);
    }
    public void appendIdToField(String id, FieldProperty field) {
        idToField.put(id, field);
    }

    public void removeIdFromMap(String id) {
        idToField.remove(id);
    }

    public List<FieldProperty> getSkippableFields() {
        return skippableFields;
    }

    public void setSkippableFields(List<FieldProperty> skippableFields) {
        this.skippableFields = skippableFields;
    }

    public void appendSkippableFields(FieldProperty field) {
        this.skippableFields.add(field);
    }

    public List<FieldProperty> getInactiveFields() {
        return inactiveFields;
    }

    public void setInactiveFields(List<FieldProperty> inactiveFields) {
        this.inactiveFields = inactiveFields;
    }

    public void appendInactiveIds(FieldProperty field) {
        this.inactiveFields.add(field);
    }

    public FieldProperty getRoot() {
        return root;
    }

    public void setRoot(FieldProperty root) {
        this.root = root;
    }

    public List<FieldProperty> getActiveFields() {
        return activeFields;
    }

    public void setActiveFields(List<FieldProperty> activeFields) {
        this.activeFields = activeFields;
    }

    public void appendActiveFields(FieldProperty field) {
        this.activeFields.add(field);
    }

    public void removeFromActiveFields(FieldProperty field) {
        this.activeFields.remove(field);
    }

    public List<FieldProperty> getRootList() {
        return rootList;
    }

    public void setRootList(List<FieldProperty> rootList) {
        this.rootList = rootList;
    }

    public List<String> getDuplicateNames() {
        return duplicateNames;
    }

    public void setDuplicateNames(List<String> duplicateNames) {
        this.duplicateNames = duplicateNames;
    }

    public List<FieldProperty> getAnchorList() {
        return anchorList;
    }

    public void setAnchorList(List<FieldProperty> anchorFields) {
        this.anchorList = anchorFields;
    }

    public FieldProperty getAnchor() {
        return anchor;
    }

    public void setAnchor(FieldProperty anchor) {
        this.anchor = anchor;
    }
}
