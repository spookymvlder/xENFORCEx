package com.spookymvlder.formtools.datatypes;

/**
 * Generic lower/upper bound predicate with per-side inclusivity. Either bound may be null to
 * indicate unbounded on that side.
 */
public class Bounds<T extends Comparable<T>> {
    private final T lower;
    private final boolean lowerInclusive;
    private final T upper;
    private final boolean upperInclusive;

    public Bounds(T lower, boolean lowerInclusive, T upper, boolean upperInclusive) {
        this.lower = lower;
        this.lowerInclusive = lowerInclusive;
        this.upper = upper;
        this.upperInclusive = upperInclusive;
    }

    public boolean test(T value) {
        if (value == null) {
            return false;
        }
        if (lower != null) {
            int cmp = value.compareTo(lower);
            if (lowerInclusive ? cmp < 0 : cmp <= 0) {
                return false;
            }
        }
        if (upper != null) {
            int cmp = value.compareTo(upper);
            if (upperInclusive ? cmp > 0 : cmp >= 0) {
                return false;
            }
        }
        return true;
    }
}
