package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.*;
import com.spookymvlder.formtools.settings.ErrorSettings;
import com.spookymvlder.formtools.settings.ImportSettings;
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
    private final FieldProperty instanceRootProperty;
    private final Document xmlDoc;
    private final XmlPathContext pathContext;

    /**
     * Sets up the form parser to iterate over a xml file and field property tree.
     * @param xmlDoc - the doc being parsed.
     * @param instanceRootProperty - the start of a form
     */
    public FormParser(Document xmlDoc, FieldProperty instanceRootProperty) {
        xpath = XPathFactory.newInstance().newXPath();
        this.xmlDoc = xmlDoc;
        this.instanceRootProperty = instanceRootProperty;


        // Ensure we have a root relative path
        String rootPath = instanceRootProperty.getRelativePath();
        if (rootPath == null) {
            rootPath = buildRelativePath(instanceRootProperty);
            if (rootPath != null) {
                instanceRootProperty.setRelativePath(rootPath);
            }
        }
        if (rootPath == null) {
            // buildRelativePath already logged a fatal error
            this.pathContext = null;
        } else {
            this.pathContext = new XmlPathContext(xpath, rootPath);
        }
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
        //TODO create helper utility method to handle index errors and order checking. Then remove extra parseForm
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

        List<FormInstance> parsedInstances = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            Node instanceNode = formRootNodes.item(i);
            if (instanceNode == null) {
                errors.addToCurrentLog(new ErrorEntry(
                        ErrorSource.PARSE_ERROR,
                        "Null instance node encountered at index " + i,
                        ErrorSeverity.ERROR
                ));
                continue;
            }

            try {
                // Root field for this form instance
                FieldData data = new FieldData("root", FieldDataTypes.ROOT, i);

                // Absolute XPath (debugging)
                String currentPath = pathContext.getRootInstanceXPath(i);

                SimpleField rootField = new SimpleField(
                        instanceRootProperty.getDisplayName(),
                        instanceRootProperty,
                        currentPath,
                        null,
                        data
                );

                // Parse children under the instance root node
                for (FieldProperty child : instanceRootProperty.getChildFields()) {
                    List<FormField> children = parseNodes(instanceNode, child, rootField, "", currentPath);
                    for (FormField childField : children) {
                        rootField.addChild(childField);
                    }
                }

                FormInstance instance = new FormInstance(instanceRootProperty.getDisplayName(), rootField);
                parsedInstances.add(instance);

            } catch (XPathExpressionException e) {
                errors.addToCurrentLog(new ErrorEntry(
                        ErrorSource.PARSE_ERROR,
                        "Unable to extract form instance at index " + i + ": " + e.getMessage(),
                        ErrorSeverity.ERROR
                ));
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
        /*
        // Use XPath or a BFS to locate the first matching node
        String rootPath = instanceRootProperty.getRelativePath();
        if (rootPath == null) {
            rootPath = buildRelativePath(instanceRootProperty);
            if (rootPath != null) {
                instanceRootProperty.setRelativePath(rootPath);
            } else {
                //TODO better handling? Should be handled already.
                return null;
            }
        }

        XPathExpression expr = xpath.compile("/" + rootPath);
        NodeList rootNodes = (NodeList) expr.evaluate(xmlDoc, XPathConstants.NODESET);
        if (rootNodes == null) {
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR,
                    "Form root node not found by parseForm.", ErrorSeverity.FATAL));
            return null;
        }
        return rootNodes;

         */
        if (pathContext == null) return null;

        NodeList rootNodes = pathContext.selectRootNodes(xmlDoc);
        if (rootNodes == null || rootNodes.getLength() == 0) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.PARSE_ERROR,
                    "Form root node not found by parseForm. XPath: " + pathContext.getRootSetXPath(),
                    ErrorSeverity.FATAL
            ));
            return null;
        }
        return rootNodes;
    }

    private String buildRelativePath(FieldProperty property) {
        String path = property.getRelativePath();
        if (path != null) {
            return path;
        }
        FieldProperty parent = property.getParentField();
        if (parent != null) {
            return buildRelativePath(parent) + '/' + property.getFieldName();
        }
        // Should be document anchor. Should have already been validated
        else if (property.getFieldName().equals(ImportSettings.getInstance().getAnchorElementName())) {
            return property.getFieldName();
        } else {
            String message = "Field property '" + property.getFieldName() + "' is not the document anchor but lacks a parent element.";
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR, message, ErrorSeverity.FATAL));
            return null;
        }
    }



    private List<FormField> parseNodes(Node contextNode, FieldProperty prop, FormField parent, String parentLabel, String callPath)
            throws XPathExpressionException {

        String name = prop.getFieldName();
        if (name == null || name.isBlank()) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.PARSE_ERROR,
                    "FieldProperty has blank fieldName. fieldId=" + prop.getFieldId(),
                    ErrorSeverity.FATAL
            ));
            return Collections.emptyList();
        }

        // Match all child elements with this name (repeats handled here)
        List<Node> matches = childElementsByName(contextNode, name);

        if (matches.isEmpty()) return Collections.emptyList();

        List<FormField> results = new ArrayList<>(matches.size());

        for (int i = 0; i < matches.size(); i++) {
            Node thisNode = matches.get(i);

            String indexedStep = name + "[" + (i + 1) + "]"; // XPath-style index
            String resolvedPath = callPath.isEmpty() ? indexedStep : callPath + "/" + indexedStep;

            // skipImport: do not create a field, but DO descend from this node

            if (prop.getSkipImport()) {
                System.out.println("skipImport=" + prop.getSkipImport() + " prop=" + prop.getFieldName());
                for (FieldProperty child : prop.getChildFields()) {
                    for (FormField skippedChild : parseNodes(thisNode, child, parent, parentLabel, resolvedPath)) {
                        parent.addChild(skippedChild);
                    }
                }
                continue;
            }

            // Label is a container type: it exists to disambiguate structure, not to extract a giant value.
            String label = parentLabel;

            FormField field;
            switch (prop.getGroupingType()) {
                case COMPOSITE: {
                    // composite: constituents read relative to resolvedPath, or better: relative to thisNode (next improvement)
                    Map<String, FieldData> compositeData = parseConstituentsFromNode(thisNode, prop);
                    field = new CompositeField(prop.getDisplayName(), prop, resolvedPath, parent, compositeData);
                    break;
                }
                case CONSTITUENT:
                    errors.addToCurrentLog(new ErrorEntry(
                            ErrorSource.PARSE_ERROR,
                            "Unexpected constituent found: " + prop.getFieldId(),
                            ErrorSeverity.ERROR
                    ));
                    continue;

                default: {
                    FieldData data = parseFieldValueFromNode(thisNode, prop);
                    field = new SimpleField(prop.getDisplayName(), prop, resolvedPath, parent, data);
                }
            }

            field.setLabel(label);

            for (FieldProperty child : prop.getChildFields()) {
                for (FormField childField : parseNodes(thisNode, child, field, label, resolvedPath)) {
                    field.addChild(childField);
                }
            }

            results.add(field);
        }

        return results;
    }

    private static List<Node> childElementsByName(Node parent, String name) {

        List<Node> out = new ArrayList<>();
        NodeList kids = parent.getChildNodes();

        for (int i = 0; i < kids.getLength(); i++) {
            Node k = kids.item(i);
            if (k.getNodeType() == Node.ELEMENT_NODE && name.equals(k.getNodeName())) {
                out.add(k);
            }
        }
        return out;
    }

    private FieldData parseFieldValueFromNode(Node thisNode, FieldProperty prop) {
        String value = "";

        if (prop.getAttribute()) {
            String attr = prop.getAttributeName();
            if (attr != null && !attr.isBlank()) {
                // XPath against the node is simplest for attributes
                value = safeEvaluate(thisNode, "@" + attr);
            }
        } else {
            // Only non-label types read element text
            if (!"label".equals(prop.getDataType())) {
                value = safeEvaluate(thisNode, "text()");
            }
        }

        return new FieldData(value, parseFieldType(prop.getDataType()), 0);
    }

    private Map<String, FieldData> parseConstituentsFromNode(Node thisNode, FieldProperty property) {
        Map<String, FieldData> constituents = new LinkedHashMap<>();
        int counter = 0;

        for (FieldProperty constituent : property.getConstituents()) {
            String name = constituent.getFieldName();
            String v = "";

            if (constituent.getAttribute()) {
                String attr = constituent.getAttributeName();
                if (attr != null && !attr.isBlank()) v = safeEvaluate(thisNode, "@" + attr);
            } else {
                // constituent elements are typically children of the composite node
                v = safeEvaluate(thisNode, name + "/text()");
            }

            constituents.put(constituent.getDisplayName(),
                    new FieldData(v, parseFieldType(constituent.getDataType()), counter++));
        }

        return constituents;
    }

    public static String safeEvaluate(Node context, String path) {
        if (context == null || path == null || path.isBlank()) return "";
        try {
            String result = (String) xpath.evaluate(path, context, XPathConstants.STRING);
            return result != null ? result.trim() : "";
        } catch (XPathExpressionException e) {
            //TODO review behavior
            return "";
        }
    }

    public String safeEvaluateFromDoc(String absoluteXPath) {
        if (absoluteXPath == null || absoluteXPath.isBlank()) return "";
        try {
            String result = (String) xpath.evaluate(absoluteXPath, xmlDoc, XPathConstants.STRING);
            return result != null ? result.trim() : "";
        } catch (XPathExpressionException e) {
            return "";
        } catch (RuntimeException e) {
            // catches NPE/IllegalArgumentException from bad expressions
            return "";
        }
    }

    private FieldData parseSimpleField(String resolvedPath, FieldProperty prop) {
        String value = safeEvaluateFromDoc(resolvedPath);
        return new FieldData(value, parseFieldType(prop.getDataType()), 0);
    }

    private Map<String, FieldData> parseConstituents(String resolvedPath, FieldProperty property) {
        Map<String, FieldData> constituents = new LinkedHashMap<>();

        int counter = 0;
        for (FieldProperty constituent : property.getConstituents()) {
            String value = safeEvaluateFromDoc(resolvedPath);

            FieldDataTypes type = parseFieldType(constituent.getDataType());

            FieldData data = new FieldData(value, type, counter);
            constituents.put(constituent.getDisplayName(), data);
            counter++;
        }

        return constituents;
    }

    //Used to find if there are sibling elements for the same node to get their relative path correctly.
    //TODO test that this works correctly for anything not the first element.
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
