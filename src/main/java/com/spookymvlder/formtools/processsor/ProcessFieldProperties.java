package com.spookymvlder.formtools.processsor;


import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.*;

//Doing too much in too many places, use this as a controller to coordinate fieldproperty validation
public class ProcessFieldProperties {
    static final ErrorSettings errors = ErrorSettings.getInstance();


    public static void processFieldProperties(List<FieldProperties> csvFields) {
        //Initialize a wrapper to maintain the schema fields
        FieldProcessorContext context = new FieldProcessorContext();
        context.setAllFields(csvFields);

        //
        List<Runnable> validationSteps = List.of(
                //Perform all actions that are field specific rather than based on the whole list.
                () -> iterateFields(context),
                //Ensure that there is a single root
                () -> GroupFieldPropertyValidator.validateSingleRoot(context),
                //Build tree structure
                () -> setFieldRelations(context),
                //*For now, retaining skippable in field tree so can build relative xpaths.
                //Remove skippable fields from tree while preserving links
                //() -> bridgeSkippable(context),
                //Remove skippable from active list and id to field map to not interfere with hierarchy check
                //() -> removeSkippableFields(context),
                () -> traverseTree(context)
        );
        for (Runnable step : validationSteps) {
            step.run();
            if (errors.getSeverityOfCurrentLog() < errors.getErrorThreshold()) {
                break;
            }
        }




            //TODO consider returning root instead.

        //TODO move someplace else
        ParserSettings settings = ParserSettings.getInstance();
        for (FieldProperties field : csvFields) {
            settings.addToFieldIdToName(field.getFieldId(), field.getFieldName());
            settings.addToFieldFromId(field.getFieldId(), field);
        }
    }

    public static void iterateFields(FieldProcessorContext context) {
        Map<String, FieldProperties> idToField = new HashMap<>();
        List<FieldProperties> skippableFields = new ArrayList<>();
        List<FieldProperties> inactiveFields = new ArrayList<>();
        List<String> fieldNames = new ArrayList<>();
        List<FieldProperties> roots = new ArrayList<>();
        List<String> duplicateNames = new ArrayList<>();


        //Step 1 - build a map of all active fields and perform field specific validation
        for (FieldProperties field : context.getAllFields()) {
            //If inactive, do some validation, but then skip field so it doesn't end up on idToField map.
            if (!field.getActive()){
                inactiveFields.add(field);
                //Field won't be going through other validation, but could have something funky with expire date.
                SingleFieldPropertyValidator.validateActiveRange(field);
                continue;
            }

            //Perform all field specific validation
            SingleFieldPropertyValidator.validateWithErrors(field);

            //Get fields being skipped to preserve links when building a tree. Also validate name while we know the field isn't being skipped.
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
            if (field.getParentId().equalsIgnoreCase("root")) {
                roots.add(field);
            }
        }

        //Remove inactive ids from list now that loop is complete
        for (FieldProperties field : inactiveFields) {
            context.removeFromActiveFields(field);
        }

        context.setIdToField(idToField);
        context.setSkippableFields(skippableFields);
        context.setInactiveFields(inactiveFields);
        context.setRootList(roots);
        context.setDuplicateNames(duplicateNames);
    }

    public static void setFieldRelations(FieldProcessorContext context) {
        List<String> inactiveIds = new ArrayList<>();
        for(FieldProperties field : context.getActiveFields()) {
            String parentId = field.getParentId();
            FieldProperties parent = context.getField(parentId);
            //Parent is null if missing from table OR if field is root. Neither needs to make connections, but if not root log error.
            if (parent == null) {
                if (!parentId.equalsIgnoreCase("root")) {
                    //Don't need inactiveIds unless this specific case comes up, so don't build it unless we need it.
                    if (inactiveIds.isEmpty()) {
                        for (FieldProperties inactive : context.getInactiveFields()) {
                            inactiveIds.add(inactive.getFieldId());
                        }
                    }
                    String message;
                    //If the id that doesn't map to an active field does belong to an inactive field, log a more specific version of the error message.
                    if (inactiveIds.contains(parentId)) {
                        message = "The parent id for this field belongs to an inactive field and does not map to a valid field property.";
                        } else {
                        message = "The parent id for the field does not map to a valid field property.";
                    }
                    ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_RELATIONSHIP_VALIDATION, message, ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
                }
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
        List<FieldProperties> unvisitedFields = context.getActiveFields();
        List<FieldProperties> visited = new ArrayList<>();
        detectCycle(context.getRoot(), unvisitedFields, visited);
        for (FieldProperties field : unvisitedFields) {
            errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "Orphaned field detected.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
        }
    }

    private static void detectCycle(FieldProperties field, List<FieldProperties> unvisitedFields, List<FieldProperties> visitedFields) {
        if (visitedFields.contains(field)) {
            errors.addToCurrentLog((new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,"Cycle detected at fieldId: " + field.getFieldId(), ErrorSeverity.FATAL)));
            return;
        }
        visitedFields.add(field);
        unvisitedFields.remove(field);
        for(FieldProperties child : field.getChildFields()) {
            detectCycle(child, unvisitedFields, visitedFields);
        }
    }
}
