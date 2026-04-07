package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.GroupingType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FieldRelation {
    private String parentId;
    private String componentOf;
    private GroupingType groupingType;

    private Set<FieldProperty> childFields = new LinkedHashSet<>();
    private FieldProperty parentField = null;

    private List<FieldProperty> constituents = new ArrayList<>();


    public FieldRelation(String parentId, GroupingType groupingType, String componentOf) {
        this.parentId = parentId;
        this.groupingType = groupingType;
        this.componentOf = componentOf;
    }

    public Set<FieldProperty> getChildFields() {
        return childFields;
    }

    public FieldProperty getChildField(String fieldId) {
        for (FieldProperty childField : childFields) {
            if (childField.getFieldId().equals(fieldId)) {
                return childField;
            }
        }
        return null;
    }

    public void setChildFields(Set<FieldProperty> childFields) {
        this.childFields = childFields;
    }

    public void addChildField(FieldProperty childField) {
        this.childFields.add(childField);
    }

    public void removeChildField(FieldProperty childField) {
        this.childFields.remove(childField);
    }

    public FieldProperty getParentField() {
        return parentField;
    }

    public void setParentField(FieldProperty parentField) {
        this.parentField = parentField;
    }

    public List<FieldProperty> getConstituents() {
        return constituents;
    }

    public void setConstituents(List<FieldProperty> constituents) {
        this.constituents = constituents;
    }

    public void addConstituent(FieldProperty constituent) {
        this.constituents.add(constituent);
    }

    public void removeConstituent(FieldProperty constituent) {
        this.constituents.remove(constituent);
    }

    public String getParentId() {
        return parentId;
    }

    public String getComponentOf() {
        return componentOf;
    }

    public GroupingType getGroupingType() {
        return groupingType;
    }
}
