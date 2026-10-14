package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.FieldDataType;
import com.spookymvlder.formtools.datatypes.LinkLogic;
import com.spookymvlder.formtools.datatypes.TypedPredicate;
import com.spookymvlder.formtools.datatypes.TypedPredicateFactory;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldCardinality;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.FieldData;
import com.spookymvlder.formtools.datatypes.fields.FormField;
import com.spookymvlder.formtools.datatypes.fields.FormStructure;
import com.spookymvlder.formtools.datatypes.fields.SimpleField;
import com.spookymvlder.formtools.utilities.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ConditionalRequirementEvaluator {

    // Per-group ALL (requiring every instance of a shared AND-ancestor to satisfy the whole
    // per-instance condition, rather than per-source instances of one field) remains unimplemented.
    // It only has meaning for AND conditions with >1 source sharing a real repeating ancestor, and
    // was deferred in favor of the per-source axis below, which covers ordinary single-source and
    // OR-connected cases too.
    public static boolean isConditionRequired(FormField dependentParent, FieldProperty dependent,
                                              ExtractValidatorContext context) {
        FieldCardinality cardinality = dependent.getCardinality();
        List<String> sourceIds = cardinality.getCondRequiredSourceIds();
        List<String[]> valueSets = cardinality.getCondRequiredValues();
        LinkLogic logic = cardinality.getCondRequiredLogic();

        if (sourceIds == null || sourceIds.isEmpty()) {
            return false;
        }

        // AND and OR are indistinguishable for a single source, and evaluateAnd's LCA-of-one-path
        // case degenerates to that source's own position rather than a real shared ancestor (nothing
        // to compare it against), which would silently break **ALL for the common single-source rule.
        // Route it through evaluateOr explicitly rather than falling into that degenerate case.
        if (sourceIds.size() == 1 || logic == LinkLogic.OR) {
            return evaluateOr(dependentParent, dependent.getFieldId(), sourceIds, valueSets, context);
        }
        return evaluateAnd(dependentParent, dependent.getFieldId(), sourceIds, valueSets, context);
    }

    private static boolean evaluateOr(FormField dependentParent, String dependentFieldId,
                                      List<String> sourceIds, List<String[]> valueSets,
                                      ExtractValidatorContext context) {
        for (int i = 0; i < sourceIds.size(); i++) {
            String[] expected = expectedFor(valueSets, i);
            List<FormField> instances = findAllSources(dependentParent, dependentFieldId,
                    sourceIds.get(i), context);
            if (matchesSource(instances, expected)) {
                return true;
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
                List<String> sourcePath = sourcePaths.get(i);
                // collectInstances (not a single-child descent) so a source repeating below the
                // shared anchor is fully collected, not just its first occurrence.
                List<FormField> sources = collectInstances(anchor, sourcePath, sLcaIndex + 1, sourcePath.size() - 1);
                if (!matchesSource(sources, expected)) {
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
            return List.of();
        }

        int lcaIdx = lcaIndex(List.of(dependentPath, sourcePath));
        if (lcaIdx < 0) {
            return List.of();
        }

        String lcaFieldId = sourcePath.get(lcaIdx);
        FormField lcaInstance = climbToFieldId(dependentParent, lcaFieldId);
        if (lcaInstance == null) {
            return List.of();
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

    /**
     * Evaluates {@code expected} against every instance of a (possibly repeating) source field, per
     * the {@code **ALL} token: absent by default, an instance matching satisfies the whole set
     * ("any instance" — the pre-existing behavior). With {@code **ALL} present, every instance must
     * match. Zero instances never matches either way, consistent with how {@code **ANY} already
     * treats an absent field as a non-match rather than vacuously true.
     */
    private static boolean matchesSource(List<FormField> instances, String[] expected) {
        if (instances.isEmpty()) {
            return false;
        }
        boolean requireAll = containsAllModifier(expected);
        String[] valueTokens = requireAll ? stripAllModifier(expected) : expected;
        if (requireAll) {
            for (FormField instance : instances) {
                if (!matches(instance, valueTokens)) {
                    return false;
                }
            }
            return true;
        }
        for (FormField instance : instances) {
            if (matches(instance, valueTokens)) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsAllModifier(String[] expected) {
        if (expected == null) {
            return false;
        }
        for (String token : expected) {
            if (token != null && token.trim().equals("**ALL")) {
                return true;
            }
        }
        return false;
    }

    private static String[] stripAllModifier(String[] expected) {
        return Arrays.stream(expected)
                .filter(token -> token == null || !token.trim().equals("**ALL"))
                .toArray(String[]::new);
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
                if (tokenMatches(source, inner)) {
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
            if (tokenMatches(source, trimmed)) {
                return true;
            }
        }
        return false;
    }

    private static boolean tokenMatches(FormField source, String token) {
        if (source instanceof SimpleField simple) {
            FieldProperty property = simple.getProperty();
            FieldDataType dataType = property != null
                    ? property.getValueRestriction().getDataType() : null;
            TypedPredicate predicate = TypedPredicateFactory.tryParseToken(token, dataType);
            if (predicate != null) {
                FieldData data = simple.getValue();
                if (data == null) {
                    return false;
                }
                String value = data.getValue();
                if (StringUtils.isNullOrEmpty(value)) {
                    return false;
                }
                return predicate.test(value);
            }
        }
        // #N enum-index reference (ENUMERATION sources)
        if (token.length() > 1 && token.charAt(0) == '#') {
            String resolved = resolveEnumIndex(source, token);
            if (resolved == null) {
                return false;
            }
            return matchesExact(source, resolved);
        }
        return matchesExact(source, token);
    }

    private static String resolveEnumIndex(FormField source, String token) {
        if (!(source instanceof SimpleField simple)) {
            return null;
        }
        FieldProperty property = simple.getProperty();
        if (property == null) {
            return null;
        }
        List<String> enumerations = property.getValueRestriction().getEnumerations();
        if (enumerations == null || enumerations.isEmpty()) {
            return null;
        }
        int idx;
        try {
            idx = Integer.parseInt(token.substring(1).trim());
        } catch (NumberFormatException e) {
            return null;
        }
        if (idx < 1 || idx > enumerations.size()) {
            return null;
        }
        return enumerations.get(idx - 1);
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
