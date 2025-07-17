package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class GroupFieldPropertyValidator {
    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateSingleRoot(FieldProcessorContext context) {
        //Error was fatal so we can skip out; splitting this out in case a weird multi-root thing comes up despite no plans to support that.
        if (!validateRootPresence(context)) {
            return;
        }
        List<FieldProperties> roots = context.getRootList();
        if (roots.size() != 1) {
            String message = "Multiple root fields found: " + roots.size() + ", offending fields: ";
            List<String> fieldIds = roots.stream().map(FieldProperties::getFieldId).toList();
            StringJoiner rootIds = new StringJoiner(", ", "(", ")");
            for (String id : fieldIds) {
                rootIds.add(id);
            }
            message += rootIds.toString();
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
        } else {
            context.setRoot(roots.getFirst());
        }

    }

    public static boolean validateRootPresence(FieldProcessorContext context) {
        if (context.getRootList().isEmpty()) {
            String message = "No root field found in schema, there must be a field where parentId = 'root'.";
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
            return false;
        }
        return true;
    }


}
