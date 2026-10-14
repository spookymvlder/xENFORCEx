package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;

public class ImportSettings {
    private static ImportSettings instance;
    private String xmlFilePath;
    private String xsdFilePath;
    private String specFilePath;
    //Where the form starts, but not necessarily the beginning of data.
    private String anchorElementName;
    private FieldProperty rootField;
    //Usually 0, what node stripping should start at.
    //Default to firstChildElement. It may be possible that the form root has data,
    private int anchorElementIndex;
    // Renamed from testIgnoreXSD: this is now a permanent, user-facing setting (exposed in the CLI
    // settings menu), not a testing scaffold. Default false: XSD validation runs unless disabled.
    private boolean skipXsdValidation;
    private Boolean specImported;
    private String rootId;
    // Blank/null means "next to the source XML" - see PipelineRunner.commit.
    private String outputDirectory;
    // Gates FormDebugPrinter's full internal field-tree dump in the full pipeline. Off by default:
    // nothing in the CLI's menu or README advertises that output, so it shouldn't run unasked.
    private boolean verbose;

    private ImportSettings() {
        this.xmlFilePath = null;
        this.xsdFilePath = null;
        this.specFilePath = null;
        this.anchorElementName = null;
        this.anchorElementIndex = 0;
        this.skipXsdValidation = false;
        this.specImported = false;
        this.rootField = null;
        this.rootId = null;
        this.outputDirectory = null;
        this.verbose = false;
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

    public String getSpecFilePath() {
        return specFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public void setXsdFilePath(String xsdFilePath) {
        this.xsdFilePath = xsdFilePath;
    }

    public void setSpecFilePath(String specFilePath) {
        this.specFilePath = specFilePath;
    }

    public String getAnchorElementName() {
        return anchorElementName;
    }

    public void setAnchorElementName(String anchorElementName) {
        this.anchorElementName = anchorElementName;
    }

    public int getAnchorElementIndex() {
        return anchorElementIndex;
    }

    public void setAnchorElementIndex(int anchorElementIndex) {
        this.anchorElementIndex = anchorElementIndex;
    }

    public boolean getSkipXsdValidation() {
        return skipXsdValidation;
    }

    public void setSkipXsdValidation(boolean skipXsdValidation) {
        this.skipXsdValidation = skipXsdValidation;
    }

    public Boolean getSpecImported() {
        return specImported;
    }

    public void setSpecImported(Boolean specImported) {
        this.specImported = specImported;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
