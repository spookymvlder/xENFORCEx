package com.spookymvlder.formtools.validator;

import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.settings.ErrorSettings;

import java.util.List;

public class ValidateFormExtract {

    static final ErrorSettings errors = ErrorSettings.getInstance();

    public static void validateForm(List<FormInstance> instances){
        for (FormInstance instance : instances){
            // Build map of fieldId to occurrences
            // For each field, run all rules from field property, use map to lookup related fields.
        }
    }
}
