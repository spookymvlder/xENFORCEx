package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.*;
import com.spookymvlder.formtools.datatypes.fieldproperties.FieldProperty;
import com.spookymvlder.formtools.datatypes.fields.*;
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
    // Built fresh per FormParser (per document parse) - see NamespaceRegistry's own javadoc for why
    // its prefix assignments shouldn't persist or be shared across parses.
    private final NamespaceRegistry namespaceRegistry = new NamespaceRegistry();



    /**
     * Sets up the form parser to iterate over a xml file and field property tree.
     * @param xmlDoc - the doc being parsed.
     * @param instanceRootProperty - the start of a form
     */
    public FormParser(Document xmlDoc, FieldProperty instanceRootProperty) {
        xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(namespaceRegistry);
        this.xmlDoc = xmlDoc;
        this.instanceRootProperty = instanceRootProperty;

        String rootTemplatePath = instanceRootProperty.getIdentity().getTemplatePath();
        if (rootTemplatePath == null || rootTemplatePath.isBlank()) {
            rootTemplatePath = buildRelativePath(instanceRootProperty);
            if (rootTemplatePath != null) {
                instanceRootProperty.getIdentity().setTemplatePath(rootTemplatePath);
            }
        }

        if (rootTemplatePath != null) {
            rootTemplatePath = rootTemplatePath.trim();

            if (rootTemplatePath.startsWith("/")) {
                rootTemplatePath = rootTemplatePath.substring(1);
                instanceRootProperty.getIdentity().setTemplatePath(rootTemplatePath);
            }
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


//TODO ensure this is updated correctly to use new parseForm
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
                FieldData data = new FieldData("root", FieldDataType.ROOT, i);

                // Absolute XPath (debugging)
                //String currentPath = pathContext.getRootInstanceXPath(i);
                String resolvedRootXPath  = DomPaths.absoluteXPath(instanceNode);

                SimpleField rootField = new SimpleField(
                        instanceRootProperty.getIdentity().getDisplayName(),
                        instanceRootProperty,
                        resolvedRootXPath ,
                        null,
                        data
                );

                // Parse children under the instance root node
                for (FieldProperty child : instanceRootProperty.getRelation().getChildFields()) {
                    List<FormField> children = parseNodes(instanceNode, child, rootField);
                    for (FormField childField : children) {
                        rootField.addChild(childField);
                    }
                }

                FormInstance instance = new FormInstance(instanceRootProperty.getIdentity().getDisplayName(), rootField);
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
        String rootTemplatePath = instanceRootProperty.getIdentity().getTemplatePath();
        if (rootTemplatePath == null) {
            errors.addToCurrentLog(new ErrorEntry(
                    ErrorSource.PARSE_ERROR,
                    "Instance root property is missing a template path.",
                    ErrorSeverity.FATAL
            ));
            return null;
        }

        return RootXPaths.selectRootNodes(xpath, xmlDoc, rootTemplatePath);
    }

    private String buildRelativePath(FieldProperty property) {
        String path = property.getIdentity().getTemplatePath();
        if (path != null) {
            return path;
        }
        FieldProperty parent = property.getRelation().getParentField();
        if (parent != null) {
            return buildRelativePath(parent) + '/'
                    + namespaceRegistry.qualify(property.getFieldName(), property.getIdentity().getNamespaceUri());
        }
        // Should be document anchor. Should have already been validated
        else if (property.getFieldName().equals(ImportSettings.getInstance().getAnchorElementName())) {
            return namespaceRegistry.qualify(property.getFieldName(), property.getIdentity().getNamespaceUri());
        } else {
            String message = "Field property '" + property.getFieldName() + "' is not the document anchor but lacks a parent element.";
            errors.addToCurrentLog(new ErrorEntry(ErrorSource.PARSE_ERROR, message, ErrorSeverity.FATAL));
            return null;
        }
    }



    private List<FormField> parseNodes(Node parentNode, FieldProperty prop, FormField parent)
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
        List<Node> matches = childElementsByName(parentNode, name, prop.getIdentity().getNamespaceUri());

        if (matches.isEmpty()) return Collections.emptyList();

        List<FormField> results = new ArrayList<>(matches.size());

        for (int i = 0; i < matches.size(); i++) {
            Node thisNode = matches.get(i);

            String resolvedXPath = DomPaths.absoluteXPath(thisNode);

            // skipImport: do not create a field, but DO descend from this node
            if (prop.getIdentity().getSkipImport()) {
                for (FieldProperty child : prop.getRelation().getChildFields()) {
                    List<FormField> childFields = parseNodes(thisNode, child, parent);
                    for (FormField cf : childFields) parent.addChild(cf);
                }
                continue;
            }

            FormField field;

            // TODO ensure that FieldProperty is converted to enum during processing.
            GroupingType fieldType = prop.getRelation().getGroupingType();
            switch (fieldType) {
                case CONTAINER: {
                    field = new ContainerField(prop.getIdentity().getDisplayName(), prop, resolvedXPath, parent);
                    break;
                }
                case COMPOSITE: {
                    Map<String, FieldData> compositeData = parseConstituentsFromNode(thisNode, prop);
                    field = new CompositeField(prop.getIdentity().getDisplayName(), prop, resolvedXPath, parent, compositeData);
                    break;
                }
                case CONSTITUENT: {
                    errors.addToCurrentLog(new ParserError(
                            ErrorSource.PARSE_ERROR,
                            "Unexpected constituent found: " + prop.getFieldId(),
                            ErrorSeverity.ERROR,
                            thisNode.getNodeName(),
                            resolvedXPath
                    ));
                    continue;
                }
                default: {
                    FieldData data = parseFieldValueFromNode(thisNode, prop, i);
                    field = new SimpleField(prop.getIdentity().getDisplayName(), prop, resolvedXPath, parent, data);
                }
            }


            for (FieldProperty child : prop.getRelation().getChildFields()) {
                List<FormField> childFields = parseNodes(thisNode, child, field);
                for (FormField cf : childFields) field.addChild(cf);
            }

            results.add(field);
        }

        return results;
    }

    /**
     * Matches by local name (not {@link Node#getNodeName()}, which includes any source-document
     * prefix) plus, when {@code namespaceUri} is non-null/blank, an exact namespace match. A null/
     * blank namespaceUri imposes no namespace constraint at all - this is what keeps every existing,
     * un-namespaced schema's matching behavior byte-identical to before this parameter existed.
     */
    private static List<Node> childElementsByName(Node parent, String name, String namespaceUri) {

        List<Node> out = new ArrayList<>();
        NodeList kids = parent.getChildNodes();

        for (int i = 0; i < kids.getLength(); i++) {
            Node k = kids.item(i);
            if (k.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            // getLocalName() is null for a node obtained without namespace-aware parsing; this
            // codebase's DocumentBuilderFactory always sets namespace-awareness, so the fallback is
            // defensive rather than expected to trigger.
            String localName = k.getLocalName() != null ? k.getLocalName() : k.getNodeName();
            if (!name.equals(localName)) {
                continue;
            }
            if (namespaceUri != null && !namespaceUri.isBlank() && !namespaceUri.equals(k.getNamespaceURI())) {
                continue;
            }
            out.add(k);
        }
        return out;
    }

    // Should only be called on GroupingType = None. TODO consider error.
    private FieldData parseFieldValueFromNode(Node thisNode, FieldProperty prop, int position) {
        String value = "";

        if (prop.getValueRestriction().getAttribute()) {
            String attr = prop.getValueRestriction().getAttributeName();
            if (attr != null && !attr.isBlank()) {
                // XPath against the node is simplest for attributes
                value = safeEvaluate(thisNode, "@" + attr);
            }
        } else {
            value = safeEvaluate(thisNode, "text()");
        }

        return new FieldData(value, prop.getValueRestriction().getDataType(), position);
    }

    private Map<String, FieldData> parseConstituentsFromNode(Node thisNode, FieldProperty property) {
        Map<String, FieldData> constituents = new LinkedHashMap<>();
        int counter = 0;

        for (FieldProperty constituent : property.getRelation().getConstituents()) {
            String name = constituent.getFieldName();
            String v = "";

            if (constituent.getValueRestriction().getAttribute()) {
                String attr = constituent.getValueRestriction().getAttributeName();
                if (attr != null && !attr.isBlank()) v = safeEvaluate(thisNode, "@" + attr);
            } else {
                // constituent elements are typically children of the composite node
                String qualifiedName = namespaceRegistry.qualify(name, constituent.getIdentity().getNamespaceUri());
                v = safeEvaluate(thisNode, qualifiedName + "/text()");
            }

            constituents.put(constituent.getIdentity().getDisplayName(),
                    new FieldData(v, constituent.getValueRestriction().getDataType(), counter++));
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

/*
    private FieldData parseSimpleField(String resolvedPath, FieldProperty prop) {
        String value = safeEvaluateFromDoc(resolvedPath);
        return new FieldData(value, prop.getValue().getDataType(), 0);
    }

    private Map<String, FieldData> parseConstituents(String resolvedPath, FieldProperty property) {
        Map<String, FieldData> constituents = new LinkedHashMap<>();

        int counter = 0;
        for (FieldProperty constituent : property.getRelation().getConstituents()) {
            String value = safeEvaluateFromDoc(resolvedPath);

            FieldDataType type = constituent.getValue().getDataType();

            FieldData data = new FieldData(value, type, counter);
            constituents.put(constituent.getIdentity().getDisplayName(), data);
            counter++;
        }

        return constituents;
    }
*/

}
