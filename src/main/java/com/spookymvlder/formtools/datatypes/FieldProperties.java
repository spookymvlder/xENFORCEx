package com.spookymvlder.formtools.datatypes;

import java.time.LocalDate;
import java.util.List;

public class FieldProperties {


    private int fieldId;
    private String fieldName;
    private String displayName;

    private int order;
    private int parentId;
    private int hierarchyLevel;

    private boolean active;
    private LocalDate expireDate;


    private char fieldRepeated;         //T, F, C
    private int minOccurs;
    private int maxOccurs;

    //Code system data type attributes
    private String codeSystemName;
    private String codeSystemId;
    private String codeSystemVersion;

    private boolean required;
    //TODO import separate conditionally mandatory logic if complicated.
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

    public FieldProperties createFieldProperties(String[] csvLine) {

    }

}
