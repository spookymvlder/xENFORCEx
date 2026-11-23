package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldValueRestriction;
import com.spookymvlder.formtools.datatypes.fields.CompositeField;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.util.List;
import java.util.function.Consumer;

/**
 * Form-side counterpart to {@link ProcessFieldProperties}. Runs canonicalization steps on parsed
 * form fields before validation, so downstream validators operate on normalized values.
 */
public class ProcessFormExtract {
    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void processForms(List<FormInstance> instances, FieldProperty rootProperty) {
        for (FormInstance instance : instances) {
            FormProcessorContext context = new FormProcessorContext();
            context.setRootProperty(rootProperty);
            context.setStartField(instance.getStartField());

            List<Runnable> steps = List.of(
                    () -> normalizeEnumValues(context)
            );
            for (Runnable step : steps) {
                step.run();
                if (errors.checkCurrentLogOverThreshold()) {
                    System.out.println("Breaking due to errors!");
                    break;
                }
            }
        }
    }

    private static void normalizeEnumValues(FormProcessorContext context) {
        FormField start = context.getStartField();
        if (start == null) {
            return;
        }
        walkNormalizeEnums(start);
    }

    private static void walkNormalizeEnums(FormField node) {
        if (node instanceof SimpleField simple) {
            normalizeEnumValue(simple);
        } else if (node instanceof CompositeField composite) {
            // A constituent's value lives on the composite's own data map, not as its own FormField
            // tree node, so it needs its own walk here rather than being reached by the recursion
            // below - the same reason ValidateFormExtract checks constituents separately from its
            // ordinary child-field walk.
            for (FieldProperty constituent : composite.getProperty().getRelation().getConstituents()) {
                normalizeEnumConstituent(composite, constituent);
            }
        }
        for (FormField child : node.getChildren()) {
            walkNormalizeEnums(child);
        }
    }

    private static void normalizeEnumValue(SimpleField field) {
        FieldProperty property = field.getProperty();
        if (property == null) {
            return;
        }
        normalizeEnumFieldData(property, field.getValue(), newData -> field.setValue(newData));
    }

    private static void normalizeEnumConstituent(CompositeField composite, FieldProperty constituent) {
        String key = constituent.getIdentity().getDisplayName();
        normalizeEnumFieldData(constituent, composite.getConstituent(key),
                newData -> composite.setConstituent(key, newData));
    }

    /**
     * Shared by both a plain field and a composite's constituent: same ENUMERATION semantics
     * (#N index reference, case-insensitive match normalized to declared casing) either way - only
     * where the value is read from and written back to differs, captured by {@code replace}.
     */
    private static void normalizeEnumFieldData(FieldProperty property, FieldData data, Consumer<FieldData> replace) {
        FieldValueRestriction restriction = property.getValueRestriction();
        if (restriction.getDataType() != FieldDataType.ENUMERATION) {
            return;
        }
        List<String> enumerations = restriction.getEnumerations();
        if (enumerations == null || enumerations.isEmpty()) {
            return;
        }

        if (data == null) {
            return;
        }
        String raw = data.getValue();
        if (StringUtils.isNullOrEmpty(raw)) {
            return;
        }
        String trimmed = raw.trim();

        // #N index reference: 1-indexed lookup into the enumerations list.
        if (trimmed.length() > 1 && trimmed.charAt(0) == '#') {
            int idx;
            try {
                idx = Integer.parseInt(trimmed.substring(1).trim());
            } catch (NumberFormatException e) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "Form field for FieldProperty " + property.getFieldId() +
                                " has value " + raw + ", which looks like an enum index reference " +
                                "but is not a valid integer.",
                        ErrorSeverity.ERROR));
                return;
            }
            if (idx < 1 || idx > enumerations.size()) {
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                        "Form field for FieldProperty " + property.getFieldId() +
                                " references enum index " + idx + ", which is out of range " +
                                "(enum list has " + enumerations.size() + " entries).",
                        ErrorSeverity.ERROR));
                return;
            }
            replace.accept(new FieldData(enumerations.get(idx - 1), data.getDataType(), data.getPosition()));
            return;
        }

        // Case-insensitive literal match. Normalize to enum-defined casing on success.
        for (String entry : enumerations) {
            if (entry.equalsIgnoreCase(trimmed)) {
                if (!entry.equals(raw)) {
                    replace.accept(new FieldData(entry, data.getDataType(), data.getPosition()));
                }
                return;
            }
        }

        errors.addToCurrentLog(new ErrorEntry(ErrorSource.FORM_EXTRACT_VALIDATION,
                "Form field for FieldProperty " + property.getFieldId() +
                        " has value " + raw + ", which does not match any entry in the enumeration list.",
                ErrorSeverity.ERROR));
    }
}
