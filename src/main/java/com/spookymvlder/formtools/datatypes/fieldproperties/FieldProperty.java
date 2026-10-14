package com.spookymvlder.formtools.datatypes.fieldproperties;



public class FieldProperty {
    private FieldIdentity identity;
    private FieldRelation relation;
    private FieldCardinality cardinality;

    private FieldValueRestriction value;
    private FieldCodeSpec codeSpec;
    private FieldMetadataUI metadataUI;

    public FieldProperty(FieldIdentity identity,
                         FieldRelation relation,
                         FieldCardinality cardinality,
                         FieldValueRestriction value,
                         FieldCodeSpec codeSpec,
                         FieldMetadataUI metadataUI) {
        this.identity = identity;
        this.relation = relation;
        this.cardinality = cardinality;
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

    public FieldValueRestriction getValueRestriction() {
        return value;
    }


    public FieldCodeSpec getCodeSpec() {
        return codeSpec;
    }

    public FieldMetadataUI getMetadataUI() {
        return metadataUI;
    }

    // Keeping some commonly used getters.
    public String getFieldId() {
        return this.identity.getFieldId();
    }

    public String getFieldName() {
        return this.getIdentity().getFieldName();
    }

    //TODO move this to FieldRelation, need a way to reference self.
    public void createConnections(FieldProperty parentField) {
        this.relation.setParentField(parentField);
        parentField.getRelation().addChildField(this);
    }

}
