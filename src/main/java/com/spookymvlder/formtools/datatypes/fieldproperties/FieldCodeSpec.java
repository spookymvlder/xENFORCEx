package com.spookymvlder.formtools.datatypes.fieldproperties;

import java.util.List;

public class FieldCodeSpec {
    private String codeSystemName;
    private String codeSystemId;
    private String codeSystemVersion;
    // The field's own code constraint (literal/range/prefix tokens - see CodeConstraintMatcher),
    // not the field's identity within the code system. Checked unconditionally whenever populated,
    // independent of whether a full code system has been imported (codeSystemEnforceValues below).
    private List<String> codeSystemCode;
    // Opt-in: when true, the field's value is also checked against a fully imported CodeSystem's
    // code list (CodeSystemRegistry), if one is loaded for codeSystemId. A code-system stub (identity
    // known, no code list yet) does not turn this into an error - see SingleFormFieldValidator.
    private Boolean codeSystemEnforceValues;

    public FieldCodeSpec(String codeSystemName, String codeSystemId, String codeSystemVersion,
                          List<String> codeSystemCode, Boolean codeSystemEnforceValues) {
        this.codeSystemName = codeSystemName;
        this.codeSystemId = codeSystemId;
        this.codeSystemVersion = codeSystemVersion;
        this.codeSystemCode = codeSystemCode;
        this.codeSystemEnforceValues = codeSystemEnforceValues;
    }

    public String getCodeSystemName() {
        return codeSystemName;
    }

    public String getCodeSystemId() {
        return codeSystemId;
    }

    public String getCodeSystemVersion() {
        return codeSystemVersion;
    }

    public List<String> getCodeSystemCode() {
        return codeSystemCode;
    }

    public boolean getCodeSystemEnforceValues() {
        return Boolean.TRUE.equals(codeSystemEnforceValues);
    }
}
