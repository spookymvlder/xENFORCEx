package com.spookymvlder.formtools.parser;

import com.spookymvlder.formtools.datatypes.ErrorEntry;
import com.spookymvlder.formtools.datatypes.FormInstance;
import com.spookymvlder.formtools.settings.ErrorSettings;
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

    // Temp main for testing.
    public static void main(String[] args) {
        //TODO remove when unnecessary.

        TEMPSETTINGS.setTestSettings();
        ImportSpecification.importSpec(settings.getSpecFilePath());

        //If we don't have a successful spec import, quit out.
        if(!settings.getSpecImported()) {
            return;
        }



        try{
        Document doc = importXML();
        //TODO send rootField from fieldProcessorContext instead
        FormParser parser = new FormParser(doc, settings.getRootField());
        List<FormInstance> forms = parser.parseForm();
        //ParseDoc.parseDoc(doc);
            //TODO remove
            System.out.println(forms);
            ErrorSettings errors = ErrorSettings.getInstance();
            //TODO this is only checking latest log, but log is currently only opened once per run.
            System.out.println("Errors: " + errors.getCurrentLog().size());
            for (int i = 1; i <= errors.getLogCount(); i++) {
                List<ErrorEntry> errorLog = errors.getErrorLog(i);
                for (ErrorEntry errorEntry : errorLog) {
                    System.out.println(errorEntry);
                }
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }



    public static Document importXML() throws Exception {
        //TODO put in specification version ID for xml so can validate that against our xpaths saved. Also put in xml id and store that temporarily so we don't double parse.
        File xmlFile = new File(settings.getXmlFilePath());
        File xsdFile = new File(settings.getXsdFilePath());
        if(!settings.getTestIgnoreXSD()) {
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