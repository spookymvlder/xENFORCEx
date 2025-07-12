package com.spookymvlder.formtools.utilities;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.datatypes.FieldPropertyValidationError;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RemoveInvalidFields {

    public static List<FieldProperties> removeInvalid(List<FieldProperties> fields) {
        List<String> removedIds = new ArrayList<>();
        List<FieldProperties> skipped = new ArrayList<>();
        for (FieldProperties field : fields) {
            if (!field.getActive()) {
                //TODO setting for effective relative date variable
                if (field.getExpireDate().isBefore(LocalDate.now())) {
                    removedIds.add(field.getFieldId());
                    fields.remove(field);
                }
            }
            if (field.getSkipImport()) {
                skipped.add(field);
                fields.remove(field);
            }
        }
        shiftSkippedFields(skipped);
        //TODO if skipImport, bubble up children of skip import
        if (!removedIds.isEmpty()) {
            for (FieldProperties field : fields) {
                if (removedIds.contains(field.getParentId())) {
                    ErrorSettings.getInstance().addToCurrentLog(new FieldPropertyValidationError(ErrorSource.SCHEME_HIERARCHY_CHECK, "The field has an inactive parent, inactive parent id: " + field.getParentId() + ".", ErrorSeverity.ERROR, field.getFieldId(), field.getFieldId()));
                }
            }
        }
        return fields;
    }

    public static void shiftSkippedFields(List<FieldProperties> fields) {
        for (FieldProperties field : fields) {
            List<String> childIds = ParserSettings.getInstance().getFormFieldsForSection(field.getFieldId());
            for (String childId : childIds) {
                //TODO WIP - seems like a lot of this will be easier with a tree.
            }
        }
    }
}
