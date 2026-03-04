package com.spookymvlder.formtools.datatypes;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class XmlPathContext {

    private final XPath xpath;
    private final String rootRelativePath;
    private final String rootSetXPath;
    private final Deque<PathFrame> stack = new ArrayDeque<>();

    /**
     * @param xpath shared XPath instance
     * @param rootRelativePath must be absolute (start with '/')
     */
    public XmlPathContext(XPath xpath, String rootRelativePath) {
        if (rootRelativePath == null || rootRelativePath.isBlank()) {
            throw new IllegalArgumentException("rootRelativePath cannot be null/blank");
        }
        if (rootRelativePath.startsWith("/")) {
            throw new IllegalArgumentException(
                    "rootRelativePath must be relative (no leading '/'): " + rootRelativePath);
        }
        this.xpath = xpath;
        this.rootRelativePath = rootRelativePath;
        this.rootSetXPath = "/" + rootRelativePath;
    }

    /* ---------------- Stack control ---------------- */

    public void push(String elementName) {
        stack.push(new PathFrame(elementName, null));
    }

    public void push(String elementName, int index) {
        if (index < 0) {
            throw new IllegalArgumentException("Index must be >= 0");
        }
        stack.push(new PathFrame(elementName, index));
    }

    public void pop() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("Path stack underflow");
        }
        stack.pop();
    }

    /* ---------------- Path resolution ---------------- */

    /**
     * Absolute XPath including root and all frames.
     */
    public String getResolvedXPath() {
        StringBuilder sb = new StringBuilder(rootSetXPath);

        Iterator<PathFrame> it = stack.descendingIterator();
        while (it.hasNext()) {
            PathFrame frame = it.next();
            sb.append('/').append(frame.name);
            if (frame.index != null) {
                // XPath is 1-based
                sb.append('[').append(frame.index + 1).append(']');
            }
        }

        return sb.toString();
    }

    /**
     * XPath relative to the root path (useful for FormField storage).
     */
    public String getRelativePath() {
        StringBuilder sb = new StringBuilder();

        Iterator<PathFrame> it = stack.descendingIterator();
        while (it.hasNext()) {
            PathFrame frame = it.next();
            if (sb.length() > 0) sb.append('/');
            sb.append(frame.name);
            if (frame.index != null) {
                sb.append('[').append(frame.index).append(']');
            }
        }

        return sb.toString();
    }

    /** XPath that selects all form instances (NODESET). */
    public String getRootSetXPath() {
        return rootSetXPath;
    }

    /** Absolute XPath for a specific instance index (0-based input, 1-based XPath output). */
    public String getRootInstanceXPath(int instanceIndex0) {
        if (instanceIndex0 < 0) throw new IllegalArgumentException("instanceIndex must be >= 0");
        return rootSetXPath + "[" + (instanceIndex0 + 1) + "]";
    }

    /** Relative path you may want to store on fields (0-based index for your internal notation). */
    public String getRootInstanceRelativePath(int instanceIndex0) {
        if (instanceIndex0 < 0) throw new IllegalArgumentException("instanceIndex must be >= 0");
        return rootRelativePath + "[" + instanceIndex0 + "]";
    }

    public NodeList selectRootNodes(Node contextNode) throws XPathExpressionException {
        return (NodeList) xpath.evaluate(getRootSetXPath(), contextNode, XPathConstants.NODESET);
    }

    /* ---------------- DOM access ---------------- */

    public Node selectSingle(Node contextNode) throws XPathExpressionException {
        return (Node) xpath.evaluate(
                getResolvedXPath(),
                contextNode,
                XPathConstants.NODE
        );
    }

    public NodeList selectAll(Node contextNode) throws XPathExpressionException {
        return (NodeList) xpath.evaluate(
                getResolvedXPath(),
                contextNode,
                XPathConstants.NODESET
        );
    }

    /* ---------------- Debugging ---------------- */

    @Override
    public String toString() {
        return getResolvedXPath();
    }
}
