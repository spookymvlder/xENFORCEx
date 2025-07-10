package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.settings.ImportSettings;

import java.util.List;

public class ImportSpecification {

    public static void importSpec(String specPath) {
        //TODO Replace with path in settings
        List<FieldProperties> fields = ReadCSV.importBeanCSV(specPath);

        if(!ImportSettings.getInstance().getSpecImported()) {
            return;
        }

    }
}
