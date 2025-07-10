package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;

import java.util.*;

public class FieldHierarchyValidator {
    public static List<ErrorEntry> validateHierarchy(List<FieldProperties> fields) {
        List<ErrorEntry> errors = new ArrayList<>();
        Map<String, FieldProperties> fieldMap = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Set<String> visiting = new HashSet<>();
        Set<String> roots = new HashSet<>();

        // Step 1: Build map for fast lookup
        for (FieldProperties field : fields) {
            String fieldId = field.getFieldId();
            if (fieldMap.containsKey(fieldId)) {
                errors.add(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK,"Duplicate field id.", ErrorSeverity.FATAL, field.getFieldId(), field.getAttributeName()));
            }
            fieldMap.put(fieldId, field);
        }

        // Step 2: Traverse each node and detect cycles / orphans
        for (FieldProperties field : fields) {
            String fieldId = field.getFieldId();
            //TODO confirm visited prevents infinite
            if (!visited.contains(fieldId)) {
                errors.addAll(detectCycle(field, fieldMap, visiting, visited));
            }

            // Track root nodes (optional check)
            if ("root".equals(field.getParentId())) {
                roots.add(fieldId);
            }
        }

        // Step 3: Optional sanity check
        if (roots.isEmpty()) {
            //TODO consider how not throws affects behavior.
            errors.add(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,"No root field found in schema, there must be a field where parentId = root.", ErrorSeverity.FATAL));
        }

        // Optional: Enforce only one root
        if (roots.size() > 1) {
            errors.add(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,"Multiple root fields found: " + roots, ErrorSeverity.FATAL));
        }
        return errors;
    }

    private static List<ErrorEntry> detectCycle(FieldProperties current,
                                    Map<String, FieldProperties> fieldMap,
                                    Set<String> visiting,
                                    Set<String> visited) {
        String currentId = current.getFieldId();
        List<ErrorEntry> errors = new ArrayList<>();

        if (visiting.contains(currentId)) {
            errors.add(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,"Cycle detected at fieldId: " + currentId, ErrorSeverity.FATAL));
        }

        //TODO Validate logic here
        if (visited.contains(currentId) || "root".equals(current.getParentId())) {
            return errors;
        }

        visiting.add(currentId);
        String parentId = current.getParentId();

        FieldProperties parent = fieldMap.get(parentId);
        if (parent == null) {
            String message = "Orphaned field: " + currentId + " (no parent with id " + parentId + ")";
            errors.add(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,message, ErrorSeverity.FATAL));
            return errors;
        }

        detectCycle(parent, fieldMap, visiting, visited);
        visiting.remove(currentId);
        visited.add(currentId);
        return errors;
    }
}
