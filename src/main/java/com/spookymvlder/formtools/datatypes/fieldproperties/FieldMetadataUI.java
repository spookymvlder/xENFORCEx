package com.spookymvlder.formtools.datatypes.fieldproperties;

public class FieldMetadataUI {
    private String uiControl;
    private String unit;
    private Integer displayOrder;
    private String validationMessage;


    public FieldMetadataUI(String uiControl,
                           String unit,
                           Integer displayOrder,
                           String validationMessage) {
        this.uiControl = uiControl;
        this.unit = unit;
        this.displayOrder = displayOrder;
        this.validationMessage = validationMessage;

    }

    public String getUiControl() {
        return uiControl;
    }

    public String getUnit() {
        return unit;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

}
