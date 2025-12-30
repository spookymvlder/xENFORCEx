package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.*;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.utilities.ErrorSeverity;
import com.spookymvlder.formtools.utilities.ErrorSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;
import java.util.*;


//New version of ParseDoc. Starting over instead of trying to untangle 4 versions of the parser.

public class FormParser {
    private final ErrorSettings errors = ErrorSettings.getInstance();
    private static XPath xpath;         // TODO see if static xpath would cause problems going forward. Possible issues include multiple forms or building FormParser multiple times before parsing different forms.
    private final FieldProperty rootProperty;
    private final Document xmlDoc;

    /**
     * Sets up the form parser to iterate over a xml file and field property tree.
     * @param xmlDoc - the doc being parsed.
     * @param rootProperty - the start of a form
     */
    public FormParser(Document xmlDoc, FieldProperty rootProperty) {
        xpath = XPathFactory.newInstance().newXPath();
        this.xmlDoc = xmlDoc;
        this.rootProperty = rootProperty;
    }

    /**
     * Parse the form based on the root property set.
     * @return all instances of the start node.
     * @throws XPathExpressionException - no root field found.
     */
    //TODO this should either return all instances of the form or the specified instance of the form
    public List<FormInstance> parseForm() throws XPathExpressionException {
        //Get all nodes matching the root of the form.
        NodeList formRootNodes = findFormRootNode();
        if (formRootNodes == null) {
            return null;
        }
        return parseFormIndex(0, formRootNodes.getLength(), formRootNodes);
    }


    public List<FormInstance> parseForm(int startIndex, int endIndex) throws XPathExpressionException {
        if ((startIndex > endIndex) || (startIndex < 0)) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "At least one parseForm instance index is out of bounds", ErrorSeverity.FATAL));
            return null;
        }


        NodeList formRootNodes = findFormRootNode();
        if (formRootNodes == null) {
            return null;
        }
        //TODO create helper utility method to handle index errors and order checking.
        int formLength = formRootNodes.getLength();
        if (startIndex > formLength) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "Start index (" + startIndex + ") of parseForm is greater than the size of instances found within the XML (" + formLength + "). ", ErrorSeverity.FATAL));
            return null;
        }

        if (formLength < endIndex) {
            endIndex = formLength;
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "End index (" + endIndex + ") of parseForm is greater than the size of instances found within the XML (" + formLength + "). ", ErrorSeverity.NOTIFICATION));
        } else {
            endIndex++;     //Increment endIndex as index implies inclusive rather than exclusive.
        }

        return parseFormIndex(startIndex, endIndex, formRootNodes);
    }

    /**
     * Handle actual parsing of the instance node and its children.
     * Parameters expected to be validated by callers.
     * @param startIndex
     * @param endIndex
     * @param formRootNodes
     * @return
     * @throws XPathExpressionException
     */
    private List<FormInstance> parseFormIndex(int startIndex, int endIndex, NodeList formRootNodes) throws XPathExpressionException {
        if (formRootNodes == null) {
            return null;
        }
        //TODO validate index values, incorporate parseForm().

        List<FormInstance> parsedInstances = new ArrayList<>();


        for (int i = startIndex; i < endIndex; i++) {
            //Iterate per instance of form
            Node instanceNode = formRootNodes.item(i);
            try {
                //Create new root field
                FieldData data = new FieldData("root", FieldDataTypes.ROOT, i);


                //TODO call simple field directly

                String currentPath = "//" + rootProperty.getRelativePath() + "[" + i + "]";
                SimpleField rootField = new SimpleField(rootProperty.getDisplayName(), rootProperty, currentPath,null, data);

                for (FieldProperty child : rootProperty.getChildFields()) {
                    FormField childField = parseNode(instanceNode, child, rootField, "", currentPath);
                    if (childField != null) {
                        rootField.addChild(childField);
                    }
                }

                FormInstance instance = new FormInstance(rootProperty.getDisplayName(), rootField);
                parsedInstances.add(instance);
                //TODO better validation here.
            } catch (XPathExpressionException e) {
                System.out.println("Unable to extract form start: " + e.getMessage());
            }
        }
        return parsedInstances;
    }

    /**
     * Gets a NodeList corresponding to the start of a form instance. Callers should quit if null returned.
     * @return
     * @throws XPathExpressionException
     */
    //TODO consider changing name from root to start if not actual root.
    private NodeList findFormRootNode() throws XPathExpressionException {
        // Use XPath or a BFS to locate the first matching node
        String rootPath = rootProperty.getRelativePath();
        XPathExpression expr = xpath.compile("//" + rootPath);
        NodeList rootNodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
        if (rootNodes == null) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "Form root node not found by parseForm.", ErrorSeverity.FATAL));
            return null;
        }
        return rootNodes;
    }


    private FormField parseNode(Node contextNode, FieldProperty prop, FormField parent, String parentLabel, String callPath) throws XPathExpressionException {

        String stepPath = prop.getRelativePath();
        int index = getElementIndex(contextNode);
        String indexedStep = index >= 0 ? stepPath + "[" + index + "]" : stepPath;
        String resolvedPath = callPath.isEmpty() ? indexedStep : callPath + "/" + indexedStep;

        if (prop.getSkipImport()) {
            for (FieldProperty child : prop.getChildFields()) {
                return parseNode(contextNode, child, parent, parentLabel, resolvedPath);
            }
        }


        String label = prop.getDataType().equals("label") ? safeEvaluate(contextNode, prop.getRelativePath()) : parentLabel;

        // Construct field
        FormField field;
        switch (prop.getGroupingType()) {
            case COMPOSITE:
                Map<String, FieldData> compositeData = parseConstituents(contextNode, prop);
                field = new CompositeField(prop.getDisplayName(), prop, resolvedPath, parent, compositeData);
                break;
            case CONSTITUENT:
                // Should be parsed by composite
                //TODO - WHY THOUGH?
                errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR, "Unexpected constituent found: " + prop.getFieldId(), ErrorSeverity.ERROR));
                return null;
            default:
                FieldData data = parseSimpleField(contextNode, prop);
                field = new SimpleField(prop.getDisplayName(), prop, resolvedPath, parent, data);
        }
        return field;
    }

    public static String safeEvaluate(Node context, String path) {
        try {
            String result = (String) xpath.evaluate(path, context, XPathConstants.STRING);
            return result != null ? result.trim() : "";
        } catch (XPathExpressionException e) {
            //TODO review behavior.
            return "";
        }
    }

    private FieldData parseSimpleField(Node contextNode, FieldProperty prop) {
        String value = safeEvaluate(contextNode, prop.getRelativePath());
        return new FieldData(value, parseFieldType(prop.getDataType()), 0);
    }

    private static Map<String, FieldData> parseConstituents(Node contextNode, FieldProperty property) {
        Map<String, FieldData> constituents = new LinkedHashMap<>();

        int counter = 0;
        for (FieldProperty constituent : property.getConstituents()) {
            String value = safeEvaluate(contextNode, constituent.getRelativePath());

            FieldDataTypes type = parseFieldType(constituent.getDataType());

            FieldData data = new FieldData(value, type, counter);
            constituents.put(constituent.getDisplayName(), data);
            counter++;
        }

        return constituents;
    }

    private int getElementIndex(Node node) {
        if (node == null || node.getNodeType() != Node.ELEMENT_NODE) return -1;

        Node parent = node.getParentNode();
        if (parent == null) return -1;

        NodeList siblings = parent.getChildNodes();
        String targetName = node.getNodeName();
        int count = 0;

        for (int i = 0; i < siblings.getLength(); i++) {
            Node sibling = siblings.item(i);
            if (sibling.getNodeType() == Node.ELEMENT_NODE && sibling.getNodeName().equals(targetName)) {
                count++;
                if (sibling == node) {
                    return count;  // XPath is 1-based index
                }
            }
        }
        return -1;
    }


    //TODO change FieldProperty to return an enum so we don't have to parse here.
    private static FieldDataTypes parseFieldType(String type) {
        FieldDataTypes enumType;
        try {
            enumType = FieldDataTypes.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            enumType = FieldDataTypes.STRING;
        }
        return enumType;
    }
}
