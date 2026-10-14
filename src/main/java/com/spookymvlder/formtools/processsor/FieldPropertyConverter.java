package com.spookymvlder.formtools.processsor;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.datatypes.Usage;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldPropertyRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class FieldPropertyConverter {
    public static class CustomDateConverter extends AbstractBeanField<LocalDate, String> {
        private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        public static void setFormatter(DateTimeFormatter f) {
            formatter = f;
        }

        @Override
        protected LocalDate convert(String value) throws CsvDataTypeMismatchException {
            if (value == null || value.isBlank()) return null;
            try {
                return LocalDate.parse(value, formatter);
            } catch (DateTimeParseException e) {
                throw new CsvDataTypeMismatchException("Date parse failed for: " + value);
            }
        }
    }

    public static class RepeatConverter extends AbstractBeanField<Character,String> {
        @Override
        protected Character convert(String value) throws CsvDataTypeMismatchException {
            if (!value.trim().toUpperCase().matches("[TFC]")) {
                throw new CsvDataTypeMismatchException("Invalid repeat flag: " + value);
            }
            return value.charAt(0);
        }
    }

    public static class BooleanYNConverter extends AbstractBeanField<Boolean, String> {
        @Override
        protected Boolean convert(String value) throws CsvDataTypeMismatchException {
            if ("Y".equalsIgnoreCase(value) || "YES".equalsIgnoreCase(value)) return true;
            if ("N".equalsIgnoreCase(value) || "NO".equalsIgnoreCase(value)) return false;
            throw new CsvDataTypeMismatchException("Invalid Y/N value: " + value);
        }
    }

    public static class TFBooleanConverter extends AbstractBeanField<Boolean, String> {
        @Override
        protected Boolean convert(String fieldValue) throws IllegalArgumentException {
            if (fieldValue == null|| fieldValue.isBlank()){
                return null;
            }
            return switch (fieldValue.trim().toUpperCase()) {
                case "T", "TRUE" -> true;
                case "F", "FALSE" -> false;
                default -> throw new IllegalArgumentException("Invalid boolean value: " + fieldValue);
            };
        }
    }



    public static String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Blank-normalizes a column bound by one of OpenCSV's split annotations. Splitting an empty cell
     * yields a single-element list holding an empty string, which survives both null and isEmpty
     * checks and reads downstream as "this column was populated". Returns null when nothing real
     * remains, so an unpopulated column is indistinguishable from an absent one.
     */
    public static List<String> normalizeList(List<String> values) {
        if (values == null) {
            return null;
        }

        List<String> normalized = new ArrayList<>();
        for (String value : values) {
            String entry = normalizeBlank(value);
            if (entry != null) {
                normalized.add(entry);
            }
        }
        return normalized.isEmpty() ? null : normalized;
    }

    // These four throw a clean, field-facing IllegalArgumentException (never the raw Enum.valueOf
    // message, which leaks the fully-qualified class name) so the catch in
    // ProcessFieldProperties.convertFieldProperties can log a readable FATAL instead of the message
    // being the only thing standing between a typo and an uncaught stack trace.
    public static FieldDataType parseDataType(String value) {
        String normalizedValue = normalizeBlank(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return FieldDataType.valueOf(normalizedValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid dataType: " + value + " (see ImportSpec.txt for the supported list)");
        }
    }

    public static GroupingType parseGroupingType(String value) {
        String normalizedValue = normalizeBlank(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return GroupingType.valueOf(normalizedValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid groupingType (expected NONE, CONTAINER, "
                    + "COMPOSITE, or CONSTITUENT): " + value);
        }
    }

    public static LinkLogic parseLinkLogic(String column, String value) {
        String normalizedValue = normalizeBlank(value);
        if (normalizedValue == null) {
            return null;
        }
        try {
            return LinkLogic.valueOf(normalizedValue.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid " + column + " (expected AND or OR): " + value);
        }
    }

    public static Usage parseUsage(String value) {
        String normalizedValue = normalizeBlank(value);
        if (normalizedValue == null) {
            return null;
        }
        Usage usage = Usage.valueOfAbbreviation(normalizedValue.toUpperCase());
        if (usage == null) {
            throw new IllegalArgumentException(
                    "Invalid required usage code (expected R, RE, CR, CE, or O): " + value);
        }
        return usage;
    }

    public static ConditionalBool parseConditionalBool(Character value) {
        String normalizedValue = normalizeBlank(String.valueOf(value));
        if (normalizedValue == null) {
            return null;
        }
        return ConditionalBool.valueOfAbbreviation(normalizedValue.toUpperCase());
    }


}
