package com.spookymvlder.formtools.processsor;


import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.GroupingType;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldIdentity;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldPropertyRow;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldValueRestriction;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.*;

//Doing too much in too many places, use this as a controller to coordinate fieldproperty validation
public class ProcessFieldProperties {
    static final ErrorSettings errors = ErrorSettings.getInstance();


    public static List<FieldProperty> processFieldProperties(List<FieldPropertyRow> csvFields) {
        //Initialize a wrapper to maintain the schema fields
        FieldProcessorContext context = new FieldProcessorContext();
        context.setAllRows(csvFields);

        //
        List<Runnable> validationSteps = List.of(
                //Ensure fields are formatted as expected.
                () -> convertFieldProperties(context),
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
            if (errors.checkCurrentLogOverThreshold()) {
                System.out.println("Breaking due to errors!");
                break;
            }
        }




            //TODO consider returning root instead.
        return context.getAllFields();
    }

    /**
     * Identity a duplicate fieldName could actually be confused with. FormParser resolves a property
     * to a node by walking a parent's children by element name, so two properties are only
     * indistinguishable when they share a parent. The same element name repeated under different
     * parents is normal and was previously warned about on every occurrence.
     * <p>
     * Siblings may also legitimately share an element name when each reads a different attribute of
     * it, so the value target forms part of the key. Only two siblings reading the same element name
     * AND the same part of that element are genuinely ambiguous.
     */
    private static List<String> siblingIdentityKey(FieldProperty field) {
        FieldValueRestriction value = field.getValueRestriction();
        String target = value.getAttribute() ? "@" + value.getAttributeName() : "#text";
        // A list key rather than a delimited string: no separator can be forged, and parentId is
        // null on the anchor field, which Arrays.asList tolerates and List.of would reject.
        return Arrays.asList(field.getRelation().getParentId(), field.getFieldName(), target);
    }

    public static void iterateFields(FieldProcessorContext context) {
        Map<String, FieldProperty> idToField = new HashMap<>();
        List<FieldProperty> skippableFields = new ArrayList<>();
        List<FieldProperty> inactiveFields = new ArrayList<>();
        Set<List<String>> siblingIdentities = new HashSet<>();
        List<FieldProperty> roots = new ArrayList<>();
        List<String> duplicateNames = new ArrayList<>();
        ImportSettings settings = ImportSettings.getInstance();
        List<FieldProperty> anchorFields = new ArrayList<>();
        List<FieldProperty> activeFields = new ArrayList<>();

        //Step 1 - build a map of all active fields and perform field specific validation
        for (FieldProperty field : context.getAllFields()) {
            FieldIdentity identity = field.getIdentity();

            //If inactive, do some validation, but then skip field so it doesn't end up on idToField map.
            if (!identity.getActive()){
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
            //TODO need to allow skippable field, handle constituent skippable different
            if (identity.getSkipImport()) {
                skippableFields.add(field);
            } else if (!siblingIdentities.add(siblingIdentityKey(field))) {
                duplicateNames.add(field.getFieldName());
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK,
                        "Another active field under parentId " + field.getRelation().getParentId() + " already uses "
                                + "this fieldName and reads the same part of the element, so the two cannot be told "
                                + "apart while parsing. Siblings may share a fieldName only when each reads a "
                                + "different attribute.",
                        ErrorSeverity.WARNING, field.getFieldId(), field.getFieldName()));
            }

            //TODO populate idToField at field creation to possibly make this more efficient.
            //TODO check versioning that I didn't mess this up.
            if (idToField.containsKey(field.getFieldId())) {
                errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "A field with the same ID has already been processed, this field is a duplicate. Duplicate has a name of " + idToField.get(field.getFieldId()).getFieldName() + ".", ErrorSeverity.FATAL, field.getFieldId(), field.getFieldName()));
            } else {
                idToField.put(field.getFieldId(), field);
            }
            //Ensure match to root
            if (field.getFieldId().equals(settings.getRootId())) {
                roots.add(field);
            }
            if (field.getRelation().getParentId() == null) {
                anchorFields.add(field);
            }
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
        FieldProperty anchor = context.getAnchor();
        for(FieldProperty field : context.getActiveFields()) {
            // If field is the anchor field, it won't have a parent ID. All other fields SHOULD have a parent ID.
            if (field == anchor) {
                continue;
            }
            // This should be guaranteed to not be null based on anchor check earlier in this function and null check in iterateFields().
            String parentId = field.getRelation().getParentId();
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

            if (field.getRelation().getGroupingType() == GroupingType.CONSTITUENT) {
                if (parent.getRelation().getGroupingType() != GroupingType.COMPOSITE) {
                    ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(
                            ErrorSource.FIELD_RELATIONSHIP_VALIDATION,
                            "Constituent field's parent is not a composite field.",
                            ErrorSeverity.ERROR,
                            field.getFieldId(),
                            field.getFieldName()
                    ));
                } else {
                    parent.getRelation().addConstituent(field);
                }
            }
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

        // Anything active but not reachable is an orphan
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

        for (FieldProperty child : field.getRelation().getChildFields()) {
            detectCycleAndMarkReachable(child, reachable, visiting, visited);
        }

        visiting.remove(field);
        visited.add(field);
    }

    private static void convertFieldProperties(FieldProcessorContext context) {
        List<FieldProperty> allFields = new ArrayList<>();
        for (FieldPropertyRow row : context.getAllRows()) {
            allFields.add(FieldPropertyFactory.buildFieldProperty(row));
        }
        context.setAllFields(allFields);

    }


}
