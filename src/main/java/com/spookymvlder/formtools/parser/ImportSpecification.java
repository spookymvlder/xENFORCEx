package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.FieldProperty;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.util.List;

public class ImportSpecification {
    private static final ParserSettings settings = ParserSettings.getInstance();

    public static void importSpec(String specPath) {
        //TODO Replace with path in settings
        List<FieldProperty> fields = ReadCSV.importBeanCSV(specPath);

        //Avoid double import, TODO add a hash somewhere/how
        if(!ImportSettings.getInstance().getSpecImported()) {
            return;
        }

        /*
        // Unclear what the plan was here
        for (FieldProperty field : fields) {
            if (field.getParentId().equals(ImportSettings.getInstance().getRootField().getFieldId())) {
                settings.addToFieldIdToName(field.getFieldId(), field.getFieldName());

            }
        }
        */
    }
}
