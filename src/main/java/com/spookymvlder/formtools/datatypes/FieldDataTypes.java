package com.spookymvlder.formtools.datatypes;

//https://www.w3schools.com/xml/schema_elements_ref.asp
public enum FieldDataTypes {
    STRING,
    INTEGER,
    DECIMAL,
    DOUBLE,
    FLOAT,
    BOOLEAN,
    DATE,
    TIME,
    DATETIME,
    DURATION,
    CHARACTER,
    //
    ANYURI,                 //URL or relative path?
    IDREF,                  //Reference to other element
    ENTITY,                 //Constant to use within document
    ID,                     //Identifier
    //NOTATION,
    LONG,
    GYEARMONTH,
    GMONTHDAY,
    GDAY,
    GMONTH,
    GYEAR,
    //
    CODED_VALUE,
    ENUMERATION,
    LABEL,
    EMAIL,
    TELEPHONE,              //Specify format requirements in settings
    URL,
    POSTAL_CODE,            //Specify format requirements in settings
    COUNTRY_CODE,           //Could be 2 or 3
    CURRENCY,
    PERCENTAGE,
    CUSTOM,
    RAW_XML,
    ROOT
}
