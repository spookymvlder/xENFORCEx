package com.spookymvlder.formtools.parser;

import javax.xml.xpath.*;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class RootXPaths {
    private RootXPaths() {}

    public static String rootSetXPath(String rootTemplatePath) {
        // rootTemplatePath must be like: "application" or "prospectiveDrivers/application"
        return "/" + rootTemplatePath;
    }

    public static NodeList selectRootNodes(XPath xpath, Node doc, String rootTemplatePath) throws XPathExpressionException {
        String expr = rootSetXPath(rootTemplatePath);
        return (NodeList) xpath.evaluate(expr, doc, XPathConstants.NODESET);
    }
}