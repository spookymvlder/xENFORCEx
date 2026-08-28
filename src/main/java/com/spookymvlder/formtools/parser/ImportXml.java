package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.settings.ImportSettings;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class ImportXml {

    private static final ImportSettings settings = ImportSettings.getInstance();

    // Entry point is now com.spookymvlder.formtools.cli.CliApp, which calls importXML() (below) as
    // one step of PipelineRunner's pipeline instead of this class running the whole thing inline.

    public static Document importXML() throws Exception {
        //TODO put in specification version ID for xml so can validate that against our xpaths saved. Also put in xml id and store that temporarily so we don't double parse.
        File xmlFile = new File(settings.getXmlFilePath());
        File xsdFile = new File(settings.getXsdFilePath());
        if(!settings.getSkipXsdValidation()) {
            validateXML(xmlFile, xsdFile);
        }
        return getDocument(xmlFile);
    }



    private static Boolean validateXmlSettings() {
        //TODO come back to when internet and see why not working.

        if(!settings.getXmlFilePath().toLowerCase().endsWith(".xml")) {
            return false;
        }
        return settings.getAnchorElementName() != null && settings.getRootId() != null;
    }

    private static Boolean validateXsdSettings() {
        if(!Paths.get(settings.getXsdFilePath()).endsWith(".xsd")) {
            return false;
        }
        return validateXmlSettings();
    }



    public static Document getDocument(File xmlFile) throws SAXException, IOException, ParserConfigurationException {

        if (!validateXmlSettings()){
            throw new InvalidPropertiesFormatException("Invalid XML settings.");
        }
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder builder = dbFactory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();
        //TODO ensure comments are stripped out too
        stripEmptyElements(doc.getElementsByTagName(settings.getAnchorElementName()).item(settings.getAnchorElementIndex()));
        return doc;
    }

    public static void validateXML(File xmlFile, File xsdFile) throws IOException, SAXException, ParserConfigurationException {
        //validateXML(xmlFile, xsdFile); Probably an error
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(xsdFile);
        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(xmlFile));
    }


    //https://stackoverflow.com/questions/11703635/strip-whitespace-and-newlines-from-xml-in-java
    private static void stripEmptyElements(Node node){
        NodeList children = node.getChildNodes();
        for(int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    child.getParentNode().removeChild(child);
                    i--;
                }
            }
            stripEmptyElements(child);
        }
    }



}