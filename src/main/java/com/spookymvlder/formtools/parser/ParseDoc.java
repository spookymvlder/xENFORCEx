package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.*;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ParserSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.*;

public class ParseDoc {
    private static final ParserSettings settings = ParserSettings.getInstance();
    static final ErrorSettings errors = ErrorSettings.getInstance();
    private static XPath xpath;

    public static void parseDoc(Document doc) {
        xpath = XPathFactory.newInstance().newXPath();
        List<FormInstance> forms = buildFormInstances(doc);
        System.out.println(forms);
    }

    //TODO change language to FormInstance
    //Containers each holding a separate completed form, if multiple exist.
    private static List<FormInstance> buildFormInstances(Document doc) {
        List<FormInstance> parsedInstances = new ArrayList<>();
        NodeList instanceList = doc.getElementsByTagName(settings.getFormStart());
        //TODO make code below work with a single settings rather than essentially grabbing the same thing from two spots.
        //NodeList instanceList = doc.getElementsByTagName((ImportSettings.getInstance().getFormRoot()));
        for (int i = 0; i < instanceList.getLength(); i++) {
            Node workingInstance = instanceList.item(i);
            try{
                //parsedInstances.add(new FormInstance(workingInstance.getNodeName(), buildTopSections(workingInstance)));
                parsedInstances.add(new FormInstance(workingInstance.getNodeName(), topTest(workingInstance)));
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
            //System.out.println("DEBUG");
        }
        return parsedInstances;
    }

    //TODO, make nomenclature clear. Define topSections as those whose only parent is the form instance.
    //TODO investigate what happens if top sections repeat within a form instance.
    private static List<FormSection> buildTopSections(Node instance) throws XPathExpressionException {
        List<FormSection> topSections = new ArrayList<>();

        //Might not need custom getter here, should be able to iterate over the body node's children elements.
        for(String sectionName : settings.getTopLevelSections()) {
            //System.out.println("**New top section: " + sectionName);

            //TODO issue seems to be an assumption here that top section is not repeated.
            FormSection topSection = buildSection(instance, sectionName);


            topSections.add(topSection);
            //System.out.println("DEBUG");
        }
        //System.out.println("DEBUG");
        return topSections;
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

    private static List<FormSection> topTest(Node instance) throws XPathExpressionException {
        List<FormSection> topSections = new ArrayList<>();
        Node start = instance.getFirstChild();
        while(start != null) {
            String sectionName = start.getNodeName();
            if (settings.getTopLevelSections().contains(sectionName)) {
                FormSection topSection = testBuildSection(start, sectionName);
                topSections.add(topSection);
            }
            start = start.getNextSibling();
        }


        return topSections;
    }

    private static FormSection testBuildSection(Node contextNode, String sectionName) throws XPathExpressionException {
        FormSection section = new FormSection (sectionName);
        buildFields(section, sectionName, contextNode);

        if (settings.getSubsectionsOf(sectionName) != null) {
            List<FormSection> subsections = new ArrayList<>();
            List<String> subsectionList = settings.getSubsectionsOf(sectionName);

            for (String subName : subsectionList) {
                NodeList occurrences = (NodeList) xpath.evaluate(
                        settings.getFormFieldToPath().get(subName),
                        contextNode,
                        XPathConstants.NODESET);

                for (int i = 0; i < occurrences.getLength(); i++) {
                    Node occurrence = occurrences.item(i);
                    FormSection subsection = testBuildSection(occurrence, subName);
                    subsections.add(subsection);
                }
                System.out.println(subName);

            }
            //TODO make it possible to find subsection by name. May run in to weird recursion scenarios with bad build.
            section.setSubSections(subsections);
        }

        return section;
    }

    //Get the root property and also the root of the document, then get form start of each to pass along.
    private static List<FormInstance> buildTempFormInstances(Document doc) throws XPathExpressionException {
        List<FormInstance> parsedInstances = new ArrayList<>();


        //TODO double check form start field is set correctly, may not be root.
        //TODO ensure that form start field does not have siblings except recurrences of itself.
        //Starting field if not root of tree.
        FieldProperty startProperty = settings.getFormStartField();
        NodeList instanceList;

        if (startProperty.getXpathOverride() != null) {
            instanceList = (NodeList) xpath.evaluate(startProperty.getXpathOverride(), doc, XPathConstants.NODESET);
        } else {
            instanceList = doc.getElementsByTagName(startProperty.getFieldName());
        }

        for (int i = 0; i < instanceList.getLength(); i++) {
            Node workingInstance = instanceList.item(i);
            try{
                FormInstance iteration = new FormInstance(workingInstance.getNodeName(), parseStartNode(workingInstance, startProperty, i));
                iteration.getStartField().setParent(iteration);
                parsedInstances.add(iteration);
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
        }
        return parsedInstances;
    }

    //Given a starting point and a tree, build sections and fields based on what the fieldProperties tree says should be present.
    //Instance should be form start
    private static SimpleField parseStartNode(Node context, FieldProperty startProperty, int position) throws XPathExpressionException {
        String name = startProperty.getDisplayName();
        //TODO - dedicated start node class that doesn't have data.
        //TODO - validate that start node is not skipped as part of validation.
        FieldData data = new FieldData("root", "root",0);
        String path = startProperty.getRelativePath() + "[" + position + "]";
        SimpleField start = new SimpleField (name, startProperty, data);
        start.setRetrievedPath(path);
        start.setPosition(position);
        parseChildren(context, startProperty, "", start);

        return start;
    }

    //TODO refactor
    private static void parseNode(Node contextNode, FieldProperty property, String parentLabel, FormField<?> parent) throws XPathExpressionException {
        Node newContextNode = (Node) safeEvaluateNode(contextNode, property.getRelativePath());
        if (property.getSkipImport()) {
            parseChildren(newContextNode, property, parentLabel, parent);
            return;
        }
        String label = parentLabel;
        if (!parentLabel.isEmpty()) {
            label += " ";
        }

        String path = property.getRelativePath();

        if (property.getDataType().equals("label")) {
            label = safeEvaluate(contextNode, path);
            parseChildren(newContextNode, property, label, parent);
            return;
        }


        FormField<?> field;
        switch(property.getGroupingType()) {
            case GroupingType.CONSTITUENT:
                errors.addToCurrentLog(new ParserError(ErrorSource.PARSE_ERROR, "Error navigating document tree while parsing, encountered constituent field property, schema may not have processed correctly.",
                        ErrorSeverity.ERROR, newContextNode.getNodeName()));
                return;
            case GroupingType.COMPOSITE:
                Map<String, FieldData> data = buildConstituents(newContextNode, property);
                field = new CompositeField(property.getDisplayName(), property, parent, data);
                break;
            //Should be skipping constituent as handled through composite.
            default:
                field = new SimpleField(property.getDisplayName(), property, parent, buildFieldData(newContextNode, property));
                break;
        }

        //Label should only persist to first non-label level.
        if (!label.isEmpty()) {
            field.setLabel(label);
        }
        parent.addChild(field);
        label = "";
        parseChildren(newContextNode, property, label, field);

    }

    //Want to get the version of this node from the root of the form
    private static int findNodePosition(Node parent, Node child) {
        NodeList nodeList = parent.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.isSameNode(child)) {
                return i;
            }
        }
        //Node not found
        return -1;
    }

    private static void parseChildren(Node contextNode, FieldProperty property, String parentLabel, FormField parent) throws XPathExpressionException {
        for (int i = 0; i < contextNode.getChildNodes().getLength(); i++) {
            Node child = contextNode.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String name = child.getNodeName();
                FieldProperty prop = property.getChildField(name);
                if (prop != null) {
                    parseNode(child, prop, label, parentField);
                }
            }
        }
        for (FieldProperty prop : property.getChildFields()) {
            parseNode(contextNode, prop, parentLabel, parent);
        }
    }

    private static Map<String, FieldData> buildConstituents(Node contextNode, FieldProperty property) {
        Map<String, FieldData> constituents = new HashMap<>();
        //TODO consider error instead?
        if (property.getGroupingType() == GroupingType.COMPOSITE) {
            int counter = 0;
            for (FieldProperty prop : property.getConstituents()) {
                String value = safeEvaluate(contextNode, prop.getRelativePath());
                String type = prop.getDataType();
                constituents.put(property.getDisplayName(), new FieldData(value, type, counter));
                counter++;
            }
        }

        return constituents;
    }

    private static FieldData buildFieldData(Node contextNode, FieldProperty property) {
        String value = safeEvaluate(contextNode, property.getRelativePath());
        String type = property.getDataType();
        return new FieldData(value, type, 0);
    }

    private static FormSection buildSection(Node contextNode, String sectionName) throws XPathExpressionException {
        FormSection section = new FormSection(sectionName);


        //Temporary workaround for supporting test code.
        //TODO update once settings code is ironed out.
        NodeList occurrences = (NodeList) xpath.evaluate(
                settings.getFormFieldToPath().get(sectionName),
                contextNode,
                XPathConstants.NODESET);

        //TODO problem might be here? Length 5
        System.out.println(occurrences.getLength());
        for (int i = 0; i < occurrences.getLength(); i++) {
            Node occurrence = occurrences.item(i);
            //TODO move to metadata
            //String foreignKey = ((Element) instance).getAttribute(settings.getForeignKey());

            // Add fields
            buildFields(section, sectionName, occurrence);

            // Parse child sections of this occurrence
            if (settings.getSubsectionsOf(sectionName) != null) {
                List<FormSection> subsections = new ArrayList<>();
                List<String> subsectionList = settings.getSubsectionsOf(sectionName);
                System.out.println(subsectionList);
                for (String subName : subsectionList) {
                    System.out.println(subName);
                    FormSection subsection = buildSection(occurrence, subName);
                    subsections.add(subsection);
                }
                //TODO make it possible to find subsection by name. May run in to weird recursion scenarios with bad build.
                section.setSubSections(subsections);
            }


        }
        System.out.println("Section: " + sectionName);
        return section;
    }

    private static void buildFields(FormSection section, String sectionName, Node contextNode)  {
        for (String fieldName : settings.getFormSectionToFields().getOrDefault(sectionName, Collections.emptyList())) {
            section.addField(fieldName, createField(section, fieldName, contextNode));
        }
    }

    private static FormField createField(FormSection section, String fieldName, Node contextNode) {
        String fieldValue = safeEvaluate(contextNode, settings.getFormFieldToPath().get(fieldName));
        return new FormField(fieldValue, fieldName, section.getFieldCount(), section.getFieldCountByName(fieldName));

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

    public static Node safeEvaluateNode(Node context, String path) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (Node) xpath.evaluate(path, context, XPathConstants.NODE);
    }


}
