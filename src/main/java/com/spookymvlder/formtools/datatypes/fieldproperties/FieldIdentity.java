package com.spookymvlder.formtools.datatypes.fieldproperties;

import java.time.LocalDate;

public class FieldIdentity {
    private String fieldId;
    private String fieldName;
    //TODO error checking if the fieldName is a duplicate for something at the same hierarchy level and is not repeating.
    private String displayName;
    //TODO figure out how to modularize
    private Boolean active;
    //TODO make active date to make updates easier.
    private LocalDate expireDate;
    private Boolean skipImport;
    private String templatePath = null;

    public FieldIdentity(String fieldId,
                         String fieldName,
                         String displayName,
                         Boolean active,
                         LocalDate expireDate,
                         Boolean skipImport) {
        this.fieldId = fieldId;
        this.fieldName = fieldName;
        this.displayName = validateDisplayName(displayName);
        this.active = active;
        this.expireDate = expireDate;
        this.skipImport = skipImport;
    }

    private String validateDisplayName(String displayName) {
        if (displayName == null) {
            return this.fieldName;
        }
        return displayName;
    }

    public String getFieldId() {
        return fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Boolean getActive() {
        return active;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public boolean hasExpireDate() {
        return expireDate != null;
    }

    public boolean isExpired(LocalDate date) {
        return hasExpireDate() && expireDate.isBefore(date);
    }

    public boolean expiresInFuture(LocalDate date) {
        return hasExpireDate() && expireDate.isAfter(date);
    }

    public Boolean getSkipImport() {
        return Boolean.TRUE.equals(this.skipImport);
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }
}
