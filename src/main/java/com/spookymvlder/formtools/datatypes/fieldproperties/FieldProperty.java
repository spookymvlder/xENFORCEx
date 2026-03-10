package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;



public class FieldProperty {
    private FieldIdentity identity;
    private FieldRelation relation;
    private FieldCardinality cardinality;
    private FieldRequirement requirement;
    private FieldValueRestriction value;
    private FieldCodeSpec codeSpec;
    private FieldMetadataUI metadataUI;

    public FieldProperty(FieldIdentity identity,
                         FieldRelation relation,
                         FieldCardinality cardinality,
                         FieldRequirement requirement,
                         FieldValueRestriction value,
                         FieldCodeSpec codeSpec,
                         FieldMetadataUI metadataUI) {
        this.identity = identity;
        this.relation = relation;
        this.cardinality = cardinality;
        this.requirement = requirement;
        this.value = value;
        this.codeSpec = codeSpec;
        this.metadataUI = metadataUI;
    }

    public FieldIdentity getIdentity() {
        return identity;
    }

    public FieldRelation getRelation() {
        return relation;
    }

    public FieldCardinality getCardinality() {
        return cardinality;
    }



    public FieldValueRestriction getValue() {
        return value;
    }

    public String getFieldId() {
        return this.identity.getFieldId();
    }

    public String getFieldName() {
        return this.getIdentity().getFieldName();
    }


    // FIELD IDENTIFIER COLUMNS

    private String fieldId;
    //TODO error checking if the fieldName is a duplicate for something at the same hierarchy level and is not repeating.
    private String fieldName;
    private String displayName;



    // FIELD HIERARCHY COLUMNS
    private String parentId;





    // FIELD VALUE RESTRICTION COLUMNS
    private String dataType;


    // ATTRIBUTE INFORMATION COLUMNS
    //TODO confirm this works and doesn't need language set somewhere.
    private Boolean isAttribute;
    private String attributeName;




    //TODO namespace support

    private Set<FieldProperty> childFields = new HashSet<>();
    private FieldProperty parentField = null;
    // TODO make it clear that import spec fields get normalized, fields like this do not.
    private String templatePath = null;
    private List<FieldProperty> constituents = new ArrayList<>();


    public Set<FieldProperty> getChildFields() {
        return childFields;
    }





    public void addChildField(FieldProperty childField) {
        this.childFields.add(childField);
    }

    public void removeChildField(FieldProperty childField) {
        this.childFields.remove(childField);
    }

    public void removeFieldFromTree() {
        if (this.parentId.equalsIgnoreCase("root")) {
            ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_TREE_RESTRUCTURING, "Attempting to remove root node field.", ErrorSeverity.FATAL, fieldId, fieldName));
        } else {
            for (FieldProperty childField : this.childFields) {
                childField.setParentField(this.parentField);
                this.parentField.addChildField(childField);
            }
            this.parentField.removeChildField(this);
        }
    }



    public FieldProperty getParentField() {
        return parentField;
    }

    public void setParentField(FieldProperty parentField) {
        this.parentField = parentField;
    }

    //TODO move this to FieldRelation, need a way to reference self.
    public void createConnections(FieldProperty parentField) {
        this.parentField = parentField;
        parentField.addChildField(this);
    }





    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }



    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getDataType() {
        return dataType;
    }

    public Boolean getAttribute() {
        return Boolean.TRUE.equals(this.isAttribute);
    }

    public String getAttributeName() {
        return attributeName;
    }








    public String getTemplatePath() {
        //TODO come back to this
//        if (relativePath == null) {
//            relativePath = buildRelativePath();
//        }
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public List<FieldProperty> getConstituents() {
        return constituents;
    }



}
