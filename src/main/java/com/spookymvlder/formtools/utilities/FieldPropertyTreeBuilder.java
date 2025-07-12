package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.util.*;

public class FieldPropertyTreeBuilder {

    public static void buildFieldTree(List<FieldProperties> csvFields) {
        Map<String, FieldProperties> idToField = new HashMap<>();
        Set<FieldProperties> skippable = new HashSet<>();
        // For every field we imported, create an id to field mapping.
        for (FieldProperties field : csvFields) {
            if (idToField.containsKey(field.getFieldId())) {
                ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.FIELD_TREE_RESTRUCTURING, "FieldId not unique to import.", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldName()));
            }
            idToField.put(field.getFieldId(), field);
        }

        // For every field we imported, set its parent field and add this field as a child on that record.
        for (FieldProperties field : csvFields) {
            field.createConnections(idToField.get(field.getParentId()));
            if (field.getSkipImport()) {
                skippable.add(field);
            }
        }
        //Once connections have been established, rearrange the structure to preserve child/parent relationships.
        if (!skippable.isEmpty()) {
            for (FieldProperties skippableField : skippable) {
                skippableField.removeFieldFromTree();
                idToField.remove(skippableField.getFieldId());
            }
        }
    }
}
