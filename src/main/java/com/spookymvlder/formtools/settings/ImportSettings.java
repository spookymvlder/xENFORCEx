package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.FieldProperty;

public class ImportSettings {
    private static ImportSettings instance;
    private String xmlFilePath;
    private String xsdFilePath;
    //Where the form starts, but not necessarily the beginning of data.
    private String formRoot;
    private FieldProperty rootField;
    //Usually 0, what node stripping should start at.
    //Default to firstChildElement. It may be possible that the form root has data,
    private int formRootIndex;
    private boolean testIgnoreXSD;
    private Boolean specImported;

    private ImportSettings() {
        this.xmlFilePath = null;
        this.xsdFilePath = null;
        this.formRoot = null;
        this.formRootIndex = 0;
        this.testIgnoreXSD = false;
        this.specImported = false;
        this.rootField = null;
    }

    public static ImportSettings getInstance() {
        if (instance == null) {
            instance = new ImportSettings();
        }
        return instance;
    }

    public FieldProperty getRootField() {
        return rootField;
    }

    public void setRootField(FieldProperty rootField) {
        this.rootField = rootField;
    }

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public String getXsdFilePath() {
        return xsdFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public void setXsdFilePath(String xsdFilePath) {
        this.xsdFilePath = xsdFilePath;
    }

    public String getFormRoot() {
        return formRoot;
    }

    public void setFormRoot(String formRoot) {
        this.formRoot = formRoot;
    }

    public int getFormRootIndex() {
        return formRootIndex;
    }

    public void setFormRootIndex(int formRootIndex) {
        this.formRootIndex = formRootIndex;
    }

    public boolean getTestIgnoreXSD() {
        return testIgnoreXSD;
    }

    public void setTestIgnoreXSD(boolean testIgnoreXSD) {
        this.testIgnoreXSD = testIgnoreXSD;
    }

    public Boolean getSpecImported() {
        return specImported;
    }

    public void setSpecImported(Boolean specImported) {
        this.specImported = specImported;
    }
}
