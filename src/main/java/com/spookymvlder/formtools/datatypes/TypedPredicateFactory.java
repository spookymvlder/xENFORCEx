package com.spookymvlder.formtools.datatypes;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * For {@code DATE} and {@code DATETIME}, a bound endpoint (but never the field's actual value) may
 * also be a signed relative offset: {@code -P6M} ("6 months ago"), {@code +P1Y} ("1 year from now").
 * The sign is mandatory — the engine has no way to infer whether a field is past-anchored (a
 * birthdate) or future-anchored (an expiration date), so the direction must be stated explicitly
 * rather than defaulted. The offset resolves against the current date/time at the moment the
 * document is validated, not at the moment the schema was imported. This intentionally does not
 * apply to {@code DURATION}-typed field values themselves: those have no calendar anchor to resolve
 * a year or month against (there is no "today" for a free-floating elapsed-time value like a call
 * length), so they remain limited to the exact-length {@code PT..} / {@code P..D} forms.
 */
public final class TypedPredicateFactory {

    private static final String RANGE_SEPARATOR = "..";

    // Signed relative offset for DATE/DATETIME bounds, e.g. "-P1Y2M3D" or "+P1DT4H5M6S". The sign is
    // required (see class javadoc). Named groups avoid ambiguity between the "M" for months (date
    // part) and "M" for minutes (time part).
    private static final Pattern RELATIVE_OFFSET_PATTERN = Pattern.compile(
            "(?<sign>[+-])P(?:(?<years>\\d+)Y)?(?:(?<months>\\d+)M)?(?:(?<weeks>\\d+)W)?(?:(?<days>\\d+)D)?"
                    + "(?:T(?:(?<hours>\\d+)H)?(?:(?<minutes>\\d+)M)?(?:(?<seconds>\\d+)S)?)?");

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
            // Bound side accepts a relative offset in addition to a literal; the value side tested
            // against it (inside buildPredicate) stays literal-only, see class javadoc.
            case DATE -> buildPredicate(trimmed, TypedPredicateFactory::parseDateBound, TypedPredicateFactory::parseLocalDate);
            case DATETIME -> buildPredicate(trimmed, TypedPredicateFactory::parseDateTimeBound, TypedPredicateFactory::parseLocalDateTime);
            case TIME -> buildPredicate(trimmed, TypedPredicateFactory::parseLocalTime);
            case GYEAR -> buildPredicate(trimmed, TypedPredicateFactory::parseYear);
            case GYEARMONTH -> buildPredicate(trimmed, TypedPredicateFactory::parseYearMonth);
            case GMONTH -> buildPredicate(trimmed, TypedPredicateFactory::parseMonth);
            case GDAY -> buildPredicate(trimmed, TypedPredicateFactory::parseGDay);
            case GMONTHDAY -> buildPredicate(trimmed, TypedPredicateFactory::parseMonthDay);
            case DURATION -> buildPredicate(trimmed, TypedPredicateFactory::parseDuration);
            default -> null;
        };
    }

    /** Strict, literal-only parse check for a field's actual value. Never accepts a relative offset. */
    public static boolean isParseable(String value, FieldDataType type) {
        return parseAsType(value, type, false) != null;
    }

    /** Parse check for a schema-declared bound (rangeStart/rangeEnd, a conditional-value token).
     * For DATE/DATETIME, also accepts the signed relative-offset grammar described in the class
     * javadoc. */
    public static boolean isBoundParseable(String bound, FieldDataType type) {
        return parseAsType(bound, type, true) != null;
    }

    /**
     * Compares a field's literal value against a schema-declared bound. Returns a negative/zero/
     * positive int per the {@link Comparable} contract, or null if either side fails to parse.
     */
    public static Integer compareValueToBound(String value, String bound, FieldDataType type) {
        return compare(parseAsType(value, type, false), parseAsType(bound, type, true));
    }

    /**
     * Compares two schema-declared bounds against each other (e.g. rangeStart vs rangeEnd). Both
     * sides may use the relative-offset grammar. When one or both are relative, the comparison
     * resolves them against the same instant, so the result is stable regardless of which date that
     * happens to be — the exception is a schema that mixes one relative and one literal bound, where
     * the result reflects only the day the schema happened to be imported on; that combination is
     * documented as an open caveat rather than solved here.
     */
    public static Integer compareBounds(String left, String right, FieldDataType type) {
        return compare(parseAsType(left, type, true), parseAsType(right, type, true));
    }

    private static Integer compare(Comparable<?> left, Comparable<?> right) {
        if (left == null || right == null) {
            return null;
        }
        @SuppressWarnings({"unchecked", "rawtypes"})
        int cmp = ((Comparable) left).compareTo(right);
        return cmp;
    }

    private static Comparable<?> parseAsType(String value, FieldDataType type, boolean asBound) {
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
            case DATE -> asBound ? parseDateBound(trimmed) : parseLocalDate(trimmed);
            case DATETIME -> asBound ? parseDateTimeBound(trimmed) : parseLocalDateTime(trimmed);
            case TIME -> parseLocalTime(trimmed);
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
        return buildPredicate(token, parser, parser);
    }

    /**
     * @param boundParser interprets the literal(s) embedded in the token itself (parseBounds).
     * @param valueParser interprets the runtime value the resulting predicate is tested against.
     *                    Kept separate from boundParser so a document's actual field value is never
     *                    accidentally reinterpreted as a relative offset (see class javadoc).
     */
    private static <T extends Comparable<T>> TypedPredicate buildPredicate(
            String token, Function<String, T> boundParser, Function<String, T> valueParser) {
        Bounds<T> bounds = parseBounds(token, boundParser);
        if (bounds == null) {
            return null;
        }
        return value -> {
            if (value == null) {
                return false;
            }
            T parsed = valueParser.apply(value.trim());
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

    /** Bound-context parser: a literal date, or a signed relative offset resolved against today. */
    private static LocalDate parseDateBound(String s) {
        LocalDate literal = parseLocalDate(s);
        if (literal != null) {
            return literal;
        }
        return parseRelativeDate(s, LocalDate.now());
    }

    /** Bound-context parser: a literal date-time, or a signed relative offset resolved against now. */
    private static LocalDateTime parseDateTimeBound(String s) {
        LocalDateTime literal = parseLocalDateTime(s);
        if (literal != null) {
            return literal;
        }
        return parseRelativeDateTime(s, LocalDateTime.now());
    }

    /**
     * Resolves a signed relative offset (e.g. {@code -P6M}) against {@code anchor}. A LocalDate has
     * no time-of-day, so a token carrying an H/M/S component is rejected rather than silently
     * ignored. Returns null if the token isn't a relative offset, or carries no components at all.
     */
    private static LocalDate parseRelativeDate(String s, LocalDate anchor) {
        Matcher m = RELATIVE_OFFSET_PATTERN.matcher(s.trim());
        if (!m.matches() || !hasAnyOffsetComponent(m)) {
            return null;
        }
        if (m.group("hours") != null || m.group("minutes") != null || m.group("seconds") != null) {
            return null;
        }
        Period period = toPeriod(m);
        return "-".equals(m.group("sign")) ? anchor.minus(period) : anchor.plus(period);
    }

    /** Resolves a signed relative offset (e.g. {@code +P1DT4H}) against {@code anchor}. */
    private static LocalDateTime parseRelativeDateTime(String s, LocalDateTime anchor) {
        Matcher m = RELATIVE_OFFSET_PATTERN.matcher(s.trim());
        if (!m.matches() || !hasAnyOffsetComponent(m)) {
            return null;
        }
        Period period = toPeriod(m);
        Duration duration = toDuration(m);
        return "-".equals(m.group("sign")) ? anchor.minus(period).minus(duration) : anchor.plus(period).plus(duration);
    }

    private static boolean hasAnyOffsetComponent(Matcher m) {
        return m.group("years") != null || m.group("months") != null || m.group("weeks") != null
                || m.group("days") != null || m.group("hours") != null || m.group("minutes") != null
                || m.group("seconds") != null;
    }

    private static int groupInt(Matcher m, String name) {
        String g = m.group(name);
        return g == null ? 0 : Integer.parseInt(g);
    }

    private static Period toPeriod(Matcher m) {
        int days = groupInt(m, "days") + groupInt(m, "weeks") * 7;
        return Period.of(groupInt(m, "years"), groupInt(m, "months"), days);
    }

    private static Duration toDuration(Matcher m) {
        return Duration.ofHours(groupInt(m, "hours"))
                .plusMinutes(groupInt(m, "minutes"))
                .plusSeconds(groupInt(m, "seconds"));
    }

    /** True if the token looks like an unsigned ISO period/duration (missing the required +/- sign). */
    public static boolean looksLikeUnsignedRelativeOffset(String token) {
        return token != null && token.trim().startsWith("P") && token.trim().length() > 1;
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
