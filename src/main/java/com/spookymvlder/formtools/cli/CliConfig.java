package com.spookymvlder.formtools.cli;

import com.spookymvlder.formtools.utilities.ErrorSeverity;

/**
 * A persistable snapshot of the settings a human actually configures — deliberately distinct from
 * the live {@code ImportSettings}/{@code ProcessingOverrides}/{@code ErrorSettings} singletons, which
 * mix in derived runtime state (rootField, specImported) that is never user-set and never persisted.
 * This snapshot is what the settings menu edits (as a "working copy") before an explicit Apply pushes
 * it into the real runtime objects — see {@link ConfigFileManager}.
 */
public class CliConfig {
    private String xmlFilePath;
    private String xsdFilePath;
    private String specFilePath;
    private String rootId;
    private boolean skipXsdValidation;
    private String dateFormat;
    private ErrorSeverity errorThreshold;
    private String outputDirectory;

    public CliConfig() {
        this.skipXsdValidation = false;
        this.dateFormat = "yyyy-MM-dd";
        this.errorThreshold = ErrorSeverity.FATAL;
    }

    public CliConfig copy() {
        CliConfig copy = new CliConfig();
        copy.xmlFilePath = this.xmlFilePath;
        copy.xsdFilePath = this.xsdFilePath;
        copy.specFilePath = this.specFilePath;
        copy.rootId = this.rootId;
        copy.skipXsdValidation = this.skipXsdValidation;
        copy.dateFormat = this.dateFormat;
        copy.errorThreshold = this.errorThreshold;
        copy.outputDirectory = this.outputDirectory;
        return copy;
    }

    public String getXmlFilePath() {
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public String getXsdFilePath() {
        return xsdFilePath;
    }

    public void setXsdFilePath(String xsdFilePath) {
        this.xsdFilePath = xsdFilePath;
    }

    public String getSpecFilePath() {
        return specFilePath;
    }

    public void setSpecFilePath(String specFilePath) {
        this.specFilePath = specFilePath;
    }

    public String getRootId() {
        return rootId;
    }

    public void setRootId(String rootId) {
        this.rootId = rootId;
    }

    public boolean isSkipXsdValidation() {
        return skipXsdValidation;
    }

    public void setSkipXsdValidation(boolean skipXsdValidation) {
        this.skipXsdValidation = skipXsdValidation;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public ErrorSeverity getErrorThreshold() {
        return errorThreshold;
    }

    public void setErrorThreshold(ErrorSeverity errorThreshold) {
        this.errorThreshold = errorThreshold;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }
}
