package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldCardinality;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.FormStructure;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConditionalRequirementEvaluator {

    //TODO support enumeration-index value references and ranges/comparators on ordered non-numeric
    //     types (DATE, TIME, GMONTH, etc. — each needs its own comparison semantics).
    //TODO support across-instance ALL semantics (currently across-instance is OR — "any matching
    //     instance fires"). The form-author opt-in for "all instances must satisfy" is future work.
    public static boolean isConditionRequired(FormField dependentParent, FieldProperty dependent,
                                              ExtractValidatorContext context) {
        FieldCardinality cardinality = dependent.getCardinality();
        List<String> sourceIds = cardinality.getCondRequiredSourceIds();
        List<String[]> valueSets = cardinality.getCondRequiredValues();
        LinkLogic logic = cardinality.getCondRequiredLogic();

        if (sourceIds == null || sourceIds.isEmpty()) {
            return false;
        }

        if (logic == LinkLogic.OR) {
            return evaluateOr(dependentParent, dependent.getFieldId(), sourceIds, valueSets, context);
        }
        // Default to AND when logic is null or AND.
        return evaluateAnd(dependentParent, dependent.getFieldId(), sourceIds, valueSets, context);
    }

    private static boolean evaluateOr(FormField dependentParent, String dependentFieldId,
                                      List<String> sourceIds, List<String[]> valueSets,
                                      ExtractValidatorContext context) {
        for (int i = 0; i < sourceIds.size(); i++) {
            String[] expected = expectedFor(valueSets, i);
            List<FormField> instances = findAllSources(dependentParent, dependentFieldId,
                    sourceIds.get(i), context);
            for (FormField instance : instances) {
                if (matches(instance, expected)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean evaluateAnd(FormField dependentParent, String dependentFieldId,
                                       List<String> sourceIds, List<String[]> valueSets,
                                       ExtractValidatorContext context) {
        Map<String, List<String>> paths = context.getSchemaPaths();
        List<String> dependentPath = paths.get(dependentFieldId);
        if (dependentPath == null) {
            return false;
        }

        List<List<String>> sourcePaths = new ArrayList<>(sourceIds.size());
        for (String sourceId : sourceIds) {
            List<String> sourcePath = paths.get(sourceId);
            if (sourcePath == null) {
                return false;
            }
            sourcePaths.add(sourcePath);
        }

        List<List<String>> allPaths = new ArrayList<>(sourcePaths.size() + 1);
        allPaths.add(dependentPath);
        allPaths.addAll(sourcePaths);
        int dLcaIndex = lcaIndex(allPaths);
        int sLcaIndex = lcaIndex(sourcePaths);
        if (dLcaIndex < 0 || sLcaIndex < 0) {
            return false;
        }

        String dLcaFieldId = dependentPath.get(dLcaIndex);
        FormField dLcaInstance = climbToFieldId(dependentParent, dLcaFieldId);
        if (dLcaInstance == null) {
            return false;
        }

        // All source paths agree from dLcaIndex+1 .. sLcaIndex (that's what makes them their LCA).
        // Use sourcePaths.get(0) to walk that shared segment and collect every instance reached.
        List<FormField> sLcaInstances = collectInstances(dLcaInstance, sourcePaths.get(0),
                dLcaIndex + 1, sLcaIndex);

        for (FormField anchor : sLcaInstances) {
            boolean allMatch = true;
            for (int i = 0; i < sourceIds.size(); i++) {
                String[] expected = expectedFor(valueSets, i);
                FormField source = descendFromAnchor(anchor, sourcePaths.get(i), sLcaIndex + 1);
                if (!matches(source, expected)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                return true;
            }
        }
        return false;
    }

    private static String[] expectedFor(List<String[]> valueSets, int index) {
        return (valueSets != null && index < valueSets.size()) ? valueSets.get(index) : new String[0];
    }

    private static List<FormField> findAllSources(FormField dependentParent, String dependentFieldId,
                                                  String sourceFieldId, ExtractValidatorContext context) {
        Map<String, List<String>> paths = context.getSchemaPaths();
        List<String> dependentPath = paths.get(dependentFieldId);
        List<String> sourcePath = paths.get(sourceFieldId);
        if (dependentPath == null || sourcePath == null) {
            return Collections.emptyList();
        }

        int lcaIdx = lcaIndex(List.of(dependentPath, sourcePath));
        if (lcaIdx < 0) {
            return Collections.emptyList();
        }

        String lcaFieldId = sourcePath.get(lcaIdx);
        FormField lcaInstance = climbToFieldId(dependentParent, lcaFieldId);
        if (lcaInstance == null) {
            return Collections.emptyList();
        }

        return collectInstances(lcaInstance, sourcePath, lcaIdx + 1, sourcePath.size() - 1);
    }

    private static int lcaIndex(List<List<String>> paths) {
        if (paths.isEmpty()) {
            return -1;
        }
        int limit = Integer.MAX_VALUE;
        for (List<String> p : paths) {
            limit = Math.min(limit, p.size());
        }
        int lca = -1;
        for (int i = 0; i < limit; i++) {
            String token = paths.get(0).get(i);
            boolean allMatch = true;
            for (int j = 1; j < paths.size(); j++) {
                if (!paths.get(j).get(i).equals(token)) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) {
                lca = i;
            } else {
                break;
            }
        }
        return lca;
    }

    private static FormField climbToFieldId(FormField start, String fieldId) {
        FormField current = start;
        while (current != null) {
            if (current.getProperty() != null && fieldId.equals(current.getProperty().getFieldId())) {
                return current;
            }
            FormStructure parent = current.getParent();
            current = (parent instanceof FormField parentField) ? parentField : null;
        }
        return null;
    }

    private static List<FormField> collectInstances(FormField startNode, List<String> path,
                                                    int startIdx, int endIdx) {
        if (startIdx > endIdx) {
            return List.of(startNode);
        }
        String targetId = path.get(startIdx);
        List<FormField> results = new ArrayList<>();
        for (FormField child : startNode.getChildren()) {
            if (child.getProperty() != null && targetId.equals(child.getProperty().getFieldId())) {
                results.addAll(collectInstances(child, path, startIdx + 1, endIdx));
            }
        }
        return results;
    }

    private static FormField descendFromAnchor(FormField anchor, List<String> path, int startIdx) {
        FormField current = anchor;
        for (int i = startIdx; i < path.size(); i++) {
            String stepId = path.get(i);
            FormField next = null;
            for (FormField child : current.getChildren()) {
                if (child.getProperty() != null && stepId.equals(child.getProperty().getFieldId())) {
                    next = child;
                    break;
                }
            }
            if (next == null) {
                return null;
            }
            current = next;
        }
        return current;
    }

    private static boolean matches(FormField source, String[] expected) {
        if (source == null || expected == null || expected.length == 0) {
            return false;
        }

        // **! anywhere in the set turns the whole comma-separated list into an exclusion list.
        // Mixed inclusion/exclusion in one value set should be expressed via source-id repetition.
        boolean exclusionMode = false;
        for (String token : expected) {
            if (token != null && token.trim().startsWith("**!")) {
                exclusionMode = true;
                break;
            }
        }

        boolean numericSource = isNumericSource(source);

        if (exclusionMode) {
            // Empty/absent source has no value to compare; no match (consistent with **ANY).
            if (!isPopulated(source)) {
                return false;
            }
            for (String token : expected) {
                String trimmed = token == null ? "" : token.trim();
                if (trimmed.isEmpty() || trimmed.equals("**ANY")) {
                    continue;
                }
                String inner = trimmed.startsWith("**!") ? trimmed.substring(3).trim() : trimmed;
                if (tokenMatches(source, inner, numericSource)) {
                    return false;
                }
            }
            return true;
        }

        for (String token : expected) {
            String trimmed = token == null ? "" : token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if (trimmed.equals("**ANY")) {
                if (isPopulated(source)) {
                    return true;
                }
                continue;
            }
            if (tokenMatches(source, trimmed, numericSource)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tokenMatches(FormField source, String token, boolean numericSource) {
        if (numericSource) {
            NumericPredicate pred = tryParseNumericPredicate(token);
            if (pred != null) {
                return matchesNumericPredicate(source, pred);
            }
        }
        return matchesExact(source, token);
    }

    private static boolean matchesExact(FormField source, String expected) {
        if (source instanceof SimpleField simple) {
            FieldData data = simple.getValue();
            if (data == null) {
                return false;
            }
            String value = data.getValue();
            return value != null && value.equals(expected);
        }
        return false;
    }

    private static boolean isNumericSource(FormField source) {
        if (!(source instanceof SimpleField simple)) {
            return false;
        }
        FieldProperty property = simple.getProperty();
        if (property == null) {
            return false;
        }
        FieldDataType dataType = property.getValueRestriction().getDataType();
        return dataType != null && dataType.isNumeric();
    }

    private static boolean matchesNumericPredicate(FormField source, NumericPredicate pred) {
        if (!(source instanceof SimpleField simple)) {
            return false;
        }
        FieldData data = simple.getValue();
        if (data == null) {
            return false;
        }
        String valueStr = data.getValue();
        if (StringUtils.isNullOrEmpty(valueStr)) {
            return false;
        }
        try {
            return pred.test(Double.parseDouble(valueStr));
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // Parses comparator/range tokens against numeric sources. Returns null for non-numeric tokens
    // (bare values, strings) so the caller can fall through to exact match.
    //   <X, <=X, >X, >=X     — single bound
    //   X:Y                  — inclusive range [X, Y]
    //   >X:Y, X:<Y, >X:<Y    — range with exclusive endpoints (the comparator-prefix on a side
    //                          makes that side exclusive; bare endpoint is inclusive)
    private static NumericPredicate tryParseNumericPredicate(String token) {
        if (token == null || token.isEmpty()) {
            return null;
        }

        int colonIdx = token.indexOf(':');
        if (colonIdx >= 0) {
            String leftPart = token.substring(0, colonIdx).trim();
            String rightPart = token.substring(colonIdx + 1).trim();
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

            try {
                double lower = Double.parseDouble(leftNum);
                double upper = Double.parseDouble(rightNum);
                return new NumericPredicate(lower, lowerInclusive, upper, upperInclusive);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        if (token.startsWith("<=")) {
            return parseSingleBound(token.substring(2).trim(), false, true);
        }
        if (token.startsWith(">=")) {
            return parseSingleBound(token.substring(2).trim(), true, true);
        }
        if (token.startsWith("<")) {
            return parseSingleBound(token.substring(1).trim(), false, false);
        }
        if (token.startsWith(">")) {
            return parseSingleBound(token.substring(1).trim(), true, false);
        }
        return null;
    }

    private static NumericPredicate parseSingleBound(String numStr, boolean isLower, boolean inclusive) {
        if (numStr.isEmpty()) {
            return null;
        }
        try {
            double n = Double.parseDouble(numStr);
            if (isLower) {
                return new NumericPredicate(n, inclusive, null, true);
            }
            return new NumericPredicate(null, true, n, inclusive);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static final class NumericPredicate {
        private final Double lower;
        private final boolean lowerInclusive;
        private final Double upper;
        private final boolean upperInclusive;

        NumericPredicate(Double lower, boolean lowerInclusive, Double upper, boolean upperInclusive) {
            this.lower = lower;
            this.lowerInclusive = lowerInclusive;
            this.upper = upper;
            this.upperInclusive = upperInclusive;
        }

        boolean test(double value) {
            if (lower != null) {
                if (lowerInclusive ? value < lower : value <= lower) {
                    return false;
                }
            }
            if (upper != null) {
                if (upperInclusive ? value > upper : value >= upper) {
                    return false;
                }
            }
            return true;
        }
    }

    static boolean isPopulated(FormField field) {
        if (field instanceof SimpleField simple) {
            FieldData data = simple.getValue();
            if (data == null) {
                return false;
            }
            return !StringUtils.isNullOrEmpty(data.getValue());
        }
        // Container/composite: present in tree counts as populated.
        return true;
    }
}
