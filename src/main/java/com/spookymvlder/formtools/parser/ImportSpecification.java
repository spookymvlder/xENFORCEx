package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.FieldProperties;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.util.List;

public class ImportSpecification {
    private static final ParserSettings settings = ParserSettings.getInstance();

    public static void importSpec(String specPath) {
        //TODO Replace with path in settings
        List<FieldProperties> fields = ReadCSV.importBeanCSV(specPath);

        if(!ImportSettings.getInstance().getSpecImported()) {
            return;
        }
        for (FieldProperties field : fields) {
            if (field.getParentId().equals(ImportSettings.getInstance().getRootField().getFieldId())) {
                settings.addToFieldIdToName(field.getFieldId(), field.getFieldName());

            }
        }
    }
}
