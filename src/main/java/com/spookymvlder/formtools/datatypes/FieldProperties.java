package com.spookymvlder.formtools.datatypes;

import com.opencsv.bean.*;
import com.opencsv.bean.customconverter.ConverterLanguageToBoolean;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.FieldPropertyConverter;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FieldProperties {
    //TODO Consider field IDs that may not be numeric, maybe convert to numeric?
    @CsvBindByName (required = true)
    private String fieldId;
    @CsvBindByName (required = true)
    private String fieldName;
    @CsvBindByName
    private String displayName;

    @CsvBindByName
    private Integer order;
    @CsvBindByName (required = true)
    private String parentId;
    @CsvBindByName
    private Integer hierarchyLevel;
    @CsvBindByName
    private String sectionHeaderOverride;

    @CsvBindByName
    private Boolean active;
    //TODO figure out how to modularize
    @CsvCustomBindByName (column="expireDate", converter = FieldPropertyConverter.CustomDateConverter.class)
    private LocalDate expireDate;

    @CsvCustomBindByName(column="fieldRepeated", converter = FieldPropertyConverter.RepeatConverter.class)
    private Character fieldRepeated;         //T, F, C
    @CsvBindByName
    private Integer minOccurs;
    @CsvBindByName
    private Integer maxOccurs;
    @CsvBindAndSplitByName(elementType=String.class, splitOn = ",")
    private String[] condRepeatedSourceIds;
    //TODO update logic here to split list level here and condRequiredValues on curly braces and array level via pipe
    @CsvBindAndSplitByName(elementType=String.class, splitOn = "|")
    private List<String[]> condRepeatedValues;
    @CsvBindByName
    private LinkLogic condRepeatedLogic;



    @CsvCustomBindByName(column="required", converter = FieldPropertyConverter.RepeatConverter.class)
    private Boolean required;
    //TODO import separate conditionally mandatory logic if complicated.
    @CsvBindAndSplitByName(elementType=String.class, splitOn = ",")
    private String[] condRequiredSourceIds;
    @CsvBindAndSplitByName(elementType=String.class, splitOn = "|")
    private List<String[]> condRequiredValues;
    @CsvBindByName
    private LinkLogic condRequiredLogic;

    //Code system data type attributes
    @CsvBindByName
    private String codeSystemName;
    @CsvBindByName
    private String codeSystemId;
    @CsvBindByName
    private String codeSystemVersion;
    @CsvBindByName
    private String codeSystemCode;

    //Allowed values for field
    @CsvBindByName (required = true)
    private String dataType;
    @CsvBindByName
    private Double rangeStart;
    @CsvBindByName
    private Double rangeEnd;
    @CsvCustomBindByName (column="rangeInclusive", converter = ConverterLanguageToBoolean.class)
    private Boolean rangeInclusive;
    @CsvBindByName
    private Integer minLength;
    @CsvBindByName
    private Integer maxLength;
    @CsvBindByName
    private List<String> enumerations;
    @CsvBindByName
    private String unit;
    @CsvBindByName
    private String regexPattern;

    @CsvBindByName
    private String xpathOverride;
    //TODO confirm this works and doesn't need language set somewhere.
    @CsvCustomBindByName (column="isAttribute", converter = ConverterLanguageToBoolean.class)
    private Boolean isAttribute;
    @CsvBindByName
    private String attributeName;

    @CsvBindByName
    private String uiControl;
    @CsvBindByName
    private String validationMessage;
    @CsvCustomBindByName (column="skipImport", converter = ConverterLanguageToBoolean.class)
    private Boolean skipImport;

    private Set<FieldProperties> childFields = new HashSet<>();
    private FieldProperties parentField = null;

    public Set<FieldProperties> getChildFields() {
        return childFields;
    }

    public void setChildFields(Set<FieldProperties> childFields) {
        this.childFields = childFields;
    }

    public void appendChildField(FieldProperties childField) {
        this.childFields.add(childField);
    }

    public void removeFieldFromTree() {
        if (this.parentId.equalsIgnoreCase("root")) {
            ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_TREE_RESTRUCTURING, "Attempting to remove root node field.", ErrorSeverity.FATAL, fieldId, fieldName));
        } else {
            if (!this.childFields.isEmpty()) {
                for (FieldProperties childField : this.childFields) {
                    childField.setParentField(this.parentField);
                    this.parentField.appendChildField(childField);
                }
            }
            this.parentField.removeChildField(this);

        }
    }

    public void removeChildField(FieldProperties childField) {
        this.childFields.remove(childField);
    }

    public FieldProperties getParentField() {
        return parentField;
    }

    public void setParentField(FieldProperties parentField) {
        this.parentField = parentField;
    }

    public void createConnections(FieldProperties parentField) {
        this.parentField = parentField;
        parentField.appendChildField(this);
    }

    public String getFieldId() {
        return fieldId;
    }

    public void setFieldId(String fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public String getSectionHeaderOverride() {
        return sectionHeaderOverride;
    }

    public void setSectionHeaderOverride(String sectionHeaderOverride) {
        this.sectionHeaderOverride = sectionHeaderOverride;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    public Character getFieldRepeated() {
        return fieldRepeated;
    }

    public void setFieldRepeated(Character fieldRepeated) {
        this.fieldRepeated = fieldRepeated;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public void setMinOccurs(Integer minOccurs) {
        this.minOccurs = minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public String[] getCondRepeatedSourceIds() {
        return condRepeatedSourceIds;
    }

    public void setCondRepeatedSourceIds(String[] condRepeatedSourceIds) {
        this.condRepeatedSourceIds = condRepeatedSourceIds;
    }

    public List<String[]> getCondRepeatedValues() {
        return condRepeatedValues;
    }

    public void setCondRepeatedValues(List<String[]> condRepeatedValues) {
        this.condRepeatedValues = condRepeatedValues;
    }

    public LinkLogic getCondRepeatedLogic() {
        return condRepeatedLogic;
    }

    public void setCondRepeatedLogic(LinkLogic condRepeatedLogic) {
        this.condRepeatedLogic = condRepeatedLogic;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getCodeSystemId() {
        return codeSystemId;
    }

    public void setCodeSystemId(String codeSystemId) {
        this.codeSystemId = codeSystemId;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public void setCodeSystemVersion(String codeSystemVersion) {
        this.codeSystemVersion = codeSystemVersion;
    }

    public String getCodeSystemCode() {
        return codeSystemCode;
    }

    public void setCodeSystemCode(String codeSystemCode) {
        this.codeSystemCode = codeSystemCode;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public String[] getCondRequiredSourceIds() {
        return condRequiredSourceIds;
    }

    public void setCondRequiredSourceIds(String[] condRequiredSourceIds) {
        this.condRequiredSourceIds = condRequiredSourceIds;
    }

    public List<String[]> getCondRequiredValues() {
        return condRequiredValues;
    }

    public void setCondRequiredValues(List<String[]> condRequiredValues) {
        this.condRequiredValues = condRequiredValues;
    }

    public LinkLogic getCondRequiredLogic() {
        return condRequiredLogic;
    }

    public void setCondRequiredLogic(LinkLogic condRequiredLogic) {
        this.condRequiredLogic = condRequiredLogic;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Double getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(Double rangeStart) {
        this.rangeStart = rangeStart;
    }

    public Double getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(Double rangeEnd) {
        this.rangeEnd = rangeEnd;
    }

    public Boolean getRangeInclusive() {
        return rangeInclusive;
    }

    public void setRangeInclusive(Boolean rangeInclusive) {
        this.rangeInclusive = rangeInclusive;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public List<String> getEnumerations() {
        return enumerations;
    }

    public void setEnumerations(List<String> enumerations) {
        this.enumerations = enumerations;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public void setRegexPattern(String regexPattern) {
        this.regexPattern = regexPattern;
    }

    public String getXpathOverride() {
        return xpathOverride;
    }

    public void setXpathOverride(String xpathOverride) {
        this.xpathOverride = xpathOverride;
    }

    public Boolean getAttribute() {
        return isAttribute;
    }

    public void setAttribute(Boolean attribute) {
        isAttribute = attribute;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getUiControl() {
        return uiControl;
    }

    public void setUiControl(String uiControl) {
        this.uiControl = uiControl;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public void setValidationMessage(String validationMessage) {
        this.validationMessage = validationMessage;
    }

    public Boolean getSkipImport() {
        return skipImport;
    }

    public void setSkipImport(Boolean skipImport) {
        this.skipImport = skipImport;
    }




/*
    public static class FieldPropertyBuilder {
        private int fieldId;
        private String fieldName;
        private String displayName;

        private int order;
        private int parentId;
        private int hierarchyLevel;

        private boolean active;
        private Date expireDate;

        private char fieldRepeated;         //T, F, C
        private int minOccurs;
        private int maxOccurs;

        //Code system data type attributes
        private String codeSystemName;
        private String codeSystemId;
        private String codeSystemVersion;

        private boolean required;
        private int[] condMandatorySourceIds;
        private List<String[]> condMandatoryValues;
        private LinkLogic condMandatoryLogic;

        //Allowed values for field
        private String dataType;
        private int rangeStart;
        private int rangeEnd;
        private boolean rangeInclusive;
        private int minLength;
        private int maxLength;
        private List<String> enumerations;
        private String unit;
        private String regexPattern;


        private String xpathOverride;
        private boolean isAttribute;
        private String attributeName;


        private String uiControl;
        private String validationMessage;
        private boolean skipImport;
        
        public static FieldPropertyBuilder newBuilder() {
            return new FieldPropertyBuilder();
        }

        public void setFieldId(int fieldId) {
            this.fieldId = fieldId;
        }
        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        public void setOrder(int order) {
            this.order = order;
        }
        public void setParentId(int parentId) {
            this.parentId = parentId;
        }
        public void setHierarchyLevel(int hierarchyLevel) {
            this.hierarchyLevel = hierarchyLevel;
        }
        public void setActive(boolean active) {
            this.active = active;
        }
        public void setExpireDate(Date expireDate) {
            this.expireDate = expireDate;
        }
        public void setFieldRepeated(char fieldRepeated) {
            this.fieldRepeated = fieldRepeated;
        }
        public void setMinOccurs(int minOccurs) {
            this.minOccurs = minOccurs;
        }
        public void setMaxOccurs(int maxOccurs) {
            this.maxOccurs = maxOccurs;
        }
        public void setCodeSystemName(String codeSystemName) {
            this.codeSystemName = codeSystemName;
        }
        public void setCodeSystemId(String codeSystemId) {
            this.codeSystemId = codeSystemId;
        }
        public void setCodeSystemVersion(String codeSystemVersion) {
            this.codeSystemVersion = codeSystemVersion;
        }
        public void setRequired(boolean required) {
            this.required = required;
        }
        public void setCondMandatorySourceIds(int[] condMandatorySourceIds) {
            this.condMandatorySourceIds = condMandatorySourceIds;
        }
        public void setCondMandatoryValues(List<String[]> condMandatoryValues) {
            this.condMandatoryValues = condMandatoryValues;
        }
        public void setCondMandatoryLogic(LinkLogic condMandatoryLogic) {
            this.condMandatoryLogic = condMandatoryLogic;
        }
        public void setDataType(String dataType) {
            this.dataType = dataType;
        }
        public void setRangeStart(int rangeStart) {
            this.rangeStart = rangeStart;
        }
        public void setRangeEnd(int rangeEnd) {
            this.rangeEnd = rangeEnd;
        }
        public void setRangeInclusive(boolean rangeInclusive) {
            this.rangeInclusive = rangeInclusive;
        }
        public void setMinLength(int minLength) {
            this.minLength = minLength;
        }
        public void setMaxLength(int maxLength) {
            this.maxLength = maxLength;
        }
        public void setEnumerations(List<String> enumerations) {
            this.enumerations = enumerations;
        }
        public void setUnit(String unit) {
            this.unit = unit;
        }
        public void setRegexPattern(String regexPattern) {
            this.regexPattern = regexPattern;
        }
        public void setXpathOverride(String xpathOverride) {
            this.xpathOverride = xpathOverride;
        }
        public void setIsAttribute(boolean isAttribute) {
            this.isAttribute = isAttribute;
        }
        public void setAttributeName(String attributeName) {
            this.attributeName = attributeName;
        }
        public void setUiControl(String uiControl) {
            this.uiControl = uiControl;
        }
        public void setValidationMessage(String validationMessage) {
            this.validationMessage = validationMessage;
        }
        public void setSkipImport(boolean skipImport) {
            this.skipImport = skipImport;
        }
        public FieldProperties build() {
            return new FieldProperties(this);
        }
    }
*/

}
