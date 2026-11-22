package com.spookymvlder.formtools.datatypes;

import java.util.List;

/**
 * Matches a field's value against the comma-delimited tokens declared in a CODED_VALUE field's
 * codeSystemCode column. Each token is one of:
 * <ul>
 *     <li>a literal code - exact, case-sensitive match (codes are identifiers, not free-text labels,
 *     so unlike ENUMERATION there is no case-insensitive re-casing);</li>
 *     <li>a numeric range {@code start..end}, reusing {@link TypedPredicateFactory}'s existing {@code
 *     ..} range parser rather than a second implementation - only recognized when the token actually
 *     contains the range separator, so a bare threshold token like "&lt;5" (which TypedPredicateFactory
 *     would otherwise also parse) is intentionally left to fall through as a literal instead, since
 *     open thresholds were never asked for here;</li>
 *     <li>a prefix wildcard, a single trailing {@code *} (e.g. {@code R45.2*}), via a plain
 *     {@code String.startsWith} check - deliberately not routed through the range parser, since
 *     lexicographic ranges over alphanumeric codes produce surprising results (e.g. "A10" &lt; "A9").</li>
 * </ul>
 */
public final class CodeConstraintMatcher {

    private static final String RANGE_SEPARATOR = "..";

    private CodeConstraintMatcher() {
    }

    /** Schema-side syntax check, at CSV import time - same timing as an invalid regexPattern. */
    public static boolean isValidToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        String trimmed = token.trim();
        if (trimmed.contains(RANGE_SEPARATOR)) {
            return TypedPredicateFactory.tryParseToken(trimmed, FieldDataType.DOUBLE) != null;
        }
        if (trimmed.endsWith("*")) {
            return trimmed.length() > 1;
        }
        return true;
    }

    /** Document-side membership check: true if value matches at least one declared token. */
    public static boolean matches(String value, List<String> tokens) {
        if (value == null || tokens == null) {
            return false;
        }
        for (String token : tokens) {
            if (matchesToken(value, token)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesToken(String value, String token) {
        String trimmed = token.trim();
        if (trimmed.contains(RANGE_SEPARATOR)) {
            TypedPredicate predicate = TypedPredicateFactory.tryParseToken(trimmed, FieldDataType.DOUBLE);
            return predicate != null && predicate.test(value);
        }
        if (trimmed.endsWith("*")) {
            return value.startsWith(trimmed.substring(0, trimmed.length() - 1));
        }
        return value.equals(trimmed);
    }
}
