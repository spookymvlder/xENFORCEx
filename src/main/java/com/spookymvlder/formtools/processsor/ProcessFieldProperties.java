package com.spookymvlder.formtools.processsor;


import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
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


    public static void processFieldProperties(List<FieldProperties> csvFields) {
        FieldProcessorContext context = new FieldProcessorContext();
        context.setAllFields(csvFields);


        //Treat these as class variables to modularize?
        Map<String, FieldProperties> idToField = new HashMap<>();
        List<FieldProperties> skippableFields = new ArrayList<>();
        List<String> inactiveIds = new ArrayList<>();
        FieldProperties root = null;

        //Perform all actions that are field specific rather than based on the whole list.
        iterateFields(context);




        if (root == null) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK,"No root field found in schema, there must be a field where parentId = 'root'.", ErrorSeverity.FATAL));
        } else {
            //TODO consider returning root instead.
            ImportSettings.getInstance().setRootField(root);
        }


        //Build tree structure and then bridge the gap for any fields being skipped.
        setFieldRelations(idToField, inactiveIds);
        bridgeSkippable(idToField, skippableFields);
        //Now that we don't need skippable fields, remove them from list so they won't interfere with hierarchy check.
        for (FieldProperties field : skippableFields) {
            csvFields.remove(field);
        }
        //Commenting this out for now as a reminder that it needs done, but this is not the place for it.
        /*
        //Only if we added something during validation.
        //TODO remove this code and make it accessible via CLI log or error log
        if (successCheck && !errors.isEmpty()) {
            //TODO add check for fatal as well.
            successCheck = errors.stream().allMatch(e -> e.getSeverity() == ErrorSeverity.ERROR);

            System.out.println("Validation errors found.");
            for (ErrorEntry error : errors) {
                System.out.println("   -" + error);
            }
        }*/
        traverseTree(root, csvFields);
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

        //Step 1 - build a map of all active fields and perform field specific validation
        for (FieldProperties field : context.getAllFields()) {
            //If inactive, do some validation, but then skip field so it doesn't end up on idToField map.
            if (!field.getActive()){
                inactiveFields.add(field);
                //Field won't be going through other validation, but could have something funky with expire date.
                FieldPropertyValidator.validateActiveRange(field);
                continue;
            }

            //Perform all field specific validation
            FieldPropertyValidator.validateWithErrors(field);

            //Get fields being skipped to preserve links when building a tree. Also validate name while we know the field isn't being skipped.
            if (field.getSkipImport()) {
                skippableFields.add(field);
            } else if (fieldNames.contains(field.getFieldName())) {
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
            /*
            if (field.getParentId().equalsIgnoreCase("root")) {
                if (root != null) {
                    errors.addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "Attempting to set this field as root after another root with id " + root.getFieldId() + " has been found .", ErrorSeverity.FATAL, field.getFieldId(), field.getFieldName()));
                } else {
                    root = field;
                }
            }*/
        }

        //Remove inactive ids from list now that loop is complete
        for (FieldProperties field : inactiveFields) {
            context.removeFromActiveFields(field);
        }

        context.setIdToField(idToField);
        context.setSkippableFields(skippableFields);
        context.setInactiveFields(inactiveFields);
        context.setRootList(roots);
    }

    public static void setFieldRelations(Map<String, FieldProperties> idToField, List<String> inactiveIds) {
        for(FieldProperties field : idToField.values()) {
            String parentId = field.getParentId();
            FieldProperties parent = idToField.get(parentId);
            //Parent is null if missing from table OR if field is root. Neither needs to make connections, but if not root log error.
            if (parent == null) {
                if (!parentId.equalsIgnoreCase("root")) {
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

    public static void bridgeSkippable(Map<String, FieldProperties> idToField, List<FieldProperties> skippableFields) {
        for (FieldProperties skipField : skippableFields) {
            skipField.removeFieldFromTree();
            idToField.remove(skipField.getFieldId());
        }
    }

    public static void traverseTree(FieldProperties root, List<FieldProperties> unvisitedFields) {
        List<FieldProperties> visited = new ArrayList<>();
        detectCycle(root, unvisitedFields, visited);
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
