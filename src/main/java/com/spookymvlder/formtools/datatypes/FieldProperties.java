package com.spookymvlder.formtools.datatypes;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ValidationError;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FieldProperties {
    //TODO Consider field IDs that may not be numeric, maybe convert to numeric?
    @CsvBindByName (required = true)
    private Integer fieldId;
    @CsvBindByName (required = true)
    private String fieldName;
    @CsvBindByName
    private String displayName;

    @CsvBindByName
    private Integer order;
    @CsvBindByName (required = true)
    private Integer parentId;
    @CsvBindByName
    private Integer hierarchyLevel;

    @CsvBindByName
    private Boolean active;
    //TODO figure out how to modularize
    @CsvBindByName
    @CsvDate("yyyy-MM-dd")
    private Date expireDate;

    @CsvCustomBindByName(column="fieldRepeated", converter = FieldRepeatConverter.class)
    private Character fieldRepeated;         //T, F, C
    @CsvBindByName
    private Integer minOccurs;
    @CsvBindByName
    private Integer maxOccurs;

    //Code system data type attributes
    @CsvBindByName
    private String codeSystemName;
    @CsvBindByName
    private String codeSystemId;
    @CsvBindByName
    private String codeSystemVersion;

    @CsvBindByName  (required = true)
    private Boolean required;
    //TODO import separate conditionally mandatory logic if complicated.
    @CsvBindAndSplitByName(elementType=Integer.class, splitOn = ",")
    private Integer[] condMandatorySourceIds;
    @CsvBindAndSplitByName(elementType=String.class, splitOn = "|")
    private List<String[]> condMandatoryValues;
    @CsvBindByName
    private LinkLogic condMandatoryLogic;

    //Allowed values for field
    @CsvBindByName (required = true)
    private String dataType;
    @CsvBindByName
    private Double rangeStart;
    @CsvBindByName
    private Double rangeEnd;
    @CsvBindByName
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
    @CsvBindByName
    private Boolean isAttribute;
    @CsvBindByName
    private String attributeName;

    @CsvBindByName
    private String uiControl;
    @CsvBindByName
    private String validationMessage;
    @CsvBindByName
    private Boolean skipImport;


    public Integer getFieldId() {
        return fieldId;
    }

    public void setFieldId(Integer fieldId) {
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

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getHierarchyLevel() {
        return hierarchyLevel;
    }

    public void setHierarchyLevel(Integer hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
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

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public Integer[] getCondMandatorySourceIds() {
        return condMandatorySourceIds;
    }

    public void setCondMandatorySourceIds(Integer[] condMandatorySourceIds) {
        this.condMandatorySourceIds = condMandatorySourceIds;
    }

    public List<String[]> getCondMandatoryValues() {
        return condMandatoryValues;
    }

    public void setCondMandatoryValues(List<String[]> condMandatoryValues) {
        this.condMandatoryValues = condMandatoryValues;
    }

    public LinkLogic getCondMandatoryLogic() {
        return condMandatoryLogic;
    }

    public void setCondMandatoryLogic(LinkLogic condMandatoryLogic) {
        this.condMandatoryLogic = condMandatoryLogic;
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

    public List<ValidationError> validateWithErrors()  {
        List<ValidationError> errors = new ArrayList<>();
        errors.addAll(this.validateAttributes());
        errors.addAll(this.validateRanges());

        return errors;
    }

    public List<ValidationError> validateAttributes() {
        List<ValidationError> errors = new ArrayList<>();
        //Unless default to no or assume if attribute name is populated it is an attribute?
        if (isAttribute == null && attributeName != null) {
            errors.add(new ValidationError(this.fieldId, this.fieldName, "isAttribute is required when attributeName is set.", ErrorSeverity.WARNING));
        }
        if (attributeName == null && isAttribute != null) {
            errors.add(new ValidationError(this.fieldId, this.fieldName, "attributeName is required when isAttribute is set.", ErrorSeverity.ERROR));
        }
        return errors;
    }


    public List<ValidationError> validateRanges() {
        List<ValidationError> errors = new ArrayList<>();
        boolean ranges = (this.rangeStart != null) || (this.rangeEnd != null);
        if (ranges && this.rangeInclusive == null) {
            errors.add(new ValidationError(this.fieldId, this.fieldName, "rangeInclusive is required if rangeStart or rangeEnd are set.", ErrorSeverity.ERROR));
        }

        if(this.rangeStart != null && this.rangeEnd != null) {
            if (this.rangeStart > this.rangeEnd) {
                errors.add(new ValidationError(this.fieldId, this.fieldName, "rangeStart must be less than or equal to rangeEnd.", ErrorSeverity.ERROR));
            }
            if (this.rangeStart.equals(this.rangeEnd) && Boolean.FALSE.equals(this.rangeInclusive)) {
                errors.add(new ValidationError(this.fieldId, this.fieldName, "No valid values for ranges.", ErrorSeverity.ERROR));
            }
        }
        return errors;
    }

    public static class FieldRepeatConverter extends AbstractBeanField<Character,String> {
        @Override
        protected Character convert(String value) throws CsvDataTypeMismatchException {
            if (!value.trim().toUpperCase().matches("[TFC]")) {
                throw new CsvDataTypeMismatchException("Invalid repeat flag: " + value);
            }
            return value.charAt(0);
        }
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
