package com.spookymvlder.formtools.parser;

import org.w3c.dom.*;

import java.util.ArrayDeque;
import java.util.Deque;

public final class DomPaths {
    private DomPaths() {}

    /** Returns an absolute XPath (1-based indices) for the given node. */
    public static String absoluteXPath(Node node) {
        if (node == null) return null;

        // If it's an attribute node, point to its owner + /@name
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            Attr a = (Attr) node;
            Element owner = a.getOwnerElement();
            String ownerPath = absoluteXPath(owner);
            return ownerPath + "/@" + a.getName();
        }

        Deque<String> steps = new ArrayDeque<>();
        Node cur = node;

        while (cur != null && cur.getNodeType() != Node.DOCUMENT_NODE) {
            if (cur.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) cur;
                String name = el.getTagName(); // includes prefix if present (e.g. ns:tag)
                int index = elementIndexAmongSameNamedSiblings(el); // 1-based
                steps.push("/" + name + "[" + index + "]");
            }
            cur = cur.getParentNode();
        }

        if (steps.isEmpty()) return "/";

        StringBuilder sb = new StringBuilder();
        while (!steps.isEmpty()) sb.append(steps.pop());
        return sb.toString();
    }

    /** 1-based index among element siblings with the same tagName. */
    private static int elementIndexAmongSameNamedSiblings(Element el) {
        Node parent = el.getParentNode();
        if (parent == null) return 1;

        String name = el.getTagName();
        int index = 0;

        Node child = parent.getFirstChild();
        while (child != null) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element ce = (Element) child;
                if (name.equals(ce.getTagName())) {
                    index++;
                    if (ce == el) return index;
                }
            }
            child = child.getNextSibling();
        }
        // Should never happen
        return 1;
    }
}
