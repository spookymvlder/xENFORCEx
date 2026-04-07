package com.spookymvlder.formtools.settings;

import com.spookymvlder.formtools.processsor.FieldPropertyConverter;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Optional overrides applied to field properties after CSV import and defaults,
 * before sub-object construction. Only fields explicitly set here will differ
 * from the imported/defaulted values. All override maps are keyed by fieldId.
 */
public class ProcessingOverrides {
    private static ProcessingOverrides instance;

    private String dateFormat;
    private Map<String, String> displayNameOverrides;
    private Map<String, Boolean> activeOverrides;
    private Map<String, Boolean> skipImportOverrides;

    private ProcessingOverrides() {
        this.dateFormat = "dd-MM-yyyy";
        this.displayNameOverrides = new HashMap<>();
        this.activeOverrides = new HashMap<>();
        this.skipImportOverrides = new HashMap<>();
    }

    public static ProcessingOverrides getInstance() {
        if (instance == null) {
            instance = new ProcessingOverrides();
        }
        return instance;
    }

    // --- Date format ---

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public void setConverters() {
        FieldPropertyConverter.CustomDateConverter.setFormatter(DateTimeFormatter.ofPattern(this.dateFormat));
    }

    // --- Display name overrides ---

    public void addDisplayNameOverride(String fieldId, String displayName) {
        this.displayNameOverrides.put(fieldId, displayName);
    }

    public String getDisplayNameOverride(String fieldId) {
        return this.displayNameOverrides.get(fieldId);
    }

    // --- Active overrides ---

    public void addActiveOverride(String fieldId, Boolean active) {
        this.activeOverrides.put(fieldId, active);
    }

    public Boolean getActiveOverride(String fieldId) {
        return this.activeOverrides.get(fieldId);
    }

    // --- Skip import overrides ---

    public void addSkipImportOverride(String fieldId, Boolean skipImport) {
        this.skipImportOverrides.put(fieldId, skipImport);
    }

    public Boolean getSkipImportOverride(String fieldId) {
        return this.skipImportOverrides.get(fieldId);
    }
}