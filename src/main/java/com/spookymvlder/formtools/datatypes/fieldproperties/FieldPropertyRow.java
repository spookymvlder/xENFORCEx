package com.spookymvlder.formtools.datatypes.fieldproperties;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.bean.processor.ConvertEmptyOrBlankStringsToDefault;
import com.opencsv.bean.processor.PreAssignmentProcessor;
import com.spookymvlder.formtools.processsor.FieldPropertyConverter;

import java.time.LocalDate;
import java.util.List;

// Represents OpenCSV import row
public class FieldPropertyRow {
    // FIELD IDENTIFIER COLUMNS
    //TODO Consider field IDs that may not be numeric, maybe convert to numeric?
    @CsvBindByName(column = "fieldId", required = true)
    private String fieldId;
    //TODO error checking if the fieldName is a duplicate for something at the same hierarchy level and is not repeating.
    @CsvBindByName (required = true)
    private String fieldName;
    @CsvBindByName
    private String displayName;



    // FIELD HIERARCHY COLUMNS
    @CsvBindByName (required = false)
    private String parentId;

    // VERSIONING COLUMNS
    @CsvCustomBindByName(column="active", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean active;
    //TODO figure out how to modularize
    @CsvCustomBindByName (column="expireDate", converter = FieldPropertyConverter.CustomDateConverter.class)
    private LocalDate expireDate;
    //TODO make active date to make updates easier.

    // CARDINALITY COLUMNS
    @CsvCustomBindByName(column="fieldRepeated", converter = FieldPropertyConverter.RepeatConverter.class)
    private Character fieldRepeated;         //T, F, C
    @CsvBindByName
    private Integer minOccurs;
    @CsvBindByName
    private Integer maxOccurs;
    @CsvBindAndSplitByName(elementType=String.class, splitOn = ",")
    private List<String> condRepeatedSourceIds;
    //TODO update logic here to split list level here and condRequiredValues on curly braces and array level via pipe
    @CsvBindAndSplitByName(elementType=String.class, splitOn = "\\|")
    private List<String> condRepeatedValues;
    private List<String[]> processedCondRepeatedValues;

    @CsvBindByName
    private String condRepeatedLogic;

    // REQUIRED FIELD COLUMNS
    //TODO change to Char or enum, or process later
    @CsvCustomBindByName(column="required", converter = FieldPropertyConverter.RepeatConverter.class)
    private Character required;

    //TODO import separate conditionally mandatory logic if complicated.
    @CsvBindAndSplitByName(elementType=String.class, splitOn = ",")
    private List<String> condRequiredSourceIds;

    @CsvBindAndSplitByName(elementType=String.class, splitOn = "\\|")
    private List<String> condRequiredValues;


    @CsvBindByName
    private String condRequiredLogic;

    // CODE SYSTEM METADATA COLUMNS
    @CsvBindByName
    private String codeSystemName;
    @CsvBindByName
    private String codeSystemId;
    @CsvBindByName
    private String codeSystemVersion;
    @CsvBindByName
    private String codeSystemCode;

    // FIELD VALUE RESTRICTION COLUMNS
    @CsvBindByName (column = "dataType", required = false)
    private String dataType;
    @CsvBindByName
    private String rangeStart;
    @CsvBindByName
    private String rangeEnd;
    @CsvCustomBindByName (column="rangeStartInclusive", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean rangeStartInclusive;
    @CsvCustomBindByName (column="rangeEndInclusive", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean rangeEndInclusive;
    @CsvBindByName
    private Integer minLength;
    @CsvBindByName
    private Integer maxLength;
    @CsvBindByName
    private List<String> enumerations;

    @CsvBindByName
    private String regexPattern;

    // ATTRIBUTE INFORMATION COLUMNS
    //TODO confirm this works and doesn't need language set somewhere.
    @CsvCustomBindByName (column="isAttribute", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean isAttribute;
    @CsvBindByName
    private String attributeName;

    // UI METADATA/CONTROL COLUMNS
    @CsvBindByName
    private String uiControl;
    @CsvBindByName
    private String unit;
    @CsvBindByName
    private Integer displayOrder;
    @CsvBindByName
    private String validationMessage;

    // IMPORT BEHAVIOR COLUMNS
    @CsvCustomBindByName (column="skipImport", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean skipImport;

    @CsvBindByName(column = "groupingType")
    @PreAssignmentProcessor(processor = ConvertEmptyOrBlankStringsToDefault.class, paramString = "NONE")
    private String groupingType;



    @CsvBindByName
    private String componentOf;

    //TODO namespace support


    public String getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getParentId() {
        return parentId;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public Character getFieldRepeated() {
        return fieldRepeated;
    }

    public Integer getMinOccurs() {
        return minOccurs;
    }

    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public List<String> getCondRepeatedSourceIds() {
        return condRepeatedSourceIds;
    }

    public List<String> getCondRepeatedValues() {
        return condRepeatedValues;
    }

    public List<String[]> getProcessedCondRepeatedValues() {
        return processedCondRepeatedValues;
    }

    public String getCondRepeatedLogic() {
        return condRepeatedLogic;
    }

    public Character getRequired() {
        return required;
    }

    public List<String> getCondRequiredSourceIds() {
        return condRequiredSourceIds;
    }

    public List<String> getCondRequiredValues() {
        return condRequiredValues;
    }

    public String getCondRequiredLogic() {
        return condRequiredLogic;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public String getCodeSystemId() {
        return codeSystemId;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public String getCodeSystemCode() {
        return codeSystemCode;
    }

    public String getDataType() {
        return dataType;
    }

    public String getRangeStart() {
        return rangeStart;
    }

    public String getRangeEnd() {
        return rangeEnd;
    }

    public Boolean getRangeStartInclusive() {
        return rangeStartInclusive;
    }

    public Boolean getRangeEndInclusive() {
        return rangeEndInclusive;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public List<String> getEnumerations() {
        return enumerations;
    }

    public String getRegexPattern() {
        return regexPattern;
    }

    public Boolean getAttribute() {
        return isAttribute;
    }

    public String getAttributeName() {
        return attributeName;
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

    public Boolean getSkipImport() {
        return skipImport;
    }

    public String getGroupingType() {
        return groupingType;
    }

    public String getComponentOf() {
        return componentOf;
    }
}
