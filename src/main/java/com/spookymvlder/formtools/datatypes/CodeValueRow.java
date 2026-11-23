package com.spookymvlder.formtools.datatypes;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.spookymvlder.formtools.processsor.FieldPropertyConverter;

import java.time.LocalDate;

// Represents one OpenCSV-bound row of a code system's "values" CSV (code, displayName, and the same
// active/expireDate retirement convention used by FieldPropertyRow).
public class CodeValueRow {
    @CsvBindByName(required = true)
    private String code;
    @CsvBindByName(required = true)
    private String displayName;
    @CsvCustomBindByName(column = "active", converter = FieldPropertyConverter.TFBooleanConverter.class)
    private Boolean active;
    @CsvCustomBindByName(column = "expireDate", converter = FieldPropertyConverter.CustomDateConverter.class)
    private LocalDate expireDate;

    public String getCode() {
        return code;
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
}
