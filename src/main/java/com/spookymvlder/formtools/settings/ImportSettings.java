package com.spookymvlder.formtools.settings;

public class ImportSettings {
    private static ImportSettings instance;
    private String xmlFilePath;
    private String xsdFilePath;
    //Where the form starts, but not necessarily the beginning of data.
    private String formRoot;
    //Usually 0, what node stripping should start at.
    //Default to firstChildElement. It may be possible that the form root has data,
    private int formRootIndex;

    private ImportSettings() {
        this.xmlFilePath = null;
        this.xsdFilePath = null;
        this.formRoot = null;
        this.formRootIndex = 0;
    }

    public static ImportSettings getInstance() {
        if (instance == null) {
            instance = new ImportSettings();
        }
        return instance;
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
}
