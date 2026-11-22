package com.spookymvlder.formtools.datatypes;

import java.util.ArrayList;
import java.util.List;

/**
 * A named code system (e.g. SNOMED CT, ICD-10, or a local vocabulary): identity metadata plus,
 * optionally, its list of valid codes. A CodeSystem may exist as a stub - identity known, codeValues
 * empty - when only its metadata has been entered/imported and no code-values CSV has been loaded yet.
 * This same shape is used both as the on-disk persisted form (see CodeSystemRegistry) and as the
 * hand-authored/importable JSON identity format, so there is one schema for both.
 */
public class CodeSystem {
    private String codeSystemId;
    private String codeSystemName;
    private String codeSystemVersion;
    private List<CodeEntry> codeValues = new ArrayList<>();

    public CodeSystem() {
    }

    public CodeSystem(String codeSystemId, String codeSystemName, String codeSystemVersion) {
        this.codeSystemId = codeSystemId;
        this.codeSystemName = codeSystemName;
        this.codeSystemVersion = codeSystemVersion;
    }

    public String getCodeSystemId() {
        return codeSystemId;
    }

    public void setCodeSystemId(String codeSystemId) {
        this.codeSystemId = codeSystemId;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public void setCodeSystemName(String codeSystemName) {
        this.codeSystemName = codeSystemName;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public void setCodeSystemVersion(String codeSystemVersion) {
        this.codeSystemVersion = codeSystemVersion;
    }

    public List<CodeEntry> getCodeValues() {
        return codeValues;
    }

    public void setCodeValues(List<CodeEntry> codeValues) {
        this.codeValues = codeValues != null ? codeValues : new ArrayList<>();
    }

    public boolean isStub() {
        return codeValues == null || codeValues.isEmpty();
    }

    /** A short label for messages: name if known, otherwise falls back to the bare id. */
    public String displayLabel() {
        return (codeSystemName != null && !codeSystemName.isBlank()) ? codeSystemName : codeSystemId;
    }
}
