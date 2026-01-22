package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FieldProperty;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;

import java.util.List;
import java.util.StringJoiner;

public class GroupFieldPropertyValidator {
    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateWithErrors(FieldProcessorContext context) {
        validateSingleAnchorElement(context);
        validateSingleRoot(context);
    }

    public static void validateSingleAnchorElement(FieldProcessorContext context) {
        //Error was fatal so we can skip out;
        if (!validateAnchorPresence(context)) {
            return;
        }
        List<FieldProperty> anchors = context.getAnchorList();
        if (anchors.size() > 1) {
            String message = "Multiple fields representing anchor elements found " + anchors.size() + ", offending fields: ";
            List<String> fieldIds = anchors.stream().map(FieldProperty::getFieldId).toList();
            StringJoiner anchorIds = new StringJoiner(", ", "(", ")");
            for (String fieldId : fieldIds) {
                anchorIds.add(fieldId);
            }
            message += anchorIds.toString();
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
        } else {
            context.setAnchor(anchors.getFirst());
            ImportSettings.getInstance().setAnchorElementName(anchors.getFirst().getFieldName());
        }
    }

    public static void validateSingleRoot(FieldProcessorContext context) {
        //Error was fatal so we can skip out; splitting this out in case a weird multi-root thing comes up despite no plans to support that.
        if (!validateRootPresence(context)) {
            return;
        }
        List<FieldProperty> roots = context.getRootList();
        if (roots.size() != 1) {
            String message = "Multiple root fields found: " + roots.size() + ", offending fields: ";
            List<String> fieldIds = roots.stream().map(FieldProperty::getFieldId).toList();
            StringJoiner rootIds = new StringJoiner(", ", "(", ")");
            for (String id : fieldIds) {
                rootIds.add(id);
            }
            message += rootIds.toString();
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
        } else {
            context.setRoot(roots.getFirst());
            ImportSettings.getInstance().setRootField(roots.getFirst());
        }

    }

    public static boolean validateRootPresence(FieldProcessorContext context) {
        if (context.getRootList().isEmpty()) {
            String message = "No field found that matches the fieldId set in Import Settings.";
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
            return false;
        }
        return true;
    }

    public static boolean validateAnchorPresence(FieldProcessorContext context) {
        if (context.getAnchorList().isEmpty()) {
            String message = "Unable to find an anchor element to start the import. This top level node should not have a parent listed for parentId in the import spec.";
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.SCHEME_HIERARCHY_CHECK, message, ErrorSeverity.FATAL));
            return false;
        }
        return true;
    }


}
