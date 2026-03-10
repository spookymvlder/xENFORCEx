package com.spookymvlder.formtools.datatypes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum ConditionalBool {
    CONDITIONALLY_TRUE("C"),
    FALSE("F"),
    TRUE("T");

    private final String abbreviation;

    ConditionalBool(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    private static final Map<String, ConditionalBool> map = Arrays.stream(values())
            .collect(Collectors.toMap(v ->v.abbreviation, v -> v));

    public static ConditionalBool valueOfAbbreviation(String abbreviation) {
        return map.get(abbreviation);
    }
}

