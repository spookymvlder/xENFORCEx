package com.spookymvlder.formtools.datatypes;

/**
 * Predicate over a raw string value. The implementation knows how to parse the string into its
 * native type and apply the appropriate comparison. Returns false on parse failure or null input.
 */
@FunctionalInterface
public interface TypedPredicate {
    boolean test(String value);
}
