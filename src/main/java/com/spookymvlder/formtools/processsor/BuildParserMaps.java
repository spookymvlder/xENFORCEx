package com.spookymvlder.formtools.processsor;

import com.spookymvlder.formtools.datatypes.FieldProperty;
import com.spookymvlder.formtools.settings.ParserSettings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BuildParserMaps {

    private static final ParserSettings settings = ParserSettings.getInstance();

    public static void buildParserMaps(FieldProperty field) {

    }

    public static void buildTopSections(FieldProcessorContext context) {
        FieldProperty root = context.getRoot();
        //TODO need to check for duplicate names at some point to see how specific relative paths need to be.
        List<FieldProperty> topFieldSections = new ArrayList<>();
        //Save the field Ids to a list in order of the get order field.
        //TODO error handling if field order is empty. May need to convert from set to list.
        topFieldSections = root.getChildFields().stream().sorted(Comparator.comparingInt(FieldProperty::getOrder)).toList();
        settings.setTopFieldSections(topFieldSections);
    }

    //Decision point - either process all of this as Field Properties, or allow for manual setting and then convert it, or convert field properties to strings
    public static void buildSections(FieldProcessorContext context) {
        FieldProperty root = context.getRoot();

    }
}
