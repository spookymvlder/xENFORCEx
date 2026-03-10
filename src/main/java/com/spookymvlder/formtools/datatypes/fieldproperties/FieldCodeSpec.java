package com.spookymvlder.formtools.datatypes.fieldproperties;

public class FieldCodeSpec {
    private String codeSystemName;
    private String codeSystemId;
    private String codeSystemVersion;
    private String codeSystemCode;

    public FieldCodeSpec(String codeSystemName, String codeSystemId, String codeSystemVersion, String codeSystemCode) {
        this.codeSystemName = codeSystemName;
        this.codeSystemId = codeSystemId;
        this.codeSystemVersion = codeSystemVersion;
        this.codeSystemCode = codeSystemCode;
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

    public String getCodeSystemCode() {
        return codeSystemCode;
    }
}
