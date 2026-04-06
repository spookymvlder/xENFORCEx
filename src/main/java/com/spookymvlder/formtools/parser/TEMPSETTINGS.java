package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.settings.ImportSettings;

import java.util.*;

public class TEMPSETTINGS {

    private static final ImportSettings settings = ImportSettings.getInstance();

    public static void setTestSettings() {


        settings.setXmlFilePath("src/main/resources/local-only/condensedSampleDriver.xml");
        settings.setXsdFilePath("src/main/resources/local-only/sampleDriverSchema.xsd");
        settings.setSpecFilePath("src/main/resources/Documentation/field_property_template_driver_short.csv");
        settings.setAnchorElementName("application");
        settings.setAnchorElementIndex(0);
        settings.setRootId("1");

        settings.setTestIgnoreXSD(true);


    }
}
