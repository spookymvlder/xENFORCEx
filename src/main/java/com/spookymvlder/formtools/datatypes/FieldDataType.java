package com.spookymvlder.formtools.datatypes;

//https://www.w3schools.com/xml/schema_elements_ref.asp
public enum FieldDataType {
    STRING(Boolean.FALSE),
    INTEGER(Boolean.TRUE),
    DECIMAL(Boolean.TRUE),
    DOUBLE(Boolean.TRUE),
    FLOAT(Boolean.TRUE),
    BOOLEAN(Boolean.FALSE),
    DATE(Boolean.FALSE),
    TIME(Boolean.FALSE),
    DATETIME(Boolean.FALSE),
    DURATION(Boolean.FALSE),
    CHARACTER(Boolean.FALSE),
    //
    ANYURI(Boolean.FALSE),                 //URL or relative path?
    IDREF(Boolean.FALSE),                  //Reference to other element
    ENTITY(Boolean.FALSE),                 //Constant to use within document
    ID(Boolean.FALSE),                     //Identifier
    //NOTATION,
    LONG(Boolean.TRUE),
    GYEARMONTH(Boolean.FALSE),
    GMONTHDAY(Boolean.FALSE),
    GDAY(Boolean.FALSE),
    GMONTH(Boolean.FALSE),
    GYEAR(Boolean.FALSE),
    //
    CODED_VALUE(Boolean.FALSE),
    ENUMERATION(Boolean.FALSE),
    LABEL(Boolean.FALSE),
    EMAIL(Boolean.FALSE),
    TELEPHONE(Boolean.FALSE),              //Specify format requirements in settings
    URL(Boolean.FALSE),
    POSTAL_CODE(Boolean.FALSE),            //Specify format requirements in settings
    COUNTRY_CODE(Boolean.FALSE),           //Could be 2 or 3
    CURRENCY(Boolean.FALSE),
    PERCENTAGE(Boolean.TRUE),
    CUSTOM(Boolean.FALSE),
    RAW_XML(Boolean.FALSE),
    ROOT(Boolean.FALSE);

    private final Boolean numeric;

    FieldDataType(Boolean numeric) {
        this.numeric = numeric;
    }

    public boolean isNumeric() {
        return numeric;
    }
}
