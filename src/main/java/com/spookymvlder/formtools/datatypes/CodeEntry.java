package com.spookymvlder.formtools.datatypes;

import java.time.LocalDate;

/**
 * One row of an imported code system's value list: a code, its human-readable label, and the same
 * active/expireDate retirement convention already used by FieldPropertyRow. displayName is carried
 * for messages/UI only - matching is always on code alone.
 */
public class CodeEntry {
    private String code;
    private String displayName;
    private Boolean active;
    private LocalDate expireDate;

    public CodeEntry() {
    }

    public CodeEntry(String code, String displayName, Boolean active, LocalDate expireDate) {
        this.code = code;
        this.displayName = displayName;
        this.active = active;
        this.expireDate = expireDate;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDate getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }
}
