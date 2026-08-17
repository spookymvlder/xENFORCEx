package com.spookymvlder.formtools.datatypes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.function.Function;

/**
 * Builds {@link TypedPredicate}s from rule tokens for each ordered {@link FieldDataType}, and
 * provides helpers to parse raw values and compare two values of the same type. The supported
 * token grammar is shared by all ordered types:
 * <pre>
 *     5            exact value (NOT a predicate — returns null so caller can fall through)
 *     &lt;X           upper bound, exclusive
 *     &lt;=X          upper bound, inclusive
 *     &gt;X           lower bound, exclusive
 *     &gt;=X          lower bound, inclusive
 *     X..Y         inclusive range [X, Y]
 *     &gt;X..Y        (X, Y]
 *     X..&lt;Y        [X, Y)
 *     &gt;X..&lt;Y       (X, Y)
 * </pre>
 * {@code X} and {@code Y} are parsed by the per-type parser (ISO-8601 for dates/times, integer or
 * named month for {@code GMONTH}, etc.). For {@code GMONTH}, both bare integers (1-12) and the
 * {@code #N} prefix resolve to the corresponding month.
 */
public final class TypedPredicateFactory {

    private static final String RANGE_SEPARATOR = "..";

    private TypedPredicateFactory() {
    }

    public static boolean supportsRanges(FieldDataType type) {
        if (type == null) {
            return false;
        }
        if (type.isNumeric()) {
            return true;
        }
        return switch (type) {
            case DATE, TIME, DATETIME, GYEAR, GYEARMONTH, GMONTH, GDAY, GMONTHDAY, DURATION -> true;
            default -> false;
        };
    }

    public static TypedPredicate tryParseToken(String token, FieldDataType type) {
        if (token == null || type == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (type.isNumeric()) {
            return buildPredicate(trimmed, TypedPredicateFactory::parseDouble);
        }
        return switch (type) {
            case DATE -> buildPredicate(trimmed, TypedPredicateFactory::parseLocalDate);
            case TIME -> buildPredicate(trimmed, TypedPredicateFactory::parseLocalTime);
            case DATETIME -> buildPredicate(trimmed, TypedPredicateFactory::parseLocalDateTime);
            case GYEAR -> buildPredicate(trimmed, TypedPredicateFactory::parseYear);
            case GYEARMONTH -> buildPredicate(trimmed, TypedPredicateFactory::parseYearMonth);
            case GMONTH -> buildPredicate(trimmed, TypedPredicateFactory::parseMonth);
            case GDAY -> buildPredicate(trimmed, TypedPredicateFactory::parseGDay);
            case GMONTHDAY -> buildPredicate(trimmed, TypedPredicateFactory::parseMonthDay);
            case DURATION -> buildPredicate(trimmed, TypedPredicateFactory::parseDuration);
            default -> null;
        };
    }

    public static boolean isParseable(String value, FieldDataType type) {
        return parseAsType(value, type) != null;
    }

    /** Returns a negative/zero/positive int per Comparable contract, or null if either parse fails. */
    public static Integer compareValues(String left, String right, FieldDataType type) {
        Comparable<?> l = parseAsType(left, type);
        Comparable<?> r = parseAsType(right, type);
        if (l == null || r == null) {
            return null;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        int cmp = ((Comparable) l).compareTo(r);
        return cmp;
    }

    private static Comparable<?> parseAsType(String value, FieldDataType type) {
        if (value == null || type == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (type.isNumeric()) {
            return parseDouble(trimmed);
        }
        return switch (type) {
            case DATE -> parseLocalDate(trimmed);
            case TIME -> parseLocalTime(trimmed);
            case DATETIME -> parseLocalDateTime(trimmed);
            case GYEAR -> parseYear(trimmed);
            case GYEARMONTH -> parseYearMonth(trimmed);
            case GMONTH -> parseMonth(trimmed);
            case GDAY -> parseGDay(trimmed);
            case GMONTHDAY -> parseMonthDay(trimmed);
            case DURATION -> parseDuration(trimmed);
            default -> null;
        };
    }

    private static <T extends Comparable<T>> TypedPredicate buildPredicate(String token, Function<String, T> parser) {
        Bounds<T> bounds = parseBounds(token, parser);
        if (bounds == null) {
            return null;
        }
        return value -> {
            if (value == null) {
                return false;
            }
            T parsed = parser.apply(value.trim());
            return parsed != null && bounds.test(parsed);
        };
    }

    private static <T extends Comparable<T>> Bounds<T> parseBounds(String token, Function<String, T> parser) {
        int rangeIdx = token.indexOf(RANGE_SEPARATOR);
        if (rangeIdx >= 0) {
            String leftPart = token.substring(0, rangeIdx).trim();
            String rightPart = token.substring(rangeIdx + RANGE_SEPARATOR.length()).trim();
            if (leftPart.isEmpty() || rightPart.isEmpty()) {
                return null;
            }
            // Range left side may only carry an optional '>' (lower bound, exclusive marker).
            boolean lowerInclusive = true;
            String leftNum = leftPart;
            if (leftPart.startsWith(">=") || leftPart.startsWith("<=") || leftPart.startsWith("<")) {
                return null;
            }
            if (leftPart.startsWith(">")) {
                lowerInclusive = false;
                leftNum = leftPart.substring(1).trim();
            }
            // Range right side may only carry an optional '<' (upper bound, exclusive marker).
            boolean upperInclusive = true;
            String rightNum = rightPart;
            if (rightPart.startsWith(">=") || rightPart.startsWith("<=") || rightPart.startsWith(">")) {
                return null;
            }
            if (rightPart.startsWith("<")) {
                upperInclusive = false;
                rightNum = rightPart.substring(1).trim();
            }
            T lower = parser.apply(leftNum);
            T upper = parser.apply(rightNum);
            if (lower == null || upper == null) {
                return null;
            }
            return new Bounds<>(lower, lowerInclusive, upper, upperInclusive);
        }
        if (token.startsWith("<=")) {
            T n = parser.apply(token.substring(2).trim());
            return n == null ? null : new Bounds<>(null, true, n, true);
        }
        if (token.startsWith(">=")) {
            T n = parser.apply(token.substring(2).trim());
            return n == null ? null : new Bounds<>(n, true, null, true);
        }
        if (token.startsWith("<")) {
            T n = parser.apply(token.substring(1).trim());
            return n == null ? null : new Bounds<>(null, true, n, false);
        }
        if (token.startsWith(">")) {
            T n = parser.apply(token.substring(1).trim());
            return n == null ? null : new Bounds<>(n, false, null, true);
        }
        return null;
    }

    // ---- per-type value parsers ----

    private static Double parseDouble(String s) {
        if (s == null) return null;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static LocalDate parseLocalDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalTime parseLocalTime(String s) {
        if (s == null) return null;
        try {
            return LocalTime.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static LocalDateTime parseLocalDateTime(String s) {
        if (s == null) return null;
        try {
            return LocalDateTime.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static Year parseYear(String s) {
        if (s == null) return null;
        try {
            return Year.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static YearMonth parseYearMonth(String s) {
        if (s == null) return null;
        try {
            return YearMonth.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a day-of-month value. Accepts XML Schema {@code gDay} format ({@code ---DD}) or a bare
     * integer 1-31. Returned as a boxed Integer (Comparable) since {@code java.time} has no
     * standalone DayOfMonth type. Returns null on no match.
     */
    private static Integer parseGDay(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (t.startsWith("---")) {
            t = t.substring(3).trim();
        }
        try {
            int day = Integer.parseInt(t);
            return (day >= 1 && day <= 31) ? day : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses an ISO-8601 month-day token via {@link MonthDay}, e.g., {@code --12-25}.
     * Returns null on parse failure.
     */
    private static MonthDay parseMonthDay(String s) {
        if (s == null) return null;
        try {
            return MonthDay.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses an ISO-8601 duration token via {@link Duration}, e.g., {@code PT1H30M}.
     * Returns null on parse failure.
     */
    private static Duration parseDuration(String s) {
        if (s == null) return null;
        try {
            return Duration.parse(s.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Parses a month token. Accepts {@code #N} (1-indexed), a bare integer 1-12, a full month name
     * (case-insensitive, English only), or any prefix length ≥ 3 of the English month name
     * (e.g., {@code Jan}, {@code JANU}). Returns null on no match.
     */
    private static Month parseMonth(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        if (t.charAt(0) == '#' && t.length() > 1) {
            try {
                int idx = Integer.parseInt(t.substring(1).trim());
                if (idx >= 1 && idx <= 12) {
                    return Month.of(idx);
                }
            } catch (NumberFormatException ignored) {
                // fall through
            }
            return null;
        }
        try {
            int idx = Integer.parseInt(t);
            if (idx >= 1 && idx <= 12) {
                return Month.of(idx);
            }
            return null;
        } catch (NumberFormatException ignored) {
            // fall through to name match
        }
        String upper = t.toUpperCase();
        try {
            return Month.valueOf(upper);
        } catch (IllegalArgumentException ignored) {
            // fall through to prefix match
        }
        if (upper.length() >= 3) {
            for (Month m : Month.values()) {
                if (m.name().startsWith(upper)) {
                    return m;
                }
            }
        }
        return null;
    }
}
