package com.spookymvlder.formtools.datatypes;

import java.util.ArrayList;
import java.util.List;

public class CodeSystem {
    private String codeSystemName;
    private String codeSystemVersion;
    private String codeSystemDescription;
    private String codeSystemIdAttribute;           //Code system identifier
    private String codeSystemCodeAttribute;         //Field identifier
    private String codeSystemPublisher;
    //TODO make generic
    private List<String> codeValues = new ArrayList<>();

    public CodeSystem(String codeSystemName, String codeSystemVersion, String codeSystemIdAttribute) {
        this.codeSystemName = codeSystemName;
        this.codeSystemVersion = codeSystemVersion;
        this.codeSystemIdAttribute = codeSystemIdAttribute;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public String getCodeSystemDescription() {
        return codeSystemDescription;
    }

    public void setCodeSystemDescription(String codeSystemDescription) {
        this.codeSystemDescription = codeSystemDescription;
    }

    public String getCodeSystemCodeAttribute() {
        return codeSystemCodeAttribute;
    }

    public void setCodeSystemCodeAttribute(String codeSystemCodeAttribute) {
        this.codeSystemCodeAttribute = codeSystemCodeAttribute;
    }

    public String getCodeSystemIdAttribute() {
        return codeSystemIdAttribute;
    }

    public void setCodeSystemIdAttribute(String codeSystemIdAttribute) {
        this.codeSystemIdAttribute = codeSystemIdAttribute;
    }

    public String getCodeSystemPublisher() {
        return codeSystemPublisher;
    }

    public void setCodeSystemPublisher(String codeSystemPublisher) {
        this.codeSystemPublisher = codeSystemPublisher;
    }

    public List<String> getCodeValues() {
        return codeValues;
    }

    public void setCodeValues(List<String> codeValues) {
        this.codeValues = codeValues;
    }

    public void addCodeValue(String codeValue) {
        this.codeValues.add(codeValue);
    }
}
