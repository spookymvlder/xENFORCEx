package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.FormField;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.datatypes.FormSection;
import com.spookymvlder.formtools.settings.ImportSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;

public class ParseDoc {
    private static final ParserSettings settings = ParserSettings.getInstance();
    private static XPath xpath;

    public static void parseDoc(Document doc) {
        xpath = XPathFactory.newInstance().newXPath();
        iterateFormInstance(doc);
    }

    //TODO change language to FormInstance
    private static void iterateFormInstance(Document doc) {
        List<FormInstance> parsedInstances = new ArrayList<>();
        NodeList instanceList = doc.getElementsByTagName(settings.getFormStart());
        //TODO make code below work with a single settings rather than essentially grabbing the same thing from two spots.
        //NodeList instanceList = doc.getElementsByTagName((ImportSettings.getInstance().getFormRoot()));
        for (int i = 0; i < instanceList.getLength(); i++) {
            Node workingInstance = instanceList.item(i);
            try{
                parsedInstances.add(new FormInstance(workingInstance.getNodeName(), parseSections(workingInstance)));
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
        }
    }

    //TODO, make nomenclature clear. Define rootSections as those whose only parent is the form instance.
    private static List<FormSection> parseSections(Node instance) throws XPathExpressionException {
        List<FormSection> rootSections = new ArrayList<>();

        //Might not need custom getter here, should be able to iterate over the body node's children elements.
        for(String rootSection : settings.getRootSections()) {
            FormSection root = parseSection(instance, rootSection);
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
