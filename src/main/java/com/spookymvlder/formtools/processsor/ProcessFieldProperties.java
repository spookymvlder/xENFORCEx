package com.spookymvlder.formtools.processsor;


import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperty;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.*;

//Doing too much in too many places, use this as a controller to coordinate fieldproperty validation
public class ProcessFieldProperties {
    static final ErrorSettings errors = ErrorSettings.getInstance();


    public static void processFieldProperties(List<FieldProperty> csvFields) {
        //Initialize a wrapper to maintain the schema fields
        FieldProcessorContext context = new FieldProcessorContext();
        context.setAllFields(csvFields);

        //
        List<Runnable> validationSteps = List.of(
                //Ensure fields are formatted as expected.
                () -> normalizeAllFields(context),
                //Perform all actions that are field specific rather than based on the whole list.
                () -> iterateFields(context),
                //Ensure that there is a single root and anchor element
                () -> GroupFieldPropertyValidator.validateWithErrors(context),
                //Build tree structure
                () -> setFieldRelations(context),
                //TODO *For now, retaining skippable in field tree so can build relative xpaths.
                //Remove skippable fields from tree while preserving links
                //() -> bridgeSkippable(context),
                //Remove skippable from active list and id to field map to not interfere with hierarchy check
                //() -> removeSkippableFields(context),
                () -> traverseTree(context)
        );
        for (Runnable step : validationSteps) {
            step.run();
            if (errors.getSeverityOfCurrentLog() >= errors.getErrorThreshold()) {
                System.out.println("Breaking due to errors!");
                break;
            }
        }




            //TODO consider returning root instead.

        //TODO move someplace else
        ParserSettings settings = ParserSettings.getInstance();
        for (FieldProperty field : csvFields) {
            settings.addToFieldIdToName(field.getFieldId(), field.getFieldName());
            settings.addToFieldFromId(field.getFieldId(), field);
        }
    }

    public static void iterateFields(FieldProcessorContext context) {
        Map<String, FieldProperty> idToField = new HashMap<>();
        List<FieldProperty> skippableFields = new ArrayList<>();
        List<FieldProperty> inactiveFields = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        List<FieldProperty> roots = new ArrayList<>();
        List<String> duplicateNames = new ArrayList<>();
        ImportSettings settings = ImportSettings.getInstance();
        List<FieldProperty> anchorFields = new ArrayList<>();
        List<FieldProperty> activeFields = new ArrayList<>();

        //TODO - duplicate name checking should be no name is a descendant of the same name
        //Step 1 - build a map of all active fields and perform field specific validation
        for (FieldProperty field : context.getAllFields()) {
            //If inactive, do some validation, but then skip field so it doesn't end up on idToField map.
            if (!field.getActive()){
                inactiveFields.add(field);
                //Field won't be going through other validation, but could have something funky with expire date.
                SingleFieldPropertyValidator.validateActiveRange(field);
                continue;
            } else {
                activeFields.add(field);
            }

            //Perform all field specific validation
            SingleFieldPropertyValidator.validateWithErrors(field);

            //Get fields being skipped to preserve links when building a tree. Also validate name while we know the field isn't being skipped.
            //TODO need to allow skippable field, handle constituent skippable different, and treat duplicate name differently if part of grouping
            if (field.getSkipImport()) {
                skippableFields.add(field);
            } else if (fieldNames.contains(field.getFieldName())) {
                duplicateNames.add(field.getFieldName());
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "Multiple active fields with the same name exist, which may cause unintended behavior.", ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
            } else {
                fieldNames.add(field.getFieldName());
            }

            //TODO populate idToField at field creation to possibly make this more efficient.
            if (idToField.containsKey(field.getFieldId())) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "A field with the same ID has already been processed, this field is a duplicate. Duplicate has a name of " + idToField.get(field.getFieldId()).getFieldName() + ".", ErrorSeverity.FATAL, field.getFieldId(), field.getFieldName()));
            } else {
                idToField.put(field.getFieldId(), field);
            }
            //Ensure match to root
            if (field.getFieldId().equals(settings.getRootId())) {
                roots.add(field);
            }
            if (field.getParentId() == null) {
                anchorFields.add(field);
            }
        }

        //Remove inactive ids from list now that loop is complete
        for (FieldProperty field : inactiveFields) {
            context.removeFromActiveFields(field);
        }
        context.setActiveFields(activeFields);
        context.setIdToField(idToField);
        context.setSkippableFields(skippableFields);
        context.setInactiveFields(inactiveFields);
        context.setRootList(roots);
        context.setDuplicateNames(duplicateNames);
        context.setAnchorList(anchorFields);
    }

    public static void setFieldRelations(FieldProcessorContext context) {
        List<String> inactiveIds = new ArrayList<>();
        FieldProperty anchor = context.getAnchor();
        for(FieldProperty field : context.getActiveFields()) {
            // If field is the anchor field, it won't have a parent ID. All other fields SHOULD have a parent ID.
            if (field == anchor) {
                continue;
            }
            // This should be guaranteed to not be null based on anchor check earlier in this function and null check in iterateFields().
            String parentId = field.getParentId();
            FieldProperty parent = context.getField(parentId);
            //Parent is null if missing from table, need to log error.
            if (parent == null) {
                String message;
                if (parentId == null) {
                    message = "The parentId is empty ";
                } else {
                    message = "The parentId " + parentId + " does not map to a valid field property ";
                }
                message += "for fieldId " + field.getFieldId();
                ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_RELATIONSHIP_VALIDATION, message, ErrorSeverity.FATAL, field.getFieldId(), field.getFieldName()));
                continue;
            }
            if (!context.getActiveFields().contains(parent)) {
                String message = "The parent id for this field belongs to an inactive field and does not map to a valid field property.";
                ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_RELATIONSHIP_VALIDATION, message, ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
                continue;
            }
            field.createConnections(parent);
        }
    }
/*
    public static void bridgeSkippable(FieldProcessorContext context) {
        for (FieldProperties skipField : context.getSkippableFields()) {
            skipField.removeFieldFromTree();
        }
    }

    public static void removeSkippableFields(FieldProcessorContext context) {
        for (FieldProperties skipField : context.getSkippableFields()) {
            context.removeIdFromMap(skipField.getFieldId());
            context.removeFromActiveFields(skipField);
        }
    }
*/
public static void traverseTree(FieldProcessorContext context) {
    // Track which active fields were reachable from root
    Set<FieldProperty> reachable = new HashSet<>();

    // 3-color sets
    Set<FieldProperty> visiting = new HashSet<>(); // GRAY
    Set<FieldProperty> visited  = new HashSet<>(); // BLACK

    detectCycleAndMarkReachable(context.getRoot(), reachable, visiting, visited);

    // Anything active but not reachable is anf orphan
    for (FieldProperty field : context.getActiveFields()) {
        //TODO ensure that any list lookup is based on ID to avoid hashing error.
        if (!reachable.contains(field) && !context.getAnchorList().contains(field)) {
            errors.addToCurrentLog(new FieldPropertyValidationError(
                    ErrorSource.SCHEME_HIERARCHY_CHECK,
                    "Orphaned field detected.",
                    ErrorSeverity.ERROR,
                    field.getFieldId(),
                    field.getFieldName()
            ));
        }
    }
}

    private static void detectCycleAndMarkReachable(
            FieldProperty field,
            Set<FieldProperty> reachable,
            Set<FieldProperty> visiting,
            Set<FieldProperty> visited
    ) {
        if (field == null) return;

        // Already fully processed: no cycle, no need to recurse again
        if (visited.contains(field)) {
            return;
        }

        // Back-edge to a node in the current recursion stack => cycle
        if (visiting.contains(field)) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.SCHEME_HIERARCHY_CHECK,
                    "Cycle detected at fieldId: " + field.getFieldId(),
                    ErrorSeverity.FATAL
            ));
            return;
        }

        visiting.add(field);
        reachable.add(field);

        for (FieldProperty child : field.getChildFields()) {
            detectCycleAndMarkReachable(child, reachable, visiting, visited);
        }

        visiting.remove(field);
        visited.add(field);
    }

    // At the moment just ensure we're storing null for empty strings instead of "" via opencsv or other import method.
    private static void normalizeAllFields(FieldProcessorContext context) {
        for (FieldProperty field : context.getAllFields()) {
            normalizeField(field);
        }
    }

    private static void normalizeField(FieldProperty field) {
        field.setParentId(normalizeBlank(field.getParentId()));
        field.setFieldName(normalizeBlank(field.getFieldName()));
        field.setDisplayName(normalizeBlank(field.getDisplayName()));
        field.setSectionHeaderOverride(normalizeBlank(field.getSectionHeaderOverride()));
        // TODO handle list strings (condRepeatedValues, ...)
        field.setCodeSystemName(normalizeBlank(field.getCodeSystemName()));
        field.setCodeSystemVersion(normalizeBlank(field.getCodeSystemVersion()));
        field.setCodeSystemId(normalizeBlank(field.getCodeSystemId()));
        field.setCodeSystemCode(normalizeBlank(field.getCodeSystemCode()));
        field.setDataType(normalizeBlank(field.getDataType()));
        field.setUnit(normalizeBlank(field.getUnit()));
        field.setRegexPattern(normalizeBlank(field.getRegexPattern()));
        field.setAttributeName(field.getAttributeName());
        field.setUiControl(normalizeBlank(field.getUiControl()));
        field.setTemplatePath(normalizeBlank(field.getTemplatePath()));

    }

    // Convert empty strings to nulls. Empty strings created automatically on import.
    private static String normalizeBlank(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
