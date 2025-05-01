package com.spookymvlder.formtools.parser;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Troubleshooter {

    //call with (doc.getDocumentElement(), "")
    private static void printNode(Node node, String indent) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            System.out.print(indent + "<" + node.getNodeName());

            NamedNodeMap attributes = node.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                System.out.print(" " + attr.getNodeName() + "=\"" + attr.getNodeValue() + "\"");
            }
            System.out.println(">");

            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                printNode(children.item(i), indent + "  ");
            }

            System.out.println(indent + "</" + node.getNodeName() + ">");
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            String text = node.getTextContent().trim();
            if (!text.isEmpty()) {
                System.out.println(indent + text);
            }
        }

    }
}
