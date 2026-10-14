package com.spookymvlder.formtools.datatypes;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Usage code for whether a field must be present in a form, and if present whether it must
 * carry a value. Inspired by the HL7 v2 conformance methodology, but written in our own words
 * and using only the codes this project supports.
 */
public enum Usage {
    /** Required and populated. Field must be present and must carry a non-empty value. */
    R("R"),
    /** Required, may be empty. Field must be present, but the value may be empty. */
    RE("RE"),
    /** Conditionally required and populated. Resolves to R when the condition matches, otherwise O. */
    CR("CR"),
    /** Conditionally required, may be empty. Resolves to RE when the condition matches, otherwise O. */
    CE("CE"),
    /** Optional. No constraint on presence or value. */
    O("O");

    private final String abbreviation;

    Usage(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    private static final Map<String, Usage> map = Arrays.stream(values())
            .collect(Collectors.toMap(v -> v.abbreviation, v -> v));

    public static Usage valueOfAbbreviation(String abbreviation) {
        return map.get(abbreviation);
    }

    public boolean isConditional() {
        return this == CR || this == CE;
    }

    public boolean requiresPopulated() {
        return this == R || this == CR;
    }
}
