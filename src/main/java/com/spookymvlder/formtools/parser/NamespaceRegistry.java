package com.spookymvlder.formtools.parser;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Assigns a stable, internally-generated prefix to each distinct namespace URI a schema declares (via
 * FieldProperty's per-field namespaceUri column), and exposes that mapping to the javax.xml.xpath
 * engine as a {@link NamespaceContext}.
 * <p>
 * These generated prefixes (ns1, ns2, ...) have nothing to do with whatever prefix the actual source
 * XML document happens to use for a namespace, or whether it uses a default (unprefixed) xmlns at all
 * - XPath only ever matches by namespace URI, so the query engine's own prefix is a private
 * implementation detail of how we write the path string, decoupled from the document's syntax.
 * <p>
 * One instance of this class is created per {@link FormParser} (so per document parse), not shared
 * or held statically - there is no reason for namespace-to-prefix assignments from one parse to leak
 * into another.
 */
public class NamespaceRegistry implements NamespaceContext {

    private final Map<String, String> uriToPrefix = new LinkedHashMap<>();
    private int nextIndex = 1;

    /**
     * Returns {@code localName} qualified with a generated prefix bound to {@code namespaceUri}, or
     * {@code localName} unchanged if {@code namespaceUri} is null/blank - preserving the exact
     * unprefixed-matching behavior that predates this column existing.
     */
    public String qualify(String localName, String namespaceUri) {
        if (namespaceUri == null || namespaceUri.isBlank()) {
            return localName;
        }
        return prefixFor(namespaceUri) + ":" + localName;
    }

    private String prefixFor(String namespaceUri) {
        return uriToPrefix.computeIfAbsent(namespaceUri, uri -> "ns" + nextIndex++);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix cannot be null");
        }
        if (XMLConstants.XML_NS_PREFIX.equals(prefix)) {
            return XMLConstants.XML_NS_URI;
        }
        if (XMLConstants.XMLNS_ATTRIBUTE.equals(prefix)) {
            return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
        }
        for (Map.Entry<String, String> entry : uriToPrefix.entrySet()) {
            if (entry.getValue().equals(prefix)) {
                return entry.getKey();
            }
        }
        return XMLConstants.NULL_NS_URI;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException("namespaceURI cannot be null");
        }
        if (XMLConstants.XML_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XML_NS_PREFIX;
        }
        if (XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            return XMLConstants.XMLNS_ATTRIBUTE;
        }
        return uriToPrefix.get(namespaceURI);
    }

    @Override
    public Iterator<String> getPrefixes(String namespaceURI) {
        String prefix = getPrefix(namespaceURI);
        return prefix == null ? Collections.emptyIterator() : Collections.singletonList(prefix).iterator();
    }
}
