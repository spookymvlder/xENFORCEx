package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.FormEntry;
import com.spookymvlder.formtools.datatypes.FormField;
import com.spookymvlder.formtools.datatypes.FormSection;
import com.spookymvlder.formtools.settings.ParserSettings;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseDoc {
    private static final ParserSettings settings = ParserSettings.getInstance();
    private static XPath xpath;

    //original version
    public static List<Map<String, List<FormEntry>>> parseDocToMap(Document doc) {
        xpath = XPathFactory.newInstance().newXPath();
        return extractFormStartNode(doc);
    }

    public static void parseDoc(Document doc) {
        xpath = XPathFactory.newInstance().newXPath();
        iterateFormStartNode(doc);
    }

    //original version
    private static List<Map<String, List<FormEntry>>> extractFormStartNode(Document doc){
        //Starting element of the form's body, may repeat.
        List<Map<String, List<FormEntry>>> formContents = new ArrayList<>();
        NodeList bodies = doc.getElementsByTagName(settings.getFormStart());
        for (int i = 0; i < bodies.getLength(); i++) {
            Node body = bodies.item(i);
            try{
                formContents.add(extractSections(body));
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
        }
        return formContents;
    }

    //TODO change language to FormInstance
    private static void iterateFormStartNode(Document doc) {
        NodeList bodies = doc.getElementsByTagName(settings.getFormStart());
        for (int i = 0; i < bodies.getLength(); i++) {
            Node body = bodies.item(i);
            try{
                parseSections(body);
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
        }
    }

    //original version
    private static Map<String, List<FormEntry>> extractSections(Node body) throws XPathExpressionException {
        Map<String, List<FormEntry>> entryMap = new HashMap<>();

        //Grab sections
        for (String section : settings.getSectionOrder()) {
            NodeList headerList = (NodeList) xpath.evaluate(settings.getFormFieldToPath().get(section), body, XPathConstants.NODESET);
            //For each section, iterate over each instance of that section
            for (int j = 0; j < headerList.getLength(); j++) {
                Node headerNode = headerList.item(j);

                String foreignKey = ((Element) headerNode).getAttribute(settings.getForeignKey());
                //Within a section occurrence, iterate over the fields in that section
                for (String value : settings.getFormSectionToFields().get(section)) {
                    //Get path for field
                    String entryValue = safeEvaluate(headerNode, settings.getFormFieldToPath().get(value));
                    //Add to map, appending it if the field already has an entry
                    entryMap.computeIfAbsent(value, k -> new ArrayList<>())
                            .add(new FormEntry(entryValue, foreignKey));
                }
            }

        }
        //Validate for one driver, then build before restarting.
        //ValidateFormExtract.validateExtractMap(entryMap);
        return entryMap;
    }

    //TODO, make nomenclature clear. Define rootSections as those whose only parent is the form instance.
    private static List<FormSection> parseSections(Node body) throws XPathExpressionException {
        List<FormSection> rootSections = new ArrayList<>();

        //Might not need custom getter here, should be able to iterate over the body node's children elements.
        for(String rootSection : settings.getRootSections()) {
            FormSection root = parseSection(body, rootSection);
            rootSections.add(root);
        }
        return rootSections;
        /*List<FormSection> storedSections = new ArrayList<>();

        //Grab sections
        for (String sectionHeader : settings.getSectionOrder()) {
            NodeList headerList = (NodeList) xpath.evaluate(settings.getFormFieldToPath().get(sectionHeader), body, XPathConstants.NODESET);
            //For each section, iterate over each instance of that section
            for (int j = 0; j < headerList.getLength(); j++) {
                Node headerOccurrence = headerList.item(j);

                String foreignKey = ((Element) headerOccurrence).getAttribute(settings.getForeignKey());

                //Initialize instance of section data type
                FormSection workingSection = new FormSection(sectionHeader);
                storedSections.add(workingSection);

                //Within a section occurrence, iterate over the fields in that section
                for (String fieldName : settings.getFormSectionToFields().get(sectionHeader)) {
                    //Get path for field
                    String entryValue = safeEvaluate(headerOccurrence, settings.getFormFieldToPath().get(fieldName));
                    FormField field = new FormField(entryValue, foreignKey, fieldName);
                    workingSection.addFields(fieldName, field);
                }
            }

        }
        //Validate for one driver, then build before restarting.
        ValidateFormExtract.validateExtractMap(storedSections);*/
    }

    //ChatGPT helped a lot here.
    private static FormSection parseSection(Node contextNode, String sectionName) throws XPathExpressionException {
        FormSection section = new FormSection(sectionName);

        NodeList occurrences = (NodeList) xpath.evaluate(
                settings.getFormFieldToPath().get(sectionName),
                contextNode,
                XPathConstants.NODESET
        );

        for (int i = 0; i < occurrences.getLength(); i++) {
            Node instance = occurrences.item(i);
            //TODO deprecate foreign key
            String foreignKey = ((Element) instance).getAttribute(settings.getForeignKey());

            // Add fields
            for (String fieldName : settings.getFormSectionToFields().get(sectionName)) {
                String fieldValue = safeEvaluate(instance, settings.getFormFieldToPath().get(fieldName));
                FormField formField = new FormField(fieldValue, foreignKey, fieldName);
                section.addFields(fieldName, formField);
            }

            // Parse child sections of this occurrence
            List<FormSection> subsections = new ArrayList<>();
            List<String> subsectionList = settings.getSubsectionsOf(sectionName);
            for (String subName : subsectionList) {
                FormSection subsection = parseSection(instance, subName);
                subsections.add(subsection);
            }
            section.setSubSections(subsections);
        }

        return section;
    }



    //Chat GPT
    public static String safeEvaluate(Node context, String path) {
        try {
            String result = (String) xpath.evaluate(path, context, XPathConstants.STRING);
            return result != null ? result.trim() : "";
        } catch (XPathExpressionException e) {
            //TODO review behavior.
            return "";
        }
    }


}
