package com.spookymvlder.formtools.output;

import com.spookymvlder.formtools.datatypes.ConditionalBool;
import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.fields.CompositeField;
import com.spookymvlder.formtools.datatypes.fields.ContainerField;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Walks a parsed, validated {@link FormInstance} into a generic value tree - nested
 * {@code Map<String,Object>}/{@code List<Object>}/scalar, no serialization format baked in. This is
 * deliberately the same shape whether the caller wants it as a plain Java object (embedding this as a
 * library) or as JSON (see {@link JsonFormWriter}): the tree is the actual output design, and each
 * format is just a mechanical walk over it.
 * <p>
 * Two things this shapes deliberately, not incidentally:
 * <ul>
 *   <li>Whether a field's value is a scalar or a list is decided by the SCHEMA
 *       (FieldCardinality.fieldRepeated), not by how many instances happen to appear in this
 *       particular document. Otherwise the same key could be an object in one document and an array
 *       in another, depending on data, which is exactly the kind of instability that makes an
 *       ad hoc XML/JSON export unpleasant to write a consumer against.</li>
 *   <li>Leaf values are converted per FieldDataType (numeric -&gt; Long/Double, BOOLEAN -&gt;
 *       Boolean) rather than left as the raw String they're stored as internally - otherwise every
 *       value in the export would be a JSON string, including numbers and booleans.</li>
 * </ul>
 */
public final class FormValueBuilder {

    private FormValueBuilder() {
    }

    public static Map<String, Object> build(FormInstance instance) {
        return buildChildren(instance.getStartField().getChildren());
    }

    private static Map<String, Object> buildChildren(List<FormField> children) {
        // Group by fieldId first, not fieldName: two DIFFERENT fields (distinct fieldId) can
        // legitimately share one fieldName as siblings, disambiguated only by which attribute each
        // reads (see ProcessFieldProperties.siblingIdentityKey, the schema-validation rule this
        // mirrors) - e.g. a <code code="DD" type="MV"/> element read by two separate fields. Grouping
        // by fieldName alone would silently merge them into one bogus array of unrelated values.
        Map<String, List<FormField>> byFieldId = new LinkedHashMap<>();
        for (FormField child : children) {
            byFieldId.computeIfAbsent(child.getProperty().getFieldId(), key -> new ArrayList<>()).add(child);
        }

        // Count how many distinct fieldIds want the same bare fieldName as their output key, so a
        // colliding pair can be qualified while the common, non-colliding case stays plain.
        Map<String, Integer> nameUseCount = new LinkedHashMap<>();
        for (List<FormField> instances : byFieldId.values()) {
            nameUseCount.merge(instances.get(0).getProperty().getFieldName(), 1, Integer::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (List<FormField> instances : byFieldId.values()) {
            FormField first = instances.get(0);
            String fieldName = first.getProperty().getFieldName();
            String key = nameUseCount.get(fieldName) > 1 ? qualifiedKey(first, fieldName) : fieldName;

            boolean repeatsPerSchema = first.getProperty().getCardinality().getFieldRepeated()
                    != ConditionalBool.FALSE;
            // More instances of one fieldId than the schema declares is itself a validation problem
            // reported elsewhere; the output layer's job is to not silently drop the extra data, not
            // to re-enforce cardinality.
            if (repeatsPerSchema || instances.size() > 1) {
                List<Object> values = new ArrayList<>();
                for (FormField instance : instances) {
                    values.add(buildField(instance));
                }
                result.put(key, values);
            } else {
                result.put(key, buildField(first));
            }
        }
        return result;
    }

    /** Only called for a fieldName two distinct fieldIds share - qualifies it by what distinguishes
     * them (which attribute, or the element's own text), matching the schema-side sibling-identity
     * rule so a collision here is never a case the schema layer would have called ambiguous. */
    private static String qualifiedKey(FormField field, String fieldName) {
        var restriction = field.getProperty().getValueRestriction();
        String target = restriction.getAttribute() ? "@" + restriction.getAttributeName() : "#text";
        return fieldName + target;
    }

    private static Object buildField(FormField field) {
        if (field instanceof CompositeField composite) {
            return buildConstituents(composite.getValue());
        }
        if (field instanceof SimpleField simple) {
            Object scalar = buildScalar(simple.getValue());
            if (field.getChildren().isEmpty()) {
                return scalar;
            }
            // A field can carry its own value (e.g. from an attribute) AND have child elements - the
            // XML equivalent of mixed content. Dropping the children here would silently lose data,
            // so fold them into an object alongside a reserved key for the field's own value, rather
            // than only handling the (much more common) either-a-value-or-children case.
            Map<String, Object> merged = new LinkedHashMap<>();
            merged.put("_value", scalar);
            merged.putAll(buildChildren(field.getChildren()));
            return merged;
        }
        // ContainerField, or anything else structural: recurse into its own children.
        return buildChildren(field.getChildren());
    }

    private static Map<String, Object> buildConstituents(Map<String, FieldData> constituents) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (constituents == null) {
            return result;
        }
        for (Map.Entry<String, FieldData> entry : constituents.entrySet()) {
            result.put(entry.getKey(), buildScalar(entry.getValue()));
        }
        return result;
    }

    private static Object buildScalar(FieldData data) {
        if (data == null || StringUtils.isNullOrEmpty(data.getValue())) {
            return null;
        }
        String value = data.getValue().trim();
        FieldDataType type = data.getDataType();
        if (type == null) {
            return value;
        }
        if (type == FieldDataType.INTEGER || type == FieldDataType.LONG) {
            try {
                return Long.parseLong(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        if (type.isNumeric()) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }
        if (type == FieldDataType.BOOLEAN) {
            String normalized = value.toLowerCase();
            if (normalized.equals("true") || normalized.equals("t") || normalized.equals("1")) {
                return Boolean.TRUE;
            }
            if (normalized.equals("false") || normalized.equals("f") || normalized.equals("0")) {
                return Boolean.FALSE;
            }
            return value;
        }
        // DATE/TIME/DATETIME/ENUMERATION/etc. are already in their canonical string form
        // (ISO-8601, canonicalized enum casing) by the time a document reaches this stage.
        return value;
    }
}
