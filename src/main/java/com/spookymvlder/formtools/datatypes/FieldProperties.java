package com.spookymvlder.formtools.datatypes;

import com.opencsv.bean.CsvBindAndSplitByName;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvDate;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class FieldProperties {
    //TODO Consider field IDs that may not be numeric, maybe convert to numeric?
    @CsvBindByName (required = true)
    private int fieldId;
    @CsvBindByName (required = true)
    private String fieldName;
    @CsvBindByName
    private String displayName;

    @CsvBindByName
    private int order;
    @CsvBindByName (required = true)
    private int parentId;
    @CsvBindByName
    private int hierarchyLevel;

    @CsvBindByName
    private boolean active;
    //TODO figure out how to modularize
    @CsvBindByName
    @CsvDate("yyyy-MM-dd")
    private Date expireDate;

    @CsvBindByName
    private char fieldRepeated;         //T, F, C
    @CsvBindByName
    private int minOccurs;
    @CsvBindByName
    private int maxOccurs;

    //Code system data type attributes
    @CsvBindByName
    private String codeSystemName;
    @CsvBindByName
    private String codeSystemId;
    @CsvBindByName
    private String codeSystemVersion;

    @CsvBindByName  (required = true)
    private boolean required;
    //TODO import separate conditionally mandatory logic if complicated.
    @CsvBindAndSplitByName(elementType=int.class, splitOn = ",")
    private int[] condMandatorySourceIds;
    @CsvBindAndSplitByName(elementType=String.class, splitOn = "|")
    private List<String[]> condMandatoryValues;
    @CsvBindByName
    private LinkLogic condMandatoryLogic;

    //Allowed values for field
    @CsvBindByName (required = true)
    private String dataType;
    @CsvBindByName
    private int rangeStart;
    @CsvBindByName
    private int rangeEnd;
    @CsvBindByName
    private boolean rangeInclusive;
    @CsvBindByName
    private int minLength;
    @CsvBindByName
    private int maxLength;
    @CsvBindByName
    private List<String> enumerations;
    @CsvBindByName
    private String unit;
    @CsvBindByName
    private String regexPattern;

    @CsvBindByName
    private String xpathOverride;
    @CsvBindByName
    private boolean isAttribute;
    @CsvBindByName
    private String attributeName;

    @CsvBindByName
    private String uiControl;
    @CsvBindByName
    private String validationMessage;
    @CsvBindByName
    private boolean skipImport;

    public FieldProperties createFieldProperties(String[] csvLine) {

    }

}
